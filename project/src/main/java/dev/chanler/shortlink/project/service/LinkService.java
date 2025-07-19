package dev.chanler.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import dev.chanler.shortlink.project.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.project.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkPageRespDTO;

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

    /**
     * 短链接分页查询
     * @param linkPageReqDTO 分页请求参数
     * @return IPage<LinkPageRespDTO>
     */
    IPage<LinkPageRespDTO> pageLink(LinkPageReqDTO linkPageReqDTO);
}
