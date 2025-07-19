package dev.chanler.shortlink.project.controller;

import dev.chanler.shortlink.project.common.convention.result.Result;
import dev.chanler.shortlink.project.common.convention.result.Results;
import dev.chanler.shortlink.project.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.project.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.project.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
