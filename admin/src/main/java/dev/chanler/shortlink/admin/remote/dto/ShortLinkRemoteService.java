package dev.chanler.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.admin.common.convention.result.Result;
import dev.chanler.shortlink.admin.remote.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.LinkPageReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.LinkUpdateReqDTO;
import dev.chanler.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.GroupLinkCountQueryRespDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.admin.remote.dto.resp.LinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
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
     * 修改短链接
     * @param linkUpdateReqDTO 短链接修改请求参数
     */
    default void updateLink(LinkUpdateReqDTO linkUpdateReqDTO) {
        HttpUtil.post("http://localhost:8001/api/short-link/v1/update", JSON.toJSONString(linkUpdateReqDTO));
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

    /**
     * 查询分组内短链接数量
     * @param gidList 分组标识列表
     * @return Result<List<GroupLinkCountQueryRespDTO>
     */
    default Result<List<GroupLinkCountQueryRespDTO>> listGroupLinkCount(List<String> gidList) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", gidList);
        String resultPageStr = HttpUtil.get("http://localhost:8001/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 根据 URL 获取标题
     * @param url URL
     * @return Result<String>
     */
    default Result<String> getTitleByUrl(String url) {
        String resultStr = HttpUtil.get("http://localhost:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    /**
     * 保存回收站数据
     * @param recycleBinSaveReqDTO 回收站保存请求参数
     * @return Result<Void>
     */
    default Result<Void> saveRecycledBin(RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        String resultStr = HttpUtil.post("http://localhost:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(recycleBinSaveReqDTO));
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }
}
