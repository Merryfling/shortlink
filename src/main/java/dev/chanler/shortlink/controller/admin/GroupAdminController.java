package dev.chanler.shortlink.controller.admin;

import dev.chanler.shortlink.common.convention.result.Result;
import dev.chanler.shortlink.common.convention.result.Results;
import dev.chanler.shortlink.dto.req.GroupSaveReqDTO;
import dev.chanler.shortlink.dto.req.GroupSortReqDTO;
import dev.chanler.shortlink.dto.req.GroupUpdateReqDTO;
import dev.chanler.shortlink.dto.resp.GroupRespDTO;
import dev.chanler.shortlink.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 * @author: Chanler
 */
@RestController
@RequiredArgsConstructor
public class GroupAdminController {

    private final GroupService groupService;

    /**
     * 创建短链接分组分组
     * @param groupSaveReqDTO
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> create(@RequestBody GroupSaveReqDTO groupSaveReqDTO) {
        groupService.saveGroup(groupSaveReqDTO.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组列表
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<GroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     * @param groupUpdateReqDTO
     * @return
     */
    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> listGroup(@RequestBody GroupUpdateReqDTO groupUpdateReqDTO) {
        groupService.updateGroup(groupUpdateReqDTO);
        return Results.success();
    }

    /**
     * 删除短链接分组名称
     * @param gid
     * @return
     */
    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> listGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 删除短链接分组名称
     * @param groupSortReqDTOs
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<GroupSortReqDTO> groupSortReqDTOs) {
        groupService.sortGroup(groupSortReqDTOs);
        return Results.success();
    }
}
