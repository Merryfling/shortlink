package dev.chanler.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.project.dto.req.GroupStatsReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkStatsAccessRecordReqDTO;
import dev.chanler.shortlink.project.dto.req.LinkStatsReqDTO;
import dev.chanler.shortlink.project.dto.resp.LinkStatsAccessRecordRespDTO;
import dev.chanler.shortlink.project.dto.resp.LinkStatsRespDTO;

/**
 * 访问统计接口层
 * @author: Chanler
 */
public interface LinkStatsService {

    /**
     * 获取单个短链接监控数据
     * @param linkStatsReqDTO 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    LinkStatsRespDTO oneShortLinkStats(LinkStatsReqDTO linkStatsReqDTO);

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param linkStatsAccessRecordReqDTO 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    IPage<LinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(LinkStatsAccessRecordReqDTO linkStatsAccessRecordReqDTO);

    /**
     * 获取分组短链接监控数据
     * @param groupStatsReqDTO 获取分组短链接监控数据入参
     * @return 短链接监控数据
     */
    LinkStatsRespDTO groupShortLinkStats(GroupStatsReqDTO groupStatsReqDTO);
}
