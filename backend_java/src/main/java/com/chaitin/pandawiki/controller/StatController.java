package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.dto.stat.*;
import com.chaitin.pandawiki.service.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 统计API控制器
 * 
 * @author chaitin
 */
@Tag(name = "统计接口", description = "统计数据相关接口")
@RestController
@RequestMapping("/api/v1/stat")
@RequiredArgsConstructor
@Slf4j
public class StatController {
    
    private final StatService statService;
    
    /**
     * 获取热门页面统计
     */
    @Operation(summary = "获取热门页面统计", description = "获取知识库的热门页面访问统计")
    @GetMapping("/hot_pages")
    public ApiResponse<List<HotPageResponse>> getHotPages(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取热门页面统计: kbId={}", kbId);
        
        try {
            List<HotPageResponse> hotPages = statService.getHotPages(kbId);
            return ApiResponse.success(hotPages);
        } catch (Exception e) {
            log.error("获取热门页面统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取热门页面统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门来源站点统计
     */
    @Operation(summary = "获取热门来源站点统计", description = "获取知识库的热门来源站点统计")
    @GetMapping("/referer_hosts")
    public ApiResponse<List<RefererHostResponse>> getRefererHosts(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取热门来源站点统计: kbId={}", kbId);
        
        try {
            List<RefererHostResponse> refererHosts = statService.getRefererHosts(kbId);
            return ApiResponse.success(refererHosts);
        } catch (Exception e) {
            log.error("获取热门来源站点统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取热门来源站点统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取浏览器和操作系统统计
     */
    @Operation(summary = "获取浏览器和操作系统统计", description = "获取知识库的浏览器和操作系统访问统计")
    @GetMapping("/browsers")
    public ApiResponse<BrowserStatsResponse> getBrowsers(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取浏览器和操作系统统计: kbId={}", kbId);
        
        try {
            BrowserStatsResponse browserStats = statService.getBrowserStats(kbId);
            return ApiResponse.success(browserStats);
        } catch (Exception e) {
            log.error("获取浏览器和操作系统统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取浏览器和操作系统统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取总计统计
     */
    @Operation(summary = "获取总计统计", description = "获取知识库的访问总计统计")
    @GetMapping("/count")
    public ApiResponse<CountStatsResponse> getCount(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取总计统计: kbId={}", kbId);
        
        try {
            CountStatsResponse count = statService.getCount(kbId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("获取总计统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取总计统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取即时访问统计（最近1小时，每分钟）
     */
    @Operation(summary = "获取即时访问统计", description = "获取知识库最近1小时的分钟级访问统计")
    @GetMapping("/instant_count")
    public ApiResponse<List<InstantCountResponse>> getInstantCount(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取即时访问统计: kbId={}", kbId);
        
        try {
            List<InstantCountResponse> instantCount = statService.getInstantCount(kbId);
            return ApiResponse.success(instantCount);
        } catch (Exception e) {
            log.error("获取即时访问统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取即时访问统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取即时页面访问列表（最新10个页面）
     */
    @Operation(summary = "获取即时页面访问列表", description = "获取知识库最新的页面访问记录")
    @GetMapping("/instant_pages")
    public ApiResponse<List<InstantPageResponse>> getInstantPages(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取即时页面访问列表: kbId={}", kbId);
        
        try {
            List<InstantPageResponse> instantPages = statService.getInstantPages(kbId);
            return ApiResponse.success(instantPages);
        } catch (Exception e) {
            log.error("获取即时页面访问列表失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取即时页面访问列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取地理位置统计（最近24小时）
     */
    @Operation(summary = "获取地理位置统计", description = "获取知识库最近24小时的地理位置访问统计")
    @GetMapping("/geo_count")
    public ApiResponse<Map<String, Integer>> getGeoCount(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取地理位置统计: kbId={}", kbId);
        
        try {
            Map<String, Integer> geoCount = statService.getGeoCount(kbId);
            return ApiResponse.success(geoCount);
        } catch (Exception e) {
            log.error("获取地理位置统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取地理位置统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取对话分布统计（最近24小时）
     */
    @Operation(summary = "获取对话分布统计", description = "获取知识库最近24小时的对话分布统计")
    @GetMapping("/conversation_distribution")
    public ApiResponse<List<ConversationDistributionResponse>> getConversationDistribution(
            @Parameter(description = "知识库ID", required = true)
            @RequestParam("kb_id") @NotBlank String kbId) {
        
        log.info("获取对话分布统计: kbId={}", kbId);
        
        try {
            List<ConversationDistributionResponse> distribution = statService.getConversationDistribution(kbId);
            return ApiResponse.success(distribution);
        } catch (Exception e) {
            log.error("获取对话分布统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return ApiResponse.error("获取对话分布统计失败: " + e.getMessage());
        }
    }
} 