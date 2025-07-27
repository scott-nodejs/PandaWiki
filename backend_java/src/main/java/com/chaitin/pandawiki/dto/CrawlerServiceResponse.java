package com.chaitin.pandawiki.dto;

import lombok.Data;

/**
 * 爬虫服务内部响应
 * 
 * @author chaitin
 */
@Data
public class CrawlerServiceResponse {
    
    /**
     * 错误码
     */
    private int err;
    
    /**
     * 错误消息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private Data data;
    
    /**
     * 数据内容
     */
    @lombok.Data
    public static class Data {
        /**
         * 页面标题
         */
        private String title;
        
        /**
         * Markdown内容
         */
        private String markdown;
    }
} 