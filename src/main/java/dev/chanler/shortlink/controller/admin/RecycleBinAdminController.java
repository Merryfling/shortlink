package dev.chanler.shortlink.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.common.convention.result.Result;
import dev.chanler.shortlink.common.convention.result.Results;
import dev.chanler.shortlink.dto.req.RecycleBinLinkPageReqDTO;
import dev.chanler.shortlink.dto.req.RecycleBinRemoveReqDTO;
import dev.chanler.shortlink.dto.req.RecycleBinRestoreReqDTO;
import dev.chanler.shortlink.dto.req.RecycleBinSaveReqDTO;
import dev.chanler.shortlink.dto.resp.LinkPageRespDTO;
import dev.chanler.shortlink.service.RecycleBinService;
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
public class RecycleBinAdminController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站数据
     * @param recycleBinSaveReqDTO 回收站保存请求参数
     * @return 结果
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycledBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        recycleBinService.saveRecycledBin(recycleBinSaveReqDTO);
        return Results.success();
    }

    /**
     * 回收站分页查询
     * @param recycleBinLinkPageReqDTO 分页请求参数
     * @return Result<IPage<LinkPageRespDTO>>
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(RecycleBinLinkPageReqDTO recycleBinLinkPageReqDTO) {
        return Results.success(recycleBinService.pageRecycleBinLink(recycleBinLinkPageReqDTO));
    }

    /**
     * 恢复短链接
     * @param recycleBinRestoreReqDTO 恢复请求参数
     * @return Result<Void>
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/restore")
    public Result<Void> restoreLink(@RequestBody RecycleBinRestoreReqDTO recycleBinRestoreReqDTO) {
        recycleBinService.restoreLink(recycleBinRestoreReqDTO);
        return Results.success();
    }

    /**
     * 从回收站移除短链接
     * @param recycleBinRemoveReqDTO 回收站移除请求参数
     * @return Result<Void>
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> removeLink(@RequestBody RecycleBinRemoveReqDTO recycleBinRemoveReqDTO) {
        recycleBinService.removeLink(recycleBinRemoveReqDTO);
        return Results.success();
    }
}
