package dev.chanler.shortlink.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import dev.chanler.shortlink.common.convention.exception.ClientException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static dev.chanler.shortlink.common.convention.errorcode.BaseErrorCode.USER_TOKEN_FAIL;

/**
 * @author: Chanler
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/v1/user/exists"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if (!IGNORE_URI.contains(requestURI)) {
            String method = httpServletRequest.getMethod();
            if (!(Objects.equals(requestURI, "/api/short-link/admin/v1/user") && Objects.equals(method, "POST"))) {
                String authz = httpServletRequest.getHeader("Authorization");
                if (StrUtil.isBlank(authz) || !StrUtil.startWithIgnoreCase(authz, "Bearer ")) {
                    throw new ClientException(USER_TOKEN_FAIL);
                }
                String token = StrUtil.trim(authz.substring(7));
                if (StrUtil.isBlank(token)) {
                    throw new ClientException(USER_TOKEN_FAIL);
                }
                String username;
                try {
                    String key = SESSION_KEY_PREFIX + token;
                    username = stringRedisTemplate.opsForValue().get(key);
                    if (StrUtil.isBlank(username)) {
                        throw new ClientException(USER_TOKEN_FAIL);
                    }
                    // 仅会话续期，不对 gid 索引续期
                    stringRedisTemplate.expire(key, SESSION_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                    // 仅刷新该用户 GID 正向索引集合 TTL（不回库、不补全）
                    expireUserGidsTTL(username);
                } catch (Exception e) {
                    throw new ClientException(USER_TOKEN_FAIL);
                }
                // 仅设置 username 即可
                UserContext.setUser(UserInfoDTO.builder().username(username).build());
            }
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    private static final String SESSION_KEY_PREFIX = "short-link:session:";
    private static final long SESSION_TTL_MINUTES = 30L;

    private static final String USER_GIDS_KEY = "short-link:user-gids:%s";

    private void expireUserGidsTTL(String username) {
        String setKey = String.format(USER_GIDS_KEY, username);
        try { stringRedisTemplate.expire(setKey, SESSION_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES); } catch (Exception ignore) {}
    }
}
