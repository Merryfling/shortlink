package dev.chanler.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import dev.chanler.shortlink.project.dto.req.RecycleBinLinkPageReqDTO;
import dev.chanler.shortlink.project.dto.req.RecycleBinRestoreReqDTO;
import dev.chanler.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import dev.chanler.shortlink.project.dto.resp.LinkPageRespDTO;

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

    /**
     * 短链接分页查询
     * @param recycleBinLinkPageReqDTO 分页请求参数
     * @return IPage<LinkPageRespDTO>
     */
    IPage<LinkPageRespDTO> pageRecycledBinLink(RecycleBinLinkPageReqDTO recycleBinLinkPageReqDTO);

    /**
     * 恢复短链接
     * @param recycleBinRestoreReqDTO 恢复请求参数
     */
    void restoreLink(RecycleBinRestoreReqDTO recycleBinRestoreReqDTO);
}
