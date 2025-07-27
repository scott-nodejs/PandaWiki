package com.chaitin.pandawiki.service;

import com.chaitin.pandawiki.dto.ScrapeRequest;
import com.chaitin.pandawiki.dto.ScrapeResponse;

/**
 * 爬虫服务接口
 * 
 * @author chaitin
 */
public interface CrawlerService {
    
    /**
     * 抓取URL内容
     * 
     * @param request 抓取请求
     * @return 抓取结果
     */
    ScrapeResponse scrapeUrl(ScrapeRequest request);
} 