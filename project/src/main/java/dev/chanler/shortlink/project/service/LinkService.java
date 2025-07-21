package dev.chanler.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import dev.chanler.shortlink.project.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkUpdateReqDTO;
import dev.chanler.shortlink.project.dto.resp.GroupLinkCountQueryRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

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
     * 更新短链接
     * @param linkUpdateReqDTO 短链接更新请求参数
     */
    void updateLink(LinkUpdateReqDTO linkUpdateReqDTO);

    /**
     * 短链接分页查询
     * @param linkPageReqDTO 分页请求参数
     * @return IPage<LinkPageRespDTO>
     */
    IPage<LinkPageRespDTO> pageLink(LinkPageReqDTO linkPageReqDTO);

    /**
     * 查询分组内短链接数量
     * @param gidList 分组标识列表
     * @return List<GroupLinkCountQueryRespDTO>
     */
    List<GroupLinkCountQueryRespDTO> listGroupLinkCount(List<String> gidList);

    /**
     * 根据短链接还原原始链接
     * @param shortUri 短链接后缀
     * @param request HttpServerRequest
     * @param response HttpServerResponse
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);
}
