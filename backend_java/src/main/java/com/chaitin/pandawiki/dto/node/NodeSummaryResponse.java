package com.chaitin.pandawiki.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 节点摘要响应
 *
 * @author chaitin
 */
@Data
@Schema(description = "节点摘要响应")
public class NodeSummaryResponse {

    @Schema(description = "生成的摘要内容")
    private String summary;
} 