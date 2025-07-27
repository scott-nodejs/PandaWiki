package com.chaitin.pandawiki.controller.share;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.dto.StatPageRequest;
import com.chaitin.pandawiki.service.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Share统计接口控制器
 * 
 * @author chaitin
 */
@Tag(name = "Share统计接口", description = "分享统计相关接口")
@RestController
@RequestMapping("/share/v1/stat")
@RequiredArgsConstructor
@Slf4j
public class ShareStatController {
    
    private final StatService statService;
    
    /**
     * 记录页面访问
     */
    @Operation(summary = "记录页面访问", description = "记录用户页面访问统计信息")
    @PostMapping("/page")
    public ApiResponse<Void> recordPage(
            @Parameter(description = "知识库ID", required = true)
            @RequestHeader("X-KB-ID") String kbId,
            @Valid @RequestBody StatPageRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("收到页面访问记录请求: kbId={}, nodeId={}, scene={}", kbId, request.getNodeId(), request.getScene());
        
        if (kbId == null || kbId.trim().isEmpty()) {
            return ApiResponse.error("知识库ID不能为空");
        }
        
        try {
            // 获取session ID
            String sessionId = getSessionId(httpRequest);
            if (sessionId == null || sessionId.trim().isEmpty()) {
                log.warn("Session ID未找到，跳过统计");
                return ApiResponse.success();
            }
            
            // 获取客户端信息
            String userAgent = httpRequest.getHeader("User-Agent");
            String referer = httpRequest.getHeader("Referer");
            String clientIp = getClientIp(httpRequest);
            
            // 记录页面访问
            statService.recordPageVisit(kbId, request.getNodeId(), request.getScene(), 
                                      sessionId, clientIp, userAgent, referer);
            
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("记录页面访问失败: kbId={}, nodeId={}, error={}", kbId, request.getNodeId(), e.getMessage(), e);
            return ApiResponse.error("记录页面访问失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取Session ID
     */
    private String getSessionId(HttpServletRequest request) {
        // 先从Cookie中获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("x-pw-session-id".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 如果Cookie中没有，从Header中获取
        return request.getHeader("x-pw-session-id");
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