package dev.chanler.shortlink.dto.req;

import lombok.Data;

/**
 * 回收站保存请求参数
 * @author: Chanler
 */
@Data
public class RecycleBinSaveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接
     */
    private String fullShortUrl;
}
