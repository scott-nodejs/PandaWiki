package com.chaitin.pandawiki.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * SSE事件数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SSEEvent {
    private String type;
    private String content;
    private ChunkResult chunkResult;

    public SSEEvent(String type, String content) {
        this.type = type;
        this.content = content;
    }

    /**
     * 文档块结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkResult {
        private String nodeId;
        private String name;
        private String summary;
    }
} 