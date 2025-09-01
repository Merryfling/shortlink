package dev.chanler.shortlink.common.biz.user;

import cn.hutool.core.util.StrUtil;
import dev.chanler.shortlink.common.convention.exception.ClientException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static dev.chanler.shortlink.common.convention.errorcode.BaseErrorCode.USER_TOKEN_FAIL;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.API_TOKEN_HASH_KEY_PREFIX;

/**
 * Core API Token 鉴权过滤器：拦截 /api/short-link/v1/*
 * 校验 Authorization: Bearer {token}，通过 TokenService 解析 username
 * 仅设置 UserContext 的 username 字段
 * @author: Chanler
 */
@RequiredArgsConstructor
@Slf4j
public class ApiTokenAuthFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        // 解析 Authorization: Bearer {token}
        String authz = req.getHeader("Authorization");
        if (StrUtil.isBlank(authz) || !StrUtil.startWithIgnoreCase(authz, "Bearer ")) {
            throw new ClientException(USER_TOKEN_FAIL);
        }
        String token = StrUtil.trim(authz.substring(7));
        if (StrUtil.isBlank(token)) {
            throw new ClientException(USER_TOKEN_FAIL);
        }
        String username = null;
        try {
            String tokenHash = sha256Hex(token);
            String key = String.format(API_TOKEN_HASH_KEY_PREFIX, tokenHash);
            username = stringRedisTemplate.opsForValue().get(key);
        } catch (Throwable t) {
            log.error("Read api token mapping error", t);
        }
        if (StrUtil.isBlank(username)) {
            throw new ClientException(USER_TOKEN_FAIL);
        }
        try {
            UserContext.setUsername(username);
            chain.doFilter(request, response);
        } finally {
            UserContext.removeUser();
        }
    }

    private String sha256Hex(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new ClientException(USER_TOKEN_FAIL);
        }
    }
}
