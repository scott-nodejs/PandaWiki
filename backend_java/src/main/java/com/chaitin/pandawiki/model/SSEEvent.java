package com.chaitin.pandawiki.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * SSE事件
 * 
 * @author chaitin
 */
@Data
public class SSEEvent {
    
    /**
     * 事件类型
     * conversation_id - 对话ID
     * nonce - 安全令牌  
     * error - 错误信息
     * data - 聊天数据
     * done - 完成标志
     * chunk_result - 文档块结果
     */
    private String type;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 文档块结果
     * 使用@JsonProperty确保JSON序列化时使用下划线命名
     */
    @JsonProperty("chunk_result")
    private ChunkResult chunkResult;
    
    public SSEEvent() {}
    
    public SSEEvent(String type) {
        this.type = type;
    }
    
    public SSEEvent(String type, String content) {
        this.type = type;
        this.content = content;
    }
    
    /**
     * 文档块结果
     */
    @Data
    public static class ChunkResult {
        /**
         * 节点ID
         */
        @JsonProperty("node_id")
        private String nodeId;
        
        /**
         * 节点名称
         */
        private String name;
        
        /**
         * 节点摘要
         */
        private String summary;
    }
} 