package dev.chanler.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页查询回收站请求参数
 * @author: Chanler
 */
@Data
public class RecycleBinLinkPageReqDTO extends Page {

    /**
     * 分组列表
     */
    private List<String> gidList;
}
