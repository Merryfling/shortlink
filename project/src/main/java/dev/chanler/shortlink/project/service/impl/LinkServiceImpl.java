package dev.chanler.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.chanler.shortlink.project.common.convention.exception.ClientException;
import dev.chanler.shortlink.project.common.convention.exception.ServiceException;
import dev.chanler.shortlink.project.common.enums.ValidDateTypeEnum;
import dev.chanler.shortlink.project.dao.entity.*;
import dev.chanler.shortlink.project.dao.mapper.*;
import dev.chanler.shortlink.project.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkUpdateReqDTO;
import dev.chanler.shortlink.project.dto.resp.GroupLinkCountQueryRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkPageRespDTO;
import dev.chanler.shortlink.project.service.LinkService;
import dev.chanler.shortlink.project.toolkit.HashUtil;
import dev.chanler.shortlink.project.toolkit.LinkUtil;
import dev.chanler.shortlink.project.toolkit.ipgeo.GeoInfo;
import dev.chanler.shortlink.project.toolkit.ipgeo.IpGeoClient;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.chanler.shortlink.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接接口实现层
 * @author: Chanler
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements LinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final LinkGotoMapper linkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final IpGeoClient ipGeoClient;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;


    @Override
    public LinkCreateRespDTO createLink(LinkCreateReqDTO linkCreateReqDTO) {
        String shortLinkSuffix = generateSuffix(linkCreateReqDTO);
        String fullShortUrl = StrBuilder.create(linkCreateReqDTO.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        LinkDO linkDO = LinkDO.builder()
                .domain(linkCreateReqDTO.getDomain())
                .originUrl(linkCreateReqDTO.getOriginUrl())
                .gid(linkCreateReqDTO.getGid())
                .createdType(linkCreateReqDTO.getCreatedType())
                .validDateType(linkCreateReqDTO.getValidDateType())
                .validDate(linkCreateReqDTO.getValidDate())
                .describe(linkCreateReqDTO.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(linkCreateReqDTO.getOriginUrl()))
                .build();
        LinkGotoDO linkGotoDO = LinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(linkCreateReqDTO.getGid())
                .build();
        try {
            baseMapper.insert(linkDO);
            linkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException ex) {
            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, fullShortUrl);
            LinkDO existLinkDO = baseMapper.selectOne(queryWrapper);
            if (existLinkDO != null) {
                log.warn("短链接:{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                linkCreateReqDTO.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(linkCreateReqDTO.getValidDate()),
                TimeUnit.MILLISECONDS
        );
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return LinkCreateRespDTO.builder()
                .fullShortUrl("http://" + linkDO.getFullShortUrl())
                .originUrl(linkDO.getOriginUrl())
                .gid(linkDO.getGid())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateLink(LinkUpdateReqDTO linkUpdateReqDTO) {
        Wrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, linkUpdateReqDTO.getGid())
                .eq(LinkDO::getFullShortUrl, linkUpdateReqDTO.getFullShortUrl())
                .eq(LinkDO::getDelFlag, 0)
                .eq(LinkDO::getEnableStatus, 0);
        LinkDO hasLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasLinkDO == null) {
            throw new ClientException("短链接不存在");
        }
        LinkDO linkDO = LinkDO.builder()
                .domain(hasLinkDO.getDomain())
                .shortUri(hasLinkDO.getShortUri())
                .clickNum(hasLinkDO.getClickNum())
                .favicon(hasLinkDO.getFavicon())
                .createdType(hasLinkDO.getCreatedType())
                .gid(linkUpdateReqDTO.getGid())
                .originUrl(linkUpdateReqDTO.getOriginUrl())
                .describe(linkUpdateReqDTO.getDescribe())
                .validDateType(linkUpdateReqDTO.getValidDateType())
                .validDate(linkUpdateReqDTO.getValidDate())
                .build();
        if (Objects.equals(hasLinkDO.getGid(), linkUpdateReqDTO.getGid())) {
            LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 0)
                    .eq(LinkDO::getFullShortUrl, linkUpdateReqDTO.getFullShortUrl())
                    .eq(LinkDO::getGid, linkUpdateReqDTO.getGid())
                    .set(Objects.equals(linkUpdateReqDTO.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), LinkDO::getValidDate, null);
            baseMapper.update(linkDO, updateWrapper);
        } else {
            LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 0)
                    .eq(LinkDO::getFullShortUrl, linkUpdateReqDTO.getFullShortUrl())
                    .eq(LinkDO::getGid, hasLinkDO.getGid());
            baseMapper.delete(updateWrapper);
            baseMapper.insert(linkDO);
        }
    }

    @Override
    public IPage<LinkPageRespDTO> pageLink(LinkPageReqDTO linkPageReqDTO) {
        LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getDelFlag, 0)
                .eq(LinkDO::getGid, linkPageReqDTO.getGid())
                .eq(LinkDO::getEnableStatus, 0)
                .orderByDesc(LinkDO::getCreateTime);
        IPage<LinkDO> resultPage = baseMapper.selectPage(linkPageReqDTO, queryWrapper);
        return resultPage.convert(each -> {
            LinkPageRespDTO bean = BeanUtil.toBean(each, LinkPageRespDTO.class);
            bean.setDomain("http://" + bean.getDomain());
            return bean;
        });
    }

    @Override
    public List<GroupLinkCountQueryRespDTO> listGroupLinkCount(List<String> gidList) {
        QueryWrapper<LinkDO> queryWrapper = Wrappers.query(new LinkDO())
                .select("gid as gid, count(*) as linkCount")
                .in("gid", gidList)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> LinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(LinkDOList, GroupLinkCountQueryRespDTO.class);
    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = StrBuilder.create(serverName).append("/").append(shortUri).toString();
        String originUrl = stringRedisTemplate.opsForValue().get(String.format((GOTO_SHORT_LINK_KEY), fullShortUrl));
        if (StrUtil.isNotBlank(originUrl)) {
            try {
                linkStats(fullShortUrl, request, response);
                ((HttpServletResponse) response).sendRedirect(originUrl);
            } catch (IOException e) {
                throw new ServiceException("重定向失败");
            }
            return;
        }
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            try {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
            } catch (IOException e) {
                throw new ServiceException("重定向失败");
            }
            return;
        }
        String gotoIsNullShortUrl = stringRedisTemplate.opsForValue().get(String.format((GOTO_IS_NULL_SHORT_LINK_KEY), fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortUrl)) {
            try {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
            } catch (IOException e) {
                throw new ServiceException("重定向失败");
            }
            return;
        }
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            originUrl = stringRedisTemplate.opsForValue().get(String.format((GOTO_SHORT_LINK_KEY), fullShortUrl));
            if (StrUtil.isNotBlank(originUrl)) {
                try {
                    linkStats(fullShortUrl, request, response);
                    ((HttpServletResponse) response).sendRedirect(originUrl);
                } catch (IOException e) {
                    throw new ServiceException("重定向失败");
                }
                return;
            }
            LambdaQueryWrapper<LinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(LinkGotoDO.class)
                    .eq(LinkGotoDO::getFullShortUrl, fullShortUrl);
            LinkGotoDO linkGotoDO = linkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (linkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(
                        String.format((GOTO_IS_NULL_SHORT_LINK_KEY), fullShortUrl),
                        "-", 30, TimeUnit.MINUTES
                );
                try {
                    ((HttpServletResponse) response).sendRedirect("/page/notfound");
                } catch (IOException e) {
                    throw new ServiceException("重定向失败");
                }
                return;
            }
            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, linkGotoDO.getFullShortUrl())
                    .eq(LinkDO::getGid, linkGotoDO.getGid())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 0);
            LinkDO linkDO = baseMapper.selectOne(queryWrapper);
            if (linkDO == null || (linkDO.getValidDate() != null && linkDO.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(
                        String.format((GOTO_IS_NULL_SHORT_LINK_KEY), fullShortUrl),
                        "-", 30, TimeUnit.MINUTES
                );
                try {
                    ((HttpServletResponse) response).sendRedirect("/page/notfound");
                } catch (IOException e) {
                    throw new ServiceException("重定向失败");
                }
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    linkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(linkDO.getValidDate()),
                    TimeUnit.MILLISECONDS
            );
            try {
                linkStats(fullShortUrl, request, response);
                ((HttpServletResponse) response).sendRedirect(linkDO.getOriginUrl());
            } catch (IOException e) {
                throw new ServiceException("重定向失败");
            }
        } finally {
            lock.unlock();
        }
    }

    private void linkStats(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addResponseCookieTask = () -> {
                uv.set(UUID.fastUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get());
                uvCookie.setMaxAge(60 * 60 * 24 * 30);
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                ((HttpServletResponse) response).addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(cookie -> Objects.equals(cookie.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(cookie -> {
                            uv.set(cookie);
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, cookie);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask);
            } else {
                addResponseCookieTask.run();
            }
            String uip = LinkUtil.getActualIp(((HttpServletRequest) request));
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, uip);
            Boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
            int hour = DateUtil.hour(new Date(), true);
            int weekDay = DateUtil.dayOfWeekEnum(new Date()).getIso8601Value();
            LinkAccessStatsDO linkAccessStats = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekDay)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkAccessStats(linkAccessStats);
            GeoInfo geoInfo = ipGeoClient.query(uip);
            LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .cnt(1)
                    .province(geoInfo.getProvince())
                    .city(geoInfo.getCity())
                    .adcode(geoInfo.getAdcode())
                    .country(geoInfo.getCountry())
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleStats(linkLocaleStatsDO);

            String os = LinkUtil.getOs((HttpServletRequest) request);
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .cnt(1)
                    .os(os)
                    .build();
            linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);

            String browser = LinkUtil.getBrowser((HttpServletRequest) request);
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .cnt(1)
                    .browser(browser)
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);

            String device = LinkUtil.getDevice((HttpServletRequest) request);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .cnt(1)
                    .device(device)
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceStats(linkDeviceStatsDO);

            String network = LinkUtil.getNetwork(geoInfo);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .cnt(1)
                    .network(network)
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkStats(linkNetworkStatsDO);

            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .user(uv.get())
                    .browser(browser)
                    .os(os)
                    .ip(uip)
                    .network(network)
                    .device(device)
                    .locale(StrBuilder.create(geoInfo.getCountry())
                            .append("-")
                            .append(geoInfo.getProvince())
                            .append("-")
                            .append(geoInfo.getCity())
                            .toString())
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
        } catch (Exception e) {
            log.error("短链接访问统计失败，fullShortUrl: {}", fullShortUrl, e);
        }
    }

    private String generateSuffix(LinkCreateReqDTO linkCreateReqDTO) {
        int customGenerateCount = 0;
        String shortUri;
        String fullShortUrl;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接生成频繁，请稍后再试");
            }
            String originUrl = linkCreateReqDTO.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            fullShortUrl = StrBuilder.create(linkCreateReqDTO.getDomain())
                    .append("/")
                    .append(shortUri)
                    .toString();
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    private String getFavicon(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            URL u = new URL(url);
            String protocol = u.getProtocol();
            String host = u.getHost();
            int port = u.getPort();
            StringBuilder base = new StringBuilder()
                    .append(protocol).append("://").append(host);
            if (port > 0 && port != u.getDefaultPort()) {
                base.append(":").append(port);
            }
            String baseUrl = base.toString();

            // 抓取页面 HTML（限 32KB）
            String html = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    StringBuilder sb = new StringBuilder();
                    try (InputStream in = conn.getInputStream();
                         InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        char[] buf = new char[4096];
                        int len;
                        int total = 0;
                        while ((len = reader.read(buf)) != -1 && total < 32768) {
                            sb.append(buf, 0, len);
                            total += len;
                        }
                    }
                    html = sb.toString();
                }
            } catch (Exception ignore) {
            }

            // 解析 <link rel="icon"...>
            if (StrUtil.isNotBlank(html)) {
                Pattern p = Pattern.compile("(?i)<link[^>]+rel=[\"'](?:shortcut\\s+)?icon[\"'][^>]*href=[\"']([^\"']+)[\"']");
                Matcher m = p.matcher(html);
                if (m.find()) {
                    String iconHref = m.group(1).trim();
                    if (iconHref.startsWith("http://") || iconHref.startsWith("https://")) {
                        return iconHref;
                    } else if (iconHref.startsWith("//")) {
                        return protocol + ":" + iconHref;
                    } else if (iconHref.startsWith("/")) {
                        return baseUrl + iconHref;
                    } else {
                        return baseUrl + "/" + iconHref;
                    }
                }
            }

            // 回退尝试 /favicon.ico
            String fallback = baseUrl + "/favicon.ico";
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(fallback).openConnection();
                c.setRequestMethod("GET"); // 有些站点不支持 HEAD
                c.setConnectTimeout(1500);
                c.setReadTimeout(1500);
                c.setRequestProperty("User-Agent", "Mozilla/5.0");
                int code = c.getResponseCode();
                String ct = c.getContentType();
                if (code >= 200 && code < 300 && ct != null && ct.toLowerCase().startsWith("image")) {
                    return fallback;
                }
            } catch (Exception ignore) {
            }
        } catch (Exception e) {
        }
        return null;
    }
}