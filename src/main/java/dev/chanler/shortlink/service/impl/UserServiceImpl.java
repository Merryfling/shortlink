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
import dev.chanler.shortlink.common.enums.UserErrorCodeEnum;
import dev.chanler.shortlink.dao.entity.UserDO;
import dev.chanler.shortlink.dao.mapper.UserMapper;
import dev.chanler.shortlink.dto.req.UserLoginReqDTO;
import dev.chanler.shortlink.dto.req.UserRegisterReqDTO;
import dev.chanler.shortlink.dto.req.UserUpdateReqDTO;
import dev.chanler.shortlink.dto.resp.UserLoginRespDTO;
import dev.chanler.shortlink.dto.resp.UserRespDTO;
import dev.chanler.shortlink.service.GroupService;
import dev.chanler.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dev.chanler.shortlink.common.constant.RedisKeyConstant.LOCK_USER_REGISTER_KEY;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.USER_LOGIN_KEY;
import static dev.chanler.shortlink.common.enums.UserErrorCodeEnum.*;

/**
 * 用户接口实现层
 * @author: Chanler
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

    @Override
    public UserRespDTO getByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
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
            stringRedisTemplate.opsForValue().set(SESSION_KEY_PREFIX + token, userLoginReqDTO.getUsername(), 30L, TimeUnit.MINUTES);
            stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), 30L, TimeUnit.MINUTES);
            return new UserLoginRespDTO(token);
        }
        // 生成新 token，并同时写入会话映射与兼容的用户名 Hash
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(SESSION_KEY_PREFIX + uuid, userLoginReqDTO.getUsername(), 30L, TimeUnit.MINUTES);
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        String actualUsername = stringRedisTemplate.opsForValue().get(SESSION_KEY_PREFIX + token);
        return actualUsername != null && actualUsername.equals(username);
    }

    @Override
    public void logout(String username, String token) {
        String key = SESSION_KEY_PREFIX + token;
        String actualUsername = stringRedisTemplate.opsForValue().get(key);
        if (actualUsername == null || !actualUsername.equals(username)) {
            throw new ClientException("用户未登录或登录已过期");
        }
        stringRedisTemplate.delete(key);
    }

    private static final String SESSION_KEY_PREFIX = "short-link:session:";
}
