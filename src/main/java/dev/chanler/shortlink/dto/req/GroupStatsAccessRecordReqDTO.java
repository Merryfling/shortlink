package dev.chanler.shortlink.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.chanler.shortlink.dao.entity.LinkAccessLogsDO;
import lombok.Data;

/**
 * 分组短链接监控访问记录请求参数
 * @author: Chanler
 */
@Data
public class GroupStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
