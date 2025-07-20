package dev.chanler.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.remote.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkPageRespDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 * @author: Chanler
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短链接
     * @param linkCreateReqDTO 短链接创建请求参数
     * @return Result<LinkCreateRespDTO>
     */
    default Result<LinkCreateRespDTO> createLink(LinkCreateReqDTO linkCreateReqDTO) {
        String resultBodyStr = HttpUtil.post("http://localhost:8001/api/short-link/v1/create", JSON.toJSONString(linkCreateReqDTO));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 短链接分页查询
     * @param linkPageReqDTO 分页请求参数
     * @return Result<IPage<LinkPageRespDTO>>
     */
    default Result<IPage<LinkPageRespDTO>> pageLink(LinkPageReqDTO linkPageReqDTO) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", linkPageReqDTO.getGid());
        requestMap.put("current", linkPageReqDTO.getCurrent());
        requestMap.put("size", linkPageReqDTO.getSize());
        String resultPageStr = HttpUtil.get("http://localhost:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }
}
