package com.chaitin.pandawiki.dto;

import lombok.Data;

/**
 * 爬虫抓取响应
 * 
 * @author chaitin
 */
@Data
public class ScrapeResponse {
    
    /**
     * 页面标题
     */
    private String title;
    
    /**
     * 页面内容(Markdown格式)
     */
    private String content;
    
    /**
     * 构造函数
     */
    public ScrapeResponse() {}
    
    /**
     * 构造函数
     * 
     * @param title 标题
     * @param content 内容
     */
    public ScrapeResponse(String title, String content) {
        this.title = title;
        this.content = content;
    }
} 