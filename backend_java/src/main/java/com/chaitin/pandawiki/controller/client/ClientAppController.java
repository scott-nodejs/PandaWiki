package com.chaitin.pandawiki.controller.client;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.model.vo.AppInfoVO;
import com.chaitin.pandawiki.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Client应用接口控制器
 * 用于处理客户端应用相关请求
 * 
 * @author chaitin
 */
@Tag(name = "Client应用接口", description = "客户端应用相关接口")
@RestController
@RequestMapping("/client/v1/app")
@RequiredArgsConstructor
@Slf4j
public class ClientAppController {
    
    private final AppService appService;
    
    /**
     * 获取Widget应用信息
     * 用于widget-bot.js获取在线客服配置
     */
    @Operation(summary = "获取Widget应用信息", description = "根据知识库ID获取Widget应用配置信息")
    @GetMapping("/widget/info")
    public ApiResponse<AppInfoVO> getWidgetAppInfo(
            @Parameter(description = "知识库ID", required = true)
            @RequestHeader("X-KB-ID") String kbId) {
        
        log.info("Client获取Widget应用信息, kbId: {}", kbId);
        
        if (kbId == null || kbId.trim().isEmpty()) {
            return ApiResponse.error("知识库ID不能为空");
        }
        
        try {
            AppInfoVO appInfo = appService.getWidgetAppInfo(kbId);
            log.info("Widget应用信息获取成功, widgetSettings: {}", 
                    appInfo.getSettings() != null ? appInfo.getSettings().getWidgetBotSettings() : null);
            return ApiResponse.success(appInfo);
        } catch (Exception e) {
            log.error("获取Widget应用信息失败", e);
            return ApiResponse.error("获取应用信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取Web应用信息
     */
    @Operation(summary = "获取Web应用信息", description = "根据知识库ID获取Web应用配置信息")
    @GetMapping("/web/info")
    public ApiResponse<AppInfoVO> getWebAppInfo(
            @Parameter(description = "知识库ID", required = true)
            @RequestHeader("X-KB-ID") String kbId) {
        
        log.info("Client获取Web应用信息, kbId: {}", kbId);
        
        if (kbId == null || kbId.trim().isEmpty()) {
            return ApiResponse.error("知识库ID不能为空");
        }
        
        try {
            AppInfoVO appInfo = appService.getWebAppInfo(kbId);
            return ApiResponse.success(appInfo);
        } catch (Exception e) {
            log.error("获取Web应用信息失败", e);
            return ApiResponse.error("获取应用信息失败: " + e.getMessage());
        }
    }
} 