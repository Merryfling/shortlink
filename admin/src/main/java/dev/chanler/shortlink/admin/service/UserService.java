package dev.chanler.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.admin.dao.entity.UserDO;
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
}
