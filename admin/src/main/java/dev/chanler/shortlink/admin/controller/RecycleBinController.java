package dev.chanler.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.remote.dto.ShortLinkRemoteService;
import dev.chanler.shortlink.admin.remote.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 * @author: Chanler
 */
@RequiredArgsConstructor
@RestController
public class RecycleBinController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 保存回收站数据
     * @param recycleBinSaveReqDTO 回收站保存请求参数
     * @return 结果
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycledBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        return shortLinkRemoteService.saveRecycledBin(recycleBinSaveReqDTO);
    }

    /**
     * 回收站分页查询
     * @param linkPageReqDTO 分页请求参数
     * @return Result<IPage<LinkPageRespDTO>>
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(@RequestBody LinkPageReqDTO linkPageReqDTO) {
        return shortLinkRemoteService.pageRecycleBinLink(linkPageReqDTO);
    }
}
