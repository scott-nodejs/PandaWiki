package com.chaitin.pandawiki.service;

import com.chaitin.pandawiki.dto.node.NodeActionRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryResponse;

/**
 * 扩展的节点服务接口
 *
 * @author chaitin
 */
public interface NodeExtendedService {

    /**
     * 生成节点摘要
     *
     * @param request 摘要请求
     * @return 摘要结果
     */
    NodeSummaryResponse generateSummary(NodeSummaryRequest request);

    /**
     * 执行节点操作
     *
     * @param request 操作请求
     */
    void executeAction(NodeActionRequest request);
} 