package dev.chanler.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import dev.chanler.shortlink.admin.common.biz.user.UserContext;
import dev.chanler.shortlink.admin.common.convention.exception.ServiceException;
import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.dao.entity.GroupDO;
import dev.chanler.shortlink.admin.dao.mapper.GroupMapper;
import dev.chanler.shortlink.admin.remote.dto.ShortLinkRemoteService;
import dev.chanler.shortlink.admin.remote.dto.req.RecycleBinLinkPageReqDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import dev.chanler.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 回收站接口实现层
 * @author: Chanler
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 分页查询回收站
     * @param recycleBinLinkPageReqDTO
     */
    Result<IPage<LinkPageRespDTO>> pageRecycleBinLink(RecycleBinLinkPageReqDTO recycleBinLinkPageReqDTO) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户没有分组，请先创建分组");
        }
        recycleBinLinkPageReqDTO.setGidList(groupDOList.stream()
                .map(GroupDO::getGid)
                .toList()
        );
        return shortLinkRemoteService.pageRecycleBinLink(recycleBinLinkPageReqDTO);
    }
}
