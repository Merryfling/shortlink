package dev.chanler.shortlink.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 配置类
 * 提供 ObjectMapper 和传输提供者
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ShortLinkMcpServer {

    private static final String MESSAGE_ENDPOINT = "/mcp/message";

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public WebMvcSseServerTransportProvider transportProvider(ObjectMapper objectMapper) {
        log.info("Creating MCP transport provider with endpoint: {}", MESSAGE_ENDPOINT);
        return new WebMvcSseServerTransportProvider(objectMapper, MESSAGE_ENDPOINT);
    }
}