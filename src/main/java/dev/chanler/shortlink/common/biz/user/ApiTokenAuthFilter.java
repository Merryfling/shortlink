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
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static dev.chanler.shortlink.common.convention.errorcode.BaseErrorCode.USER_TOKEN_FAIL;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.API_TOKEN_KEY_PREFIX;

/**
 * Core API Token 鉴权过滤器：拦截 /api/short-link/v1/*
 * 校验 Authorization: Bearer {token}，通过 TokenService 解析 username
 * 仅设置 UserContext 的 username 字段
 * @author: Chanler
 */
@RequiredArgsConstructor
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
            String key = String.format(API_TOKEN_KEY_PREFIX, token);
            username = stringRedisTemplate.opsForValue().get(key);
        } catch (Exception ignore) {
        }
        if (StrUtil.isBlank(username)) {
            throw new ClientException(USER_TOKEN_FAIL);
        }
        try {
            UserContext.setUser(UserInfoDTO.builder().username(username).build());
            chain.doFilter(request, response);
        } finally {
            UserContext.removeUser();
        }
    }
}
