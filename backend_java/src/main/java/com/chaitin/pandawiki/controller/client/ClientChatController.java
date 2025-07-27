package com.chaitin.pandawiki.controller.client;

import com.chaitin.pandawiki.dto.ChatRequest;
import com.chaitin.pandawiki.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.chaitin.pandawiki.model.SSEEvent;

/**
 * Client聊天接口控制器
 * 用于处理前端应用的聊天请求
 * 
 * @author chaitin
 */
@Tag(name = "Client聊天接口", description = "客户端聊天相关接口")
@RestController
@RequestMapping("/client/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ClientChatController {
    
    private final ChatService chatService;
    
    /**
     * 聊天消息SSE接口
     */
    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatMessage(
            @RequestHeader(value = "X-KB-ID", required = false) String kbId,
            @RequestHeader(value = "x-simple-auth-password", required = false) String password,
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        
        try {
            // 设置CORS头
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            
            log.info("开始处理Client聊天消息 - kbId: {}, message: {}", kbId, request.getMessage());
            
            // 验证参数
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30分钟超时
                try {
                    SSEEvent errorEvent = new SSEEvent();
                    errorEvent.setType("error");
                    errorEvent.setContent("消息内容不能为空");
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(errorEvent));
                    emitter.complete();
                } catch (Exception e) {
                    log.error("发送错误消息失败", e);
                    emitter.completeWithError(e);
                }
                return emitter;
            }
            
            // 设置请求参数
            request.setKbId(kbId);
            request.setRemoteIp(getClientIp(httpRequest));
            
            return chatService.chat(request);
            
        } catch (Exception e) {
            log.error("Client聊天接口异常", e);
            SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
            try {
                SSEEvent errorEvent = new SSEEvent();
                errorEvent.setType("error");
                errorEvent.setContent("系统错误：" + e.getMessage());
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(errorEvent));
                emitter.complete();
            } catch (Exception sendError) {
                log.error("发送错误消息失败", sendError);
                emitter.completeWithError(sendError);
            }
            return emitter;
        }
    }

    /**
     * Widget聊天消息SSE接口
     */
    @PostMapping(value = "/widget", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatWidget(
            @RequestHeader(value = "X-KB-ID", required = false) String kbId,
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        
        try {
            // 设置CORS头
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            
            log.info("开始处理Client Widget聊天消息 - kbId: {}, message: {}", kbId, request.getMessage());
            
            // 验证参数
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
                try {
                    SSEEvent errorEvent = new SSEEvent();
                    errorEvent.setType("error");
                    errorEvent.setContent("消息内容不能为空");
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(errorEvent));
                    emitter.complete();
                } catch (Exception e) {
                    log.error("发送错误消息失败", e);
                    emitter.completeWithError(e);
                }
                return emitter;
            }
            
            // 设置请求参数
            request.setKbId(kbId);
            request.setRemoteIp(getClientIp(httpRequest));
            
            return chatService.chat(request);
            
        } catch (Exception e) {
            log.error("Client Widget聊天接口异常", e);
            SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
            try {
                SSEEvent errorEvent = new SSEEvent();
                errorEvent.setType("error");
                errorEvent.setContent("系统错误：" + e.getMessage());
                emitter.send(SseEmitter.event()
                    .name("message")  
                    .data(errorEvent));
                emitter.complete();
            } catch (Exception sendError) {
                log.error("发送错误消息失败", sendError);
                emitter.completeWithError(sendError);
            }
            return emitter;
        }
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
} 