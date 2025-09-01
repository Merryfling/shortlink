package dev.chanler.shortlink.controller.admin;

import dev.chanler.shortlink.common.convention.result.Result;
import dev.chanler.shortlink.common.convention.result.Results;
import dev.chanler.shortlink.dto.req.TokenCreateReqDTO;
import dev.chanler.shortlink.dto.resp.TokenRespDTO;
import dev.chanler.shortlink.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Token 管理控制层（Admin 控制台）
 */
@RestController
@RequiredArgsConstructor
public class TokenAdminController {

    private final TokenService tokenService;

    /** 创建令牌（返回明文 token，仅一次） */
    @PostMapping("/api/short-link/admin/v1/token")
    public Result<String> createToken(@RequestBody TokenCreateReqDTO req) {
        return Results.success(tokenService.createToken(req));
    }

    /** 列表（脱敏展示 token） */
    @GetMapping("/api/short-link/admin/v1/token")
    public Result<List<TokenRespDTO>> listTokens() {
        return Results.success(tokenService.listTokens());
    }

    /** 删除（吊销）令牌 */
    @DeleteMapping("/api/short-link/admin/v1/token/{id}")
    public Result<Void> deleteToken(@PathVariable("id") Long id) {
        tokenService.deleteToken(id);
        return Results.success();
    }

    /** 启用/禁用令牌 */
    @PatchMapping("/api/short-link/admin/v1/token/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("enable") Boolean enable) {
        tokenService.updateStatus(id, enable);
        return Results.success();
    }
}
