package dev.chanler.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.admin.dao.entity.UserDO;
import dev.chanler.shortlink.admin.dto.req.UserLoginReqDTO;
import dev.chanler.shortlink.admin.dto.req.UserRegisterReqDTO;
import dev.chanler.shortlink.admin.dto.req.UserUpdateReqDTO;
import dev.chanler.shortlink.admin.dto.resp.UserLoginRespDTO;
import dev.chanler.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 * @author: Chanler
 */
public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return UserRespDTO 用户返回实体
     */
    UserRespDTO getByUsername(String username);

    /**
     * 查看用户名是否存在
     * @param username 用户名
     * @return Boolean
     */
    Boolean existsByUsername(String username);

    /**
     * 注册用户
     * @param userRegisterReqDTO
     * @return void
     */
    void register(UserRegisterReqDTO userRegisterReqDTO);

    /**
     * 修改用户信息
     * @param userUpdateReqDTO
     * @return void
     */
    void updateByUsername(UserUpdateReqDTO userUpdateReqDTO);

    /**
     * 用户登录
     * @param userLoginReqDTO
     * @return UserLoginRespDTO
     */
    UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);

    /**
     * 检查用户是否登录
     * @param username
     * @param token
     * @return Boolean
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     * @param username
     * @param token
     * @return
     */
    void logout(String username, String token);
}
