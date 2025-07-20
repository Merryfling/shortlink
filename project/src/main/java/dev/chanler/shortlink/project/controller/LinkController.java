package dev.chanler.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.project.common.convention.result.Result;
import dev.chanler.shortlink.project.common.convention.result.Results;
import dev.chanler.shortlink.project.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkUpdateReqDTO;
import dev.chanler.shortlink.project.dto.resp.GroupLinkCountQueryRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkPageRespDTO;
import dev.chanler.shortlink.project.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 * @author: Chanler
 */
@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    /**
     * 创建短链接
     *
     * @return Result
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateReqDTO linkCreateReqDTO) {
        return Results.success(linkService.createLink(linkCreateReqDTO));
    }

    @PutMapping("/api/short-link/v1/update")
    public Result<Void> updateLink(@RequestBody LinkUpdateReqDTO linkUpdateReqDTO) {
        linkService.updateLink(linkUpdateReqDTO);
        return Results.success();
    }

    /**
     * 短链接分页查询
     * @param linkPageReqDTO 分页请求参数
     * @return Result<IPage<LinkPageRespDTO>>
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(LinkPageReqDTO linkPageReqDTO) {
        return Results.success(linkService.pageLink(linkPageReqDTO));
    }

    /**
     * 查询分组内短链接数量
     * @return Result<List<GroupLinkCountQueryRespDTO>>
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<GroupLinkCountQueryRespDTO>> listGroupLinkCount(@RequestParam("requestParam") List<String> gidList) {
        return Results.success(linkService.listGroupLinkCount(gidList));

    }
}
