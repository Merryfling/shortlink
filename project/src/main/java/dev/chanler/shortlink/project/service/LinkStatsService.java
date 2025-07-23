package dev.chanler.shortlink.project.service;

import dev.chanler.shortlink.project.dto.req.LinkStatsReqDTO;
import dev.chanler.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

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
    ShortLinkStatsRespDTO oneShortLinkStats(LinkStatsReqDTO linkStatsReqDTO);
}
