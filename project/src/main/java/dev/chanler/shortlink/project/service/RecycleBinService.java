package dev.chanler.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import dev.chanler.shortlink.project.dto.req.RecycleBinSaveReqDTO;

/**
 * 回收站接口层
 * @author: Chanler
 */
public interface RecycleBinService extends IService<LinkDO> {
    /**
     * 保存回收站数据
     * @param recycleBinSaveReqDTO 回收站保存请求参数
     */
    void saveRecycledBin(RecycleBinSaveReqDTO recycleBinSaveReqDTO);
}
