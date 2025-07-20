package dev.chanler.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.remote.dto.ShortLinkRemoteService;
import dev.chanler.shortlink.admin.remote.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接后管控制层
 * @author: Chanler
 */
@RestController
public class LinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 创建短链接
     * @param linkCreateReqDTO 短链接创建请求参数
     * @return Result
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateReqDTO linkCreateReqDTO) {
        return shortLinkRemoteService.createLink(linkCreateReqDTO);
    }

    /**
     * 短链接分页查询
     * @param linkPageReqDTO 分页请求参数
     * @return Result<IPage<LinkPageRespDTO>>
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(@RequestBody LinkPageReqDTO linkPageReqDTO) {
        return shortLinkRemoteService.pageLink(linkPageReqDTO);
    }
}
