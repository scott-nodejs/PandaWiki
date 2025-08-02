package com.chaitin.pandawiki.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 节点操作请求
 *
 * @author chaitin
 */
@Data
@Schema(description = "节点操作请求")
public class NodeActionRequest {

    @NotEmpty(message = "节点ID列表不能为空")
    @Schema(description = "节点ID列表")
    private List<String> ids;

    @NotEmpty(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private String kb_id;

    @NotEmpty(message = "操作类型不能为空")
    @Pattern(regexp = "^(delete|private|public)$", message = "操作类型只能是delete、private或public")
    @Schema(description = "操作类型：delete-删除，private-设为私有，public-设为公开")
    private String action;
}
