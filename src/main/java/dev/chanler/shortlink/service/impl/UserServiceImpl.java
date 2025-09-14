package dev.chanler.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.chanler.shortlink.common.convention.exception.ClientException;
import dev.chanler.shortlink.common.convention.errorcode.BaseErrorCode;
import dev.chanler.shortlink.dao.entity.UserDO;
import dev.chanler.shortlink.dao.entity.GroupDO;
import dev.chanler.shortlink.dao.mapper.UserMapper;
import dev.chanler.shortlink.dao.mapper.GroupMapper;
import dev.chanler.shortlink.dto.req.UserLoginReqDTO;
import dev.chanler.shortlink.dto.req.UserRegisterReqDTO;
import dev.chanler.shortlink.dto.req.UserUpdateReqDTO;
import dev.chanler.shortlink.dto.resp.UserLoginRespDTO;
import dev.chanler.shortlink.dto.resp.UserRespDTO;
import dev.chanler.shortlink.service.GroupService;
import dev.chanler.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dev.chanler.shortlink.common.constant.RedisKeyConstant.LOCK_USER_REGISTER_KEY;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.USER_LOGIN_KEY;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.SESSION_KEY;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.USER_GIDS_KEY;
import static dev.chanler.shortlink.common.convention.errorcode.BaseErrorCode.*;

/**
 * 用户接口实现层
 * @author: Chanler
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;
    private final GroupMapper groupMapper;

    private static final String USER_GIDS_REFRESH_LUA_SCRIPT_PATH = "lua/user_gids_refresh.lua";

    @Override
    public UserRespDTO getByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(BaseErrorCode.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (existsByUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(USER_NAME_EXIST);
        }
        try {
            int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if (inserted < 1) {
                throw new ClientException(USER_SAVE_ERROR);
            }
            groupService.saveGroup(requestParam.getUsername(), "默认分组");
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateByUsername(UserUpdateReqDTO userUpdateReqDTO) {
        // TODO: 需要添加权限校验
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getUsername, userUpdateReqDTO.getUsername());
        baseMapper.update(BeanUtil.toBean(userUpdateReqDTO, UserDO.class),updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, userLoginReqDTO.getUsername())
                .eq(UserDO::getPassword, userLoginReqDTO.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + userLoginReqDTO.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            // 续期旧 token 的会话映射
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            stringRedisTemplate.opsForValue().set(String.format(SESSION_KEY, token), userLoginReqDTO.getUsername(), 30, TimeUnit.MINUTES);
            stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), 30, TimeUnit.MINUTES);
            // 刷新该用户 GID 正向索引集合 TTL（并补齐集合）
            refreshUserGidsIndex(userLoginReqDTO.getUsername());
            return new UserLoginRespDTO(token);
        }
        // 生成新 token，并同时写入会话映射与兼容的用户名 Hash
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(String.format(SESSION_KEY, uuid), userLoginReqDTO.getUsername(), 30, TimeUnit.MINUTES);
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), 30, TimeUnit.MINUTES);
        // 刷新该用户 GID 正向索引集合 TTL（并补齐集合）
        refreshUserGidsIndex(userLoginReqDTO.getUsername());
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        String actualUsername = stringRedisTemplate.opsForValue().get(String.format(SESSION_KEY, token));
        return actualUsername != null && actualUsername.equals(username);
    }

    @Override
    public void logout(String username, String token) {
        String key = String.format(SESSION_KEY, token);
        String actualUsername = stringRedisTemplate.opsForValue().get(key);
        if (actualUsername == null || !actualUsername.equals(username)) {
            throw new ClientException("用户未登录或登录已过期");
        }
        stringRedisTemplate.delete(key);
    }

    /**
     * 刷新用户所有 gid 的反向索引 TTL（并纠偏 value）
     */
    private void refreshUserGidsIndex(String username) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, username)
                .eq(GroupDO::getDelFlag, 0);
        try {
            List<GroupDO> groups = groupMapper.selectList(queryWrapper);
            String setKey = String.format(USER_GIDS_KEY, username);
            if (groups == null || groups.isEmpty()) {
                stringRedisTemplate.expire(setKey, 30, TimeUnit.MINUTES);
                return;
            }
            // 使用 Lua（资源文件）一次性重建集合并设置 TTL（原子性更好）
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setResultType(Long.class);
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource(USER_GIDS_REFRESH_LUA_SCRIPT_PATH)));
            List<String> keys = Collections.singletonList(setKey);
            List<Object> args = new ArrayList<>();
            args.add(String.valueOf(30 * 60)); // TTL 秒，转为字符串
            for (GroupDO groupDO : groups) {
                args.add(groupDO.getGid());
            }
            stringRedisTemplate.execute(script, keys, args.toArray());
        } catch (Throwable t) {
            // 可按需记录日志
            log.error("Refresh user_gids index error, username={}", username, t);
        }
    }
}
