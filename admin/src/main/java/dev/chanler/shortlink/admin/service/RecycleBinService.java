package dev.chanler.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.remote.dto.req.RecycleBinLinkPageReqDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkPageRespDTO;

/**
 * 回收站接口层
 * @author: Chanler
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站
     * @param recycleBinLinkPageReqDTO
     */
    Result<IPage<LinkPageRespDTO>> pageRecycleBinLink(RecycleBinLinkPageReqDTO recycleBinLinkPageReqDTO);
}
