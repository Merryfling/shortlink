package dev.chanler.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.chanler.shortlink.project.dao.entity.LinkDO;
import lombok.Data;

import java.util.List;

/**
 * 分页查询回收站请求参数
 * @author: Chanler
 */
@Data
public class RecycleBinLinkPageReqDTO extends Page<LinkDO> {

    /**
     * 分组列表
     */
    private List<String> gidList;
}
