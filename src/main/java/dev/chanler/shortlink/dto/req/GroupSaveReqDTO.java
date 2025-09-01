package dev.chanler.shortlink.dto.req;

import lombok.Data;

/**
 * 新增短链接分组请求参数
 * @author: Chanler
 */
@Data
public class GroupSaveReqDTO {

    /**
     * 分组名称
     */
    private String name;
}
