package dev.chanler.shortlink.project.controller;

import dev.chanler.shortlink.project.common.convention.result.Result;
import dev.chanler.shortlink.project.common.convention.result.Results;
import dev.chanler.shortlink.project.dto.req.LinkStatsReqDTO;
import dev.chanler.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import dev.chanler.shortlink.project.service.LinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访问统计控制器
 * @author: Chanler
 */
@RestController
@RequiredArgsConstructor
public class LinkStatsController {

    private final LinkStatsService linkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     * @param linkStatsReqDTO 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(LinkStatsReqDTO linkStatsReqDTO) {
        return Results.success(linkStatsService.oneShortLinkStats(linkStatsReqDTO));
    }
}
