package com.chaitin.pandawiki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布列表响应
 */
@Data
public class ReleaseListResponse {
    
    /**
     * 总数
     */
    private Long total;
    
    /**
     * 发布记录列表
     */
    private List<ReleaseItem> data;
    
    /**
     * 发布记录项
     */
    @Data
    public static class ReleaseItem {
        private String id;
        
        @JsonProperty("kb_id")
        private String kbId;
        
        private String tag;
        private String message;
        
        @JsonProperty("created_at")
        private LocalDateTime publishTime;
        
        private Integer status;
        
        @JsonProperty("node_ids")
        private List<String> nodeIds;
    }
} 