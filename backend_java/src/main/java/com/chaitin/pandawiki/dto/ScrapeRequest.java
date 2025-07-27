package com.chaitin.pandawiki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 爬虫抓取请求
 * 
 * @author chaitin
 */
@Data
public class ScrapeRequest {
    
    /**
     * 待抓取的URL
     */
    @NotBlank(message = "URL不能为空")
    private String url;
    
    /**
     * 知识库ID
     */
    @JsonProperty("kb_id")
    @NotBlank(message = "知识库ID不能为空")
    private String kbId;
} 