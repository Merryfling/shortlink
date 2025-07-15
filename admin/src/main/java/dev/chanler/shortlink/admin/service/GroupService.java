package dev.chanler.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.admin.dao.entity.GroupDO;

/**
 * 短链接分组接口层
 * @author: Chanler
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     * @return void
     */
    void saveGroup(String groupName);
}
