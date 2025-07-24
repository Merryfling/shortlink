package dev.chanler.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import lombok.Data;

/**
 * 短链接分页请求参数
 * 继承了 current、size、orders 等分页参数
 * @author: Chanler
 */
@Data
public class LinkPageReqDTO extends Page<LinkDO> {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序标识
     */
    private String orderTag;
}
