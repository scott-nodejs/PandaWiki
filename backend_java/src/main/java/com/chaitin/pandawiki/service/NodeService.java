package com.chaitin.pandawiki.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chaitin.pandawiki.dto.CreateNodeDetailRequest;
import com.chaitin.pandawiki.dto.CreateNodeRequest;
import com.chaitin.pandawiki.entity.Node;
import com.chaitin.pandawiki.model.vo.NodeDetailVO;
import com.chaitin.pandawiki.model.vo.NodeListItemVO;
import com.chaitin.pandawiki.model.vo.NodeWithRecommendationsVO;

import java.util.List;

/**
 * 节点服务接口
 */
public interface NodeService extends IService<Node> {

    /**
     * 创建节点
     */
    String createNode(CreateNodeRequest request);

    /**
     * 更新节点
     */
    void updateNode(CreateNodeDetailRequest request);

    /**
     * 删除节点
     */
    void deleteNode(String id);

    /**
     * 获取节点详情
     */
    Node getNodeDetail(String id);

    /**
     * 获取节点列表
     */
    List<Node> getNodeList(String kbId, String parentId);

    /**
     * 获取知识库的所有节点（包括父子关系）
     */
    List<Node> getAllNodesByKbId(String kbId);

    /**
     * 移动节点
     */
    void moveNode(String nodeId, String targetParentId);

    /**
     * 批量移动节点
     */
    void batchMoveNodes(List<String> nodeIds, String targetParentId);
    
    /**
     * 获取已发布的节点列表（用于share接口）
     *
     * @param kbId 知识库ID
     * @return 已发布的节点列表
     */
    List<NodeListItemVO> getPublishedNodeList(String kbId);
    
    /**
     * 获取已发布的节点详情（用于share接口）
     *
     * @param kbId 知识库ID
     * @param nodeId 节点ID
     * @return 已发布的节点详情
     */
    NodeDetailVO getPublishedNodeDetail(String kbId, String nodeId);

    /**
     * 推荐节点
     *
     * @param kbId 知识库ID
     * @param nodeIds 节点ID列表
     * @return 推荐的节点列表
     */
    List<NodeWithRecommendationsVO> recommendNodes(String kbId, List<String> nodeIds);
}
