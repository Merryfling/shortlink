package dev.chanler.shortlink.admin.controller;

import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.common.convention.result.Results;
import dev.chanler.shortlink.admin.dto.req.GroupSaveReqDTO;
import dev.chanler.shortlink.admin.dto.req.GroupUpdateReqDTO;
import dev.chanler.shortlink.admin.dto.resp.GroupRespDTO;
import dev.chanler.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 * @author: Chanler
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 创建短链接分组分组
     * @param groupSaveReqDTO
     * @return
     */
    @PostMapping("api/short-link/v1/group")
    public Result<Void> create(@RequestBody GroupSaveReqDTO groupSaveReqDTO) {
        groupService.saveGroup(groupSaveReqDTO.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组列表
     * @return
     */
    @GetMapping("api/short-link/v1/group")
    public Result<List<GroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     * @param groupUpdateReqDTO
     * @return
     */
    @PutMapping("api/short-link/v1/group")
    public Result<Void> listGroup(@RequestBody GroupUpdateReqDTO groupUpdateReqDTO) {
        groupService.updateGroup(groupUpdateReqDTO);
        return Results.success();
    }

    /**
     * 删除短链接分组名称
     * @param gid
     * @return
     */
    @DeleteMapping("api/short-link/v1/group")
    public Result<Void> listGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }
}
