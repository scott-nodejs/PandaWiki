package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.config.CrawlerConfig;
import com.chaitin.pandawiki.dto.CrawlerServiceResponse;
import com.chaitin.pandawiki.dto.ScrapeRequest;
import com.chaitin.pandawiki.dto.ScrapeResponse;
import com.chaitin.pandawiki.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫服务实现类
 * 
 * @author chaitin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerServiceImpl implements CrawlerService {
    
    private final RestTemplate restTemplate;
    private final CrawlerConfig crawlerConfig;
    
    @Override
    public ScrapeResponse scrapeUrl(ScrapeRequest request) {
        try {
            log.info("开始抓取URL: {}, kbId: {}", request.getUrl(), request.getKbId());
            
            String targetUrl = request.getUrl();
            
            // 处理上传文件的URL，参考Go服务的逻辑
            if (targetUrl.startsWith("/static-file")) {
                targetUrl = crawlerConfig.getNginxUrl() + targetUrl;
                log.info("转换static-file路径: {} -> {}", request.getUrl(), targetUrl);
            }
            
            // 构建请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("url", targetUrl);
            requestBody.put("kb_id", request.getKbId());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            // 构建完整的API URL，添加/scrape路径
            String apiUrl = crawlerConfig.getServiceUrl() + "/scrape";
            log.info("调用爬虫服务: {}", apiUrl);
            
            // 调用爬虫服务
            ResponseEntity<CrawlerServiceResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                CrawlerServiceResponse.class
            );
            
            CrawlerServiceResponse crawlerResponse = response.getBody();
            
            if (crawlerResponse == null) {
                throw new RuntimeException("爬虫服务返回空响应");
            }
            
            if (crawlerResponse.getErr() != 0) {
                throw new RuntimeException("爬虫服务返回错误: " + crawlerResponse.getMsg());
            }
            
            if (crawlerResponse.getData() == null) {
                throw new RuntimeException("爬虫服务返回数据为空");
            }
            
            ScrapeResponse result = new ScrapeResponse();
            result.setTitle(crawlerResponse.getData().getTitle());
            result.setContent(crawlerResponse.getData().getMarkdown());
            
            log.info("成功抓取URL: {}, 标题: {}", request.getUrl(), result.getTitle());
            
            return result;
            
        } catch (Exception e) {
            log.error("抓取URL失败: {}, 错误: {}", request.getUrl(), e.getMessage(), e);
            throw new RuntimeException("抓取URL失败: " + e.getMessage());
        }
    }
} 