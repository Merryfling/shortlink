package dev.chanler.shortlink.service;

import dev.chanler.shortlink.dto.req.TokenCreateReqDTO;
import dev.chanler.shortlink.dto.resp.TokenRespDTO;

import java.util.List;

/**
 * @author: Chanler
 */
public interface TokenService {
    /** 创建 API 访问令牌，返回明文 token（仅创建时可见） */
    String createToken(TokenCreateReqDTO req);

    /** 列出当前用户的所有令牌（脱敏显示 token） */
    List<TokenRespDTO> listTokens();

    /** 吊销（删除）令牌 */
    void deleteToken(Long id);

    /** 启用/禁用令牌 */
    void updateStatus(Long id, Boolean enable);
}
