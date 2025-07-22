package dev.chanler.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import dev.chanler.shortlink.project.toolkit.ipgeo.GeoInfo;
import dev.chanler.shortlink.project.toolkit.ipgeo.IpGeoClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.Optional;

import static dev.chanler.shortlink.project.common.constant.LinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 * @author: Chanler
 */
@RequiredArgsConstructor
public class LinkUtil {

    private static IpGeoClient ipGeoClient;

    /**
     * 获取短链接缓存有效时间
     * @param validDate 有效期时间
     * @return 缓存有效时间，单位：ms
     */
    public static long getLinkCacheValidTime(Date validDate) {
        return Optional.ofNullable(validDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }

    /**
     * 获取实际访问IP
     * @param request HttpServletResponse对象
     * @return 实际访问IP
     */
    public static String getActualIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                continue;
            }
            String first = ip.split(",")[0].trim();
            if (!first.isEmpty() && !"unknown".equalsIgnoreCase(first)) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取操作系统
     * @param request HttpServletResponse对象
     * @return 操作系统
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "Mac";
        } else if (userAgent.contains("x11") || userAgent.contains("linux")) {
            return "Unix";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取浏览器
     * @param request HttpServletResponse对象
     * @return 浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("edg")) {
            return "Edge";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        } else if (userAgent.contains("chrome")) {
            return "Chrome";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取设备类型
     * @param request HttpServletResponse对象
     * @return 设备类型
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("mobile")) {
            return "Mobile";
        } else {
            return "Desktop";
        }
    }

    /**
     * 获取网络类型
     * @param geoInfo GeoInfo 对象
     * @return 网络类型
     */
    public static String getNetwork(GeoInfo geoInfo) {
        if (geoInfo == null) {
            return "Unknown";
        }
        String isp = geoInfo.getIsp();
        if (isp == null) {
            return "Unknown";
        }
        return isp;
    }
}
