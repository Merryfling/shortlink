package dev.chanler.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.admin.dao.entity.UserDO;
import dev.chanler.shortlink.admin.dto.req.UserRegisterReqDTO;
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
}
