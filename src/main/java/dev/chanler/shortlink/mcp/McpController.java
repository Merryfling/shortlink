package dev.chanler.shortlink.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.chanler.shortlink.common.biz.user.UserContext;
import dev.chanler.shortlink.common.constant.UserConstant;
import dev.chanler.shortlink.dto.req.LinkCreateReqDTO;
import dev.chanler.shortlink.dto.resp.LinkCreateRespDTO;
import dev.chanler.shortlink.service.LinkService;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * MCP 服务器控制器
 * 手动处理 SSE 端点和消息传递
 */
@RestController
@RequestMapping()
@RequiredArgsConstructor
@Slf4j
public class McpController {

    private final LinkService linkService;
    private final ObjectMapper objectMapper;
    private final Map<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();
    private final Map<String, String> connectionToSession = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToConnection = new ConcurrentHashMap<>();

    /**
     * SSE 连接端点
     */
    @GetMapping(value = "/mcp", produces = "text/event-stream")
    public SseEmitter sseConnection() {
        log.info("MCP SSE connection requested");
        // 使用 0L 表示不超时，结合心跳确保连接保持活跃
        SseEmitter emitter = new SseEmitter(0L);
        
        String connectionId = java.util.UUID.randomUUID().toString();
        String sessionId = java.util.UUID.randomUUID().toString();
        
        activeConnections.put(connectionId, emitter);
        connectionToSession.put(connectionId, sessionId);
        sessionToConnection.put(sessionId, connectionId);
        
        // 发送端点信息和会话ID
        try {
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/mcp/message?sessionId=" + sessionId));
            log.info("MCP SSE connection established: {} -> {}", connectionId, sessionId);
        } catch (Exception e) {
            log.error("Failed to establish SSE connection", e);
            cleanupConnection(connectionId);
        }
        
        // 连接关闭时清理
        emitter.onCompletion(() -> {
            cleanupConnection(connectionId);
            log.info("MCP SSE connection closed: {}", connectionId);
        });
        
        emitter.onTimeout(() -> {
            cleanupConnection(connectionId);
            log.info("MCP SSE connection timeout: {}", connectionId);
        });
        
        return emitter;
    }

    private void cleanupConnection(String connectionId) {
        activeConnections.remove(connectionId);
        String sessionId = connectionToSession.remove(connectionId);
        if (sessionId != null) {
            sessionToConnection.remove(sessionId);
        }
    }

    /**
     * MCP 消息处理端点
     */
    @PostMapping("/mcp/message")
    public ResponseEntity<Void> handleMessage(@RequestParam(required = false) String sessionId, @RequestBody String message) {
        log.debug("Received MCP message for session {}: {}", sessionId, message);
        
        try {
            // 解析请求获取消息ID
            Map<String, Object> request = objectMapper.readValue(message, Map.class);
            Object idObj = request.get("id");
            String messageId = idObj == null ? null : String.valueOf(idObj);
            String method = (String) request.get("method");
            
            // 验证会话
            if (sessionId == null || !sessionToConnection.containsKey(sessionId)) {
                log.warn("Invalid or missing session ID: {}", sessionId);
                return ResponseEntity.badRequest().build();
            }
            
            // 通知（无 id）语义：不应返回响应
            if (messageId == null) {
                if ("notifications/initialized".equals(method)) {
                    sessionInitialized.put(sessionId, true);
                    log.info("Session {} received notifications/initialized", sessionId);
                } else {
                    log.debug("Ignoring notification without id. method={}, session={}", method, sessionId);
                }
                return ResponseEntity.accepted().build();
            }
            
            // 根据方法处理消息（有 id 的请求）
            String response;
            switch (method) {
                case "initialize":
                    response = handleInitialize(request);
                    break;
                case "tools/list":
                    response = handleToolsList(request);
                    break;
                case "tools/call":
                    response = handleToolCall(request);
                    break;
                case "ping":
                    response = handlePing(request);
                    break;
                default:
                    response = createErrorResponse(request, -32601, "Method not found: " + method);
                    break;
            }
            
            // 发送响应到特定会话
            sendToSession(sessionId, response);
            log.debug("Sent response for message {} to session {}", messageId, sessionId);
            
        } catch (Exception e) {
            log.error("Error processing MCP message", e);
            if (sessionId != null) {
                sendToSession(sessionId, createErrorResponse(Map.of("id", "unknown"), -32603, "Internal error: " + e.getMessage()));
            }
        }
        
        return ResponseEntity.accepted().build();
    }

    private String handleInitialize(Map<String, Object> request) {
        Object id = request.get("id");
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        String requestedVersion = null;
        if (params != null) {
            Object pv = params.get("protocolVersion");
            if (pv != null) {
                requestedVersion = String.valueOf(pv);
            }
        }

        // 简单版本协商：如果客户端请求的版本受支持，则回同版；否则回当前支持的最新版
        String latestSupported = LATEST_SUPPORTED_PROTOCOL;
        String negotiated = SUPPORTED_PROTOCOLS.contains(requestedVersion) ? requestedVersion : latestSupported;
        Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "result", Map.of(
                        "protocolVersion", negotiated,
                        "capabilities", Map.of(
                                "tools", Map.of("listChanged", true)
                        ),
                        "serverInfo", Map.of(
                                "name", "ShortLink MCP Server",
                                "version", "1.0.0"
                        )
                )
        );
        
