package dev.chanler.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 新增短链接分组请求参数
 * @author: Chanler
 * @date: 2025/7/15 - 20:24
 */
@Data
public class GroupSaveReqDTO {

    /**
     * 分组名称
     */
    private String name;
}
