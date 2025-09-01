package dev.chanler.shortlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.chanler.shortlink.common.biz.user.UserContext;
import dev.chanler.shortlink.common.convention.exception.ClientException;
import dev.chanler.shortlink.dao.entity.TokenDO;
import dev.chanler.shortlink.dao.mapper.TokenMapper;
import dev.chanler.shortlink.dto.req.TokenCreateReqDTO;
import dev.chanler.shortlink.dto.resp.TokenRespDTO;
import dev.chanler.shortlink.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static dev.chanler.shortlink.common.constant.RedisKeyConstant.API_TOKEN_KEY_PREFIX;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl extends ServiceImpl<TokenMapper, TokenDO> implements TokenService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String createToken(TokenCreateReqDTO req) {
        String username = Objects.requireNonNull(UserContext.getUsername(), "用户未登录");
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        TokenDO entity = TokenDO.builder()
                .username(username)
                .token(token)
                .name(StrUtil.blankToDefault(req.getName(), "默认令牌"))
                .enableStatus(0)
                .validDate(req.getValidDate())
                .describe(req.getDescribe())
                .build();
        baseMapper.insert(entity);
        writeRedisMapping(token, username, req.getValidDate());
        return token;
    }

    @Override
    public List<TokenRespDTO> listTokens() {
        String username = Objects.requireNonNull(UserContext.getUsername(), "用户未登录");
        LambdaQueryWrapper<TokenDO> qw = Wrappers.lambdaQuery(TokenDO.class)
                .eq(TokenDO::getUsername, username)
                .eq(TokenDO::getDelFlag, 0)
                .orderByDesc(TokenDO::getUpdateTime);
        List<TokenDO> list = baseMapper.selectList(qw);
        return list.stream().map(each -> TokenRespDTO.builder()
                .name(each.getName())
                .enableStatus(each.getEnableStatus())
                .validDate(each.getValidDate())
                .describe(each.getDescribe())
                .tokenMasked(mask(each.getToken()))
                .build()).toList();
    }

    @Override
    public void deleteToken(Long id) {
        String username = Objects.requireNonNull(UserContext.getUsername(), "用户未登录");
        TokenDO token = baseMapper.selectById(id);
        if (token == null || !Objects.equals(token.getUsername(), username) || token.getDelFlag() != 0) {
            throw new ClientException("令牌不存在");
        }
        // 删除 Redis 映射
        try { stringRedisTemplate.delete(String.format(API_TOKEN_KEY_PREFIX, token.getToken())); } catch (Exception ignore) {}
        // 逻辑删除
        token.setDelFlag(1);
        baseMapper.updateById(token);
    }

    @Override
    public void updateStatus(Long id, Boolean enable) {
        String username = Objects.requireNonNull(UserContext.getUsername(), "用户未登录");
        TokenDO token = baseMapper.selectById(id);
        if (token == null || !Objects.equals(token.getUsername(), username) || token.getDelFlag() != 0) {
            throw new ClientException("令牌不存在");
        }
        if (Boolean.TRUE.equals(enable)) {
            // 启用：写入 Redis，若已过期则报错
            if (token.getValidDate() != null && token.getValidDate().getTime() <= System.currentTimeMillis()) {
                throw new ClientException("令牌已过期");
            }
            token.setEnableStatus(0);
            baseMapper.updateById(token);
            writeRedisMapping(token.getToken(), username, token.getValidDate());
        } else {
            token.setEnableStatus(1);
            baseMapper.updateById(token);
            try { stringRedisTemplate.delete(String.format(API_TOKEN_KEY_PREFIX, token.getToken())); } catch (Exception ignore) {}
        }
    }

    private void writeRedisMapping(String token, String username, java.util.Date validDate) {
        String key = String.format(API_TOKEN_KEY_PREFIX, token);
        if (validDate == null) {
            stringRedisTemplate.opsForValue().set(key, username);
        } else {
            long ttl = validDate.getTime() - System.currentTimeMillis();
            if (ttl <= 0) throw new ClientException("令牌过期时间无效");
            stringRedisTemplate.opsForValue().set(key, username, ttl, TimeUnit.MILLISECONDS);
        }
    }

    private String mask(String token) {
        if (StrUtil.isBlank(token)) return "";
        int n = token.length();
        String tail = token.substring(Math.max(0, n - 4));
        return "****" + tail;
    }
}
