package dev.chanler.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import dev.chanler.shortlink.project.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.project.dto.resp.LinkCreateRespDTO;

/**
 * 短链接接口层
 * @author: Chanler
 */
public interface LinkService extends IService<LinkDO> {

    /**
     * 创建短链接
     * @param linkCreateReqDTO
     * @return
     */
    LinkCreateRespDTO createLink(LinkCreateReqDTO linkCreateReqDTO);
}
