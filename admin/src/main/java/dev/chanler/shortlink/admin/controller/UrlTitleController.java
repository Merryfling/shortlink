package dev.chanler.shortlink.admin.controller;

import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.remote.dto.ShortLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL 标题控制层
 * @author: Chanler
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 根据 URL 获取标题
     * @param url URL
     * @return Result<String>
     */
    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return shortLinkRemoteService.getTitleByUrl(url);
    }
}
