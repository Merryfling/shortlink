package dev.chanler.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 分组查询响应参数
 * @author: Chanler
 */
@Data
public class GroupLinkCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数
     */
    private Integer linkCount;
}
