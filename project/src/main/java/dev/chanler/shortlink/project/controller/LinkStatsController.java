package dev.chanler.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.project.common.convention.result.Result;
import dev.chanler.shortlink.project.common.convention.result.Results;
import dev.chanler.shortlink.project.dto.req.LinkStatsAccessRecordReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkStatsReqDTO;
import dev.chanler.shortlink.project.dto.resp.LinkStatsAccessRecordRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkStatsRespDTO;
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
    public Result<LinkStatsRespDTO> shortLinkStats(LinkStatsReqDTO linkStatsReqDTO) {
        return Results.success(linkStatsService.oneShortLinkStats(linkStatsReqDTO));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param linkStatsAccessRecordReqDTO 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<LinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(LinkStatsAccessRecordReqDTO linkStatsAccessRecordReqDTO) {
        return Results.success(linkStatsService.shortLinkStatsAccessRecord(linkStatsAccessRecordReqDTO));
    }
}
