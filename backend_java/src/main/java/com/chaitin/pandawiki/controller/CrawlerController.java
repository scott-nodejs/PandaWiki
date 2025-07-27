package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.config.CrawlerConfig;
import com.chaitin.pandawiki.dto.ScrapeRequest;
import com.chaitin.pandawiki.dto.ScrapeResponse;
import com.chaitin.pandawiki.dto.crawler.*;
import com.chaitin.pandawiki.service.CrawlerService;
import com.chaitin.pandawiki.service.CrawlerExtendedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫控制器
 * 
 * @author chaitin
 */
@Tag(name = "爬虫管理", description = "爬虫相关接口")
@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {
    
    private final CrawlerService crawlerService;
    private final CrawlerExtendedService crawlerExtendedService;
    private final CrawlerConfig crawlerConfig;
    
    /**
     * 测试配置接口
     */
    @Operation(summary = "测试爬虫配置", description = "检查爬虫服务配置是否正确加载")
    @GetMapping("/config")
    public ApiResponse<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("serviceUrl", crawlerConfig.getServiceUrl());
        config.put("nginxUrl", crawlerConfig.getNginxUrl());
        log.info("当前爬虫配置 - serviceUrl: {}, nginxUrl: {}", 
                crawlerConfig.getServiceUrl(), crawlerConfig.getNginxUrl());
        return ApiResponse.success(config);
    }
    
    /**
     * 抓取URL内容
     */
    @Operation(summary = "抓取URL内容", description = "抓取指定URL的标题和内容")
    @PostMapping("/scrape")
    public ApiResponse<ScrapeResponse> scrape(@Valid @RequestBody ScrapeRequest request) {
        try {
            log.info("收到抓取请求: URL={}, kbId={}", request.getUrl(), request.getKbId());
            log.info("使用爬虫服务地址: {}", crawlerConfig.getServiceUrl());
            
            ScrapeResponse result = crawlerService.scrapeUrl(request);
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("抓取URL失败: {}", e.getMessage(), e);
            return ApiResponse.error("抓取失败: " + e.getMessage());
        }
    }

    // ==== 扩展接口 ====

    @Operation(summary = "解析RSS", description = "解析RSS链接获取文章列表")
    @PostMapping("/parse_rss")
    public ApiResponse<ParseURLResponse> parseRss(@Valid @RequestBody ParseURLRequest request) {
        log.info("解析RSS请求: {}", request.getUrl());
        try {
            ParseURLResponse response = crawlerExtendedService.parseRss(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("解析RSS失败", e);
            return ApiResponse.error("解析RSS失败: " + e.getMessage());
        }
    }

    @Operation(summary = "解析Sitemap", description = "解析Sitemap获取URL列表")
    @PostMapping("/parse_sitemap")
    public ApiResponse<ParseURLResponse> parseSitemap(@Valid @RequestBody ParseURLRequest request) {
        log.info("解析Sitemap请求: {}", request.getUrl());
        try {
            ParseURLResponse response = crawlerExtendedService.parseSitemap(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("解析Sitemap失败", e);
            return ApiResponse.error("解析Sitemap失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取Notion列表", description = "获取Notion页面列表")
    @PostMapping("/notion/get_list")
    public ApiResponse<List<Object>> getNotionList(@Valid @RequestBody NotionRequest.GetListRequest request) {
        log.info("获取Notion列表请求: integration={}", request.getIntegration());
        try {
            List<Object> response = crawlerExtendedService.getNotionList(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("获取Notion列表失败", e);
            return ApiResponse.error("获取Notion列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取Notion文档", description = "获取Notion文档内容")
    @PostMapping("/notion/get_doc")
    public ApiResponse<List<Object>> getNotionDocs(@Valid @RequestBody NotionRequest.GetDocsRequest request) {
        log.info("获取Notion文档请求: integration={}", request.getIntegration());
        try {
            List<Object> response = crawlerExtendedService.getNotionDocs(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("获取Notion文档失败", e);
            return ApiResponse.error("获取Notion文档失败: " + e.getMessage());
        }
    }

    @Operation(summary = "EPUB转换", description = "将EPUB文件转换为文档")
    @PostMapping("/epub/convert")
    public ApiResponse<Object> convertEpub(
            @Parameter(description = "EPUB文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "知识库ID") @RequestParam("kb_id") String kbId) {
        log.info("EPUB转换请求: filename={}, kbId={}", file.getOriginalFilename(), kbId);
        try {
            Object response = crawlerExtendedService.convertEpub(file, kbId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("EPUB转换失败", e);
            return ApiResponse.error("EPUB转换失败: " + e.getMessage());
        }
    }

    @Operation(summary = "分析WikiJS导出文件", description = "分析WikiJS导出的文件")
    @PostMapping("/wikijs/analysis_export_file")
    public ApiResponse<List<Object>> analyzeWikiJSFile(
            @Parameter(description = "WikiJS导出文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "知识库ID") @RequestParam("kb_id") String kbId) {
        log.info("分析WikiJS文件请求: filename={}, kbId={}", file.getOriginalFilename(), kbId);
        try {
            List<Object> response = crawlerExtendedService.analyzeWikiJSFile(file, kbId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("分析WikiJS文件失败", e);
            return ApiResponse.error("分析WikiJS文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "分析Confluence导出文件", description = "分析Confluence导出的文件")
    @PostMapping("/confluence/analysis_export_file")
    public ApiResponse<List<Object>> analyzeConfluenceFile(
            @Parameter(description = "Confluence导出文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "知识库ID") @RequestParam("kb_id") String kbId) {
        log.info("分析Confluence文件请求: filename={}, kbId={}", file.getOriginalFilename(), kbId);
        try {
            List<Object> response = crawlerExtendedService.analyzeConfluenceFile(file, kbId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("分析Confluence文件失败", e);
            return ApiResponse.error("分析Confluence文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取Feishu空间列表", description = "获取Feishu的空间列表")
    @PostMapping("/feishu/list_spaces")
    public ApiResponse<List<FeishuResponse.GetSpaceListResponse>> getFeishuSpaces(
            @Valid @RequestBody FeishuRequest.GetSpaceListRequest request) {
        log.info("获取Feishu空间列表请求: appId={}", request.getAppId());
        try {
            List<FeishuResponse.GetSpaceListResponse> response = crawlerExtendedService.getFeishuSpaces(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("获取Feishu空间列表失败", e);
            return ApiResponse.error("获取Feishu空间列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取Feishu文档列表", description = "获取Feishu的文档列表")
    @PostMapping("/feishu/list_doc")
    public ApiResponse<List<FeishuResponse.SearchDocxResponse>> getFeishuDocs(
            @Valid @RequestBody FeishuRequest.SearchDocxRequest request) {
        log.info("获取Feishu文档列表请求: appId={}", request.getAppId());
        try {
            List<FeishuResponse.SearchDocxResponse> response = crawlerExtendedService.getFeishuDocs(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("获取Feishu文档列表失败", e);
            return ApiResponse.error("获取Feishu文档列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "搜索Feishu Wiki", description = "在Feishu空间中搜索Wiki")
    @PostMapping("/feishu/search_wiki")
    public ApiResponse<List<FeishuResponse.SearchWikiResponse>> searchFeishuWiki(
            @Valid @RequestBody FeishuRequest.SearchWikiRequest request) {
        log.info("搜索Feishu Wiki请求: spaceId={}, query={}", request.getSpaceId(), request.getQuery());
        try {
            List<FeishuResponse.SearchWikiResponse> response = crawlerExtendedService.searchFeishuWiki(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("搜索Feishu Wiki失败", e);
            return ApiResponse.error("搜索Feishu Wiki失败: " + e.getMessage());
        }
    }
} 