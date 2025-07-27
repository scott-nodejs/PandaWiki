package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.dto.CrawlerServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 模拟爬虫控制器 - 仅用于测试
 * 
 * @author chaitin
 */
@RestController
@RequestMapping("/mock/api/v1")
@Slf4j
public class MockCrawlerController {
    
    /**
     * 模拟爬虫抓取接口
     */
    @PostMapping("/scrape")
    public CrawlerServiceResponse mockScrape(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        String kbId = request.get("kb_id");
        
        log.info("模拟抓取请求: URL={}, kbId={}", url, kbId);
        
        CrawlerServiceResponse response = new CrawlerServiceResponse();
        response.setErr(0);
        response.setMsg("success");
        
        CrawlerServiceResponse.Data data = new CrawlerServiceResponse.Data();
        data.setTitle("模拟页面标题 - " + url);
        data.setMarkdown("# 模拟内容\n\n这是从URL [" + url + "](" + url + ") 抓取的模拟内容。\n\n**知识库ID:** " + kbId);
        
        response.setData(data);
        
        return response;
    }
} 