package dev.chanler.shortlink.admin.remote.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 短链接批量创建响应对象
 * @author: Chanler
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkBatchCreateRespDTO {

    /**
     * 成功数量
     */
    private Integer total;

    /**
     * 批量创建返回参数
     */
    private List<LinkBaseInfoRespDTO> baseLinkInfos;
}
