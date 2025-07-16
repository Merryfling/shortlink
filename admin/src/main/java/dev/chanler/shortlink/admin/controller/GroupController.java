package dev.chanler.shortlink.admin.controller;

import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.common.convention.result.Results;
import dev.chanler.shortlink.admin.dto.req.GroupSaveReqDTO;
import dev.chanler.shortlink.admin.dto.resp.GroupRespDTO;
import dev.chanler.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
     *
     * @param groupSaveReqDTO
     * @return
     */
    @PostMapping("api/short-link/v1/group")
    public Result<Void> create(@RequestBody GroupSaveReqDTO groupSaveReqDTO) {
        groupService.saveGroup(groupSaveReqDTO.getName());
        return Results.success();
    }

    @GetMapping("api/short-link/v1/group")
    public Result<List<GroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }
}
