package dev.chanler.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.common.convention.result.Results;
import dev.chanler.shortlink.admin.remote.dto.ShortLinkRemoteService;
import dev.chanler.shortlink.admin.remote.dto.req.GroupStatsAccessRecordReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.GroupStatsReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.LinkStatsAccessRecordReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.LinkStatsReqDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkStatsAccessRecordRespDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 链接统计控制器
 * @author: Chanler
 */
@RestController
@RequiredArgsConstructor
public class LinkStatsController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     * @param linkStatsReqDTO 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<LinkStatsRespDTO> shortLinkStats(LinkStatsReqDTO linkStatsReqDTO) {
        return Results.success(shortLinkRemoteService.oneShortLinkStats(linkStatsReqDTO));
    }

    /**
     * 访问分组短链接指定时间内监控数据
     * @param groupStatsReqDTO 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<LinkStatsRespDTO> groupShortLinkStats(GroupStatsReqDTO groupStatsReqDTO) {
        return Results.success(shortLinkRemoteService.groupShortLinkStats(groupStatsReqDTO));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param linkStatsAccessRecordReqDTO 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<LinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(LinkStatsAccessRecordReqDTO linkStatsAccessRecordReqDTO) {
        return Results.success(shortLinkRemoteService.shortLinkStatsAccessRecord(linkStatsAccessRecordReqDTO));
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     * @param groupStatsAccessRecordReqDTO 获取分组短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<IPage<LinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(GroupStatsAccessRecordReqDTO groupStatsAccessRecordReqDTO) {
        return Results.success(shortLinkRemoteService.groupShortLinkStatsAccessRecord(groupStatsAccessRecordReqDTO));
    }
}