        return toJson(response);
    }

    private String handleToolsList(Map<String, Object> request) {
        Object id = request.get("id");
        Map<String, Object> tool = Map.of(
                "name", "createShortLink",
                "title", "Create Short Link",
                "description", "Create a short link from a long URL with 3-day validity period",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "originUrl", Map.of(
                                        "type", "string",
                                        "description", "The original long URL to shorten"
                                ),
                                "describe", Map.of(
                                        "type", "string",
                                        "description", "Optional description for the link"
                                )
                        ),
                        "required", List.of("originUrl")
                )
        );
        
        Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "result", Map.of(
                        "tools", List.of(tool)
                )
        );
        
        return toJson(response);
    }

    private String handleToolCall(Map<String, Object> request) {
        Object id = request.get("id");
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        if (params == null) {
            return createErrorResponse(request, -32602, "Missing params");
        }
        String name = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        if (arguments == null) {
            return createErrorResponse(request, -32602, "Missing arguments");
        }
        
        if (!"createShortLink".equals(name)) {
            return createErrorResponse(request, -32601, "Unknown tool: " + name);
        }
        
        try {
            String originUrl = (String) arguments.get("originUrl");
            String describe = (String) arguments.get("describe");
            
            if (originUrl == null || originUrl.trim().isEmpty()) {
                return createErrorResponse(request, -32602, "originUrl is required");
            }
            
            // 设置 public 用户上下文
            UserContext.setUsername(UserConstant.PUBLIC_USERNAME);
            
            LinkCreateReqDTO linkRequest = new LinkCreateReqDTO();
            linkRequest.setOriginUrl(originUrl);
            linkRequest.setDescribe(describe);
            linkRequest.setCreatedType(0);
            
            // 设置默认有效期为3天
            Date validDate = cn.hutool.core.date.DateUtil.offsetDay(new Date(), 3);
            linkRequest.setValidDate(validDate);
            
            LinkCreateRespDTO linkResponse = linkService.createLink(linkRequest);
            
            String resultText = String.format(
                    "Short link created successfully!\n" +
                            "Original URL: %s\n" +
                            "Short URL: %s\n" +
                            "Description: %s\n" +
                            "Valid until: %s",
                    originUrl,
                    linkResponse.getFullShortUrl(),
                    describe != null ? describe : "No description",
                    cn.hutool.core.date.DateUtil.formatDateTime(validDate)
            );
            
            log.info("MCP: Created short link for URL: {} -> {}", originUrl, linkResponse.getFullShortUrl());
            
            Map<String, Object> response = Map.of(
                    "jsonrpc", "2.0",
                    "id", id,
                    "result", Map.of(
                            "isError", false,
                            "content", List.of(Map.of(
                                    "type", "text",
                                    "text", resultText
                            ))
                    )
            );
            
            return toJson(response);
            
        } catch (Exception e) {
            log.error("MCP: Failed to create short link", e);
            return createErrorResponse(request, -32603, "Failed to create short link: " + e.getMessage());
        } finally {
            // 清理用户上下文
            UserContext.removeUser();
        }
    }

    private String handlePing(Map<String, Object> request) {
        Object id = request.get("id");
        Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "result", Map.of()
        );
        return toJson(response);
    }

    private String createErrorResponse(Map<String, Object> request, int code, String message) {
        Object id = request.get("id");
        Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "error", Map.of(
                        "code", code,
                        "message", message
                )
        );
        return toJson(response);
    }

    private void sendToSession(String sessionId, String message) {
        String connectionId = sessionToConnection.get(sessionId);
        if (connectionId == null) {
            log.warn("Session {} not found, cannot send message", sessionId);
            return;
        }
        
        SseEmitter emitter = activeConnections.get(connectionId);
        if (emitter == null) {
            log.warn("Connection {} for session {} not found", connectionId, sessionId);
            return;
        }
        
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(message));
            log.debug("Sent message to session {}: {}", sessionId, message);
        } catch (Exception e) {
            log.warn("Failed to send message to session {}: {}", sessionId, e.getMessage());
            cleanupConnection(connectionId);
        }
    }

    private void sendToAllConnections(String message) {
        log.debug("Sending message to {} active connections: {}", activeConnections.size(), message);
        
        activeConnections.forEach((connectionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
            } catch (Exception e) {
                log.warn("Failed to send message to connection {}: {}", connectionId, e.getMessage());
                cleanupConnection(connectionId);
            }
        });
    }

    // 维护会话初始化状态
    private final Map<String, Boolean> sessionInitialized = new ConcurrentHashMap<>();

    // 心跳：每 20 秒发送一次 keepalive 事件，防止代理/负载均衡切断连接
    @Scheduled(fixedRate = 20000)
    public void sendKeepalive() {
        if (activeConnections.isEmpty()) {
            return;
        }
        activeConnections.forEach((connectionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("keepalive").data("ping"));
            } catch (Exception e) {
                log.debug("Keepalive failed for connection {}: {}", connectionId, e.getMessage());
                cleanupConnection(connectionId);
            }
        });
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize JSON", e);
            return "{\"error\":\"Serialization failed\"}";
        }
    }

    // 支持的 MCP 协议版本（如需扩展，加入更多版本并将最新版置于 LATEST_SUPPORTED_PROTOCOL）
    private static final List<String> SUPPORTED_PROTOCOLS = List.of(
            "2024-11-05",
            "2025-03-26"
    );
    private static final String LATEST_SUPPORTED_PROTOCOL = SUPPORTED_PROTOCOLS.get(SUPPORTED_PROTOCOLS.size() - 1);
}
