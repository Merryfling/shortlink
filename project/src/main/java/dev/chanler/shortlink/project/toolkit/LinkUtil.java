package dev.chanler.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

import static dev.chanler.shortlink.project.common.constant.LinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 * @author: Chanler
 */
public class LinkUtil {

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
}
