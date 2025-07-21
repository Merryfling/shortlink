package dev.chanler.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
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
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import dev.chanler.shortlink.project.dao.entity.LinkGotoDO;
import dev.chanler.shortlink.project.dao.mapper.LinkGotoMapper;
import dev.chanler.shortlink.project.dao.mapper.LinkMapper;
import dev.chanler.shortlink.project.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkUpdateReqDTO;
import dev.chanler.shortlink.project.dto.resp.GroupLinkCountQueryRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkPageRespDTO;
import dev.chanler.shortlink.project.service.LinkService;
import dev.chanler.shortlink.project.toolkit.HashUtil;
import dev.chanler.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
            if (linkDO != null) {
                if (linkDO.getValidDate() != null && linkDO.getValidDate().before(new Date())) {
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
                    ((HttpServletResponse) response).sendRedirect(linkDO.getOriginUrl());
                } catch (IOException e) {
                    throw new ServiceException("重定向失败");
                }
            }
        } finally {
            lock.unlock();
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
}