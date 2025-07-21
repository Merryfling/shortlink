package dev.chanler.shortlink.project.controller;

import dev.chanler.shortlink.project.common.convention.result.Result;
import dev.chanler.shortlink.project.common.convention.result.Results;
import dev.chanler.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import dev.chanler.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
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

    private final RecycleBinService recycleBinService;

    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycledBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        recycleBinService.saveRecycledBin(recycleBinSaveReqDTO);
        return Results.success();
    }
}
