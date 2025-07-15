package dev.chanler.shortlink.admin.controller;

import dev.chanler.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接分组控制层
 * @author: Chanler
 */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
}
