package dev.chanler.shortlink.dto.req;

import lombok.Data;

/**
 * 短链接分组修改请求参数
 * @author: Chanler
 */
@Data
public class GroupUpdateReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名
     */
    private String name;
}
