package com.chaitin.pandawiki.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 节点摘要请求
 *
 * @author chaitin
 */
@Data
@Schema(description = "节点摘要请求")
public class NodeSummaryRequest {

    @NotEmpty(message = "节点ID列表不能为空")
    @Schema(description = "节点ID列表")
    private List<String> ids;

    @NotEmpty(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private String kbId;
} 