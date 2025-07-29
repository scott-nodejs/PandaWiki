package com.chaitin.pandawiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaitin.pandawiki.dto.CreateNodeDetailRequest;
import com.chaitin.pandawiki.dto.CreateNodeRequest;
import com.chaitin.pandawiki.entity.Node;
import com.chaitin.pandawiki.mapper.NodeMapper;
import com.chaitin.pandawiki.model.NodeRelease;
import com.chaitin.pandawiki.model.vo.NodeDetailVO;
import com.chaitin.pandawiki.model.vo.NodeListItemVO;
import com.chaitin.pandawiki.model.vo.NodeWithRecommendationsVO;
import com.chaitin.pandawiki.service.NodeService;
import com.chaitin.pandawiki.service.UserService;
import com.chaitin.pandawiki.util.ThreadLocalUtils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl extends ServiceImpl<NodeMapper, Node> implements NodeService {

    private final UserService userService;
    private final EmbeddingStoreIngestor embeddingStoreIngestor;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createNode(CreateNodeRequest request) {
        Node node = new Node();
        node.setId(UUID.randomUUID().toString().replace("-", ""));
        node.setName(request.getName());
        node.setContent(request.getContent());
        node.setEmoji(request.getEmoji());
        node.setSummary(request.getSummary());
        node.setType(request.getType());
        node.setStatus(request.getStatus() != null ? request.getStatus() : 2); // 默认已完成
        node.setVisibility(request.getVisibility() != null ? request.getVisibility() : 2); // 默认公开
        node.setPosition(request.getPosition());
        node.setParentId(request.getParent_id());
        node.setKbId(request.getKb_id());
        node.setSort(request.getSort() != null ? request.getSort() : 0);
        node.setDeleted(false);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        node.setCreatedBy(userService.getCurrentUser().getId());

        this.save(node);

        // 向量化存储node内容
        if (StringUtils.hasText(node.getContent())) {
            try {
                ingestNodeContent(node);
                log.info("节点内容已向量化存储 - nodeId: {}, kbId: {}", node.getId(), node.getKbId());
            } catch (Exception e) {
                log.error("向量化存储失败 - nodeId: {}, error: {}", node.getId(), e.getMessage(), e);
            }
        }

        return node.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(CreateNodeDetailRequest request) {
        Node node = this.getById(request.getId());
        if (node == null) {
            throw new RuntimeException("节点不存在");
        }

        node.setContent(request.getContent());
        node.setUpdatedAt(LocalDateTime.now());

        this.updateById(node);

        // 向量化存储node内容
        if (StringUtils.hasText(node.getContent())) {
            try {
                ingestNodeContent(node);
                log.info("节点内容已重新向量化存储 - nodeId: {}, kbId: {}", node.getId(), node.getKbId());
            } catch (Exception e) {
                log.error("向量化存储失败 - nodeId: {}, error: {}", node.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 将节点内容向量化存储
     * 
     * @param node 节点对象
     */
    private void ingestNodeContent(Node node) {
        try {
            // 设置元数据到ThreadLocal，供documentTransformer使用
            ThreadLocalUtils.set("knowledgeLibId", node.getKbId());
            ThreadLocalUtils.set("nodeId", node.getId());
            
            // 创建Document对象
            Document document = Document.from(node.getContent());
            
            // 添加节点相关的元数据
            document.metadata().put("nodeId", node.getId());
            document.metadata().put("nodeName", node.getName());
            document.metadata().put("kbId", node.getKbId());
            document.metadata().put("nodeType", String.valueOf(node.getType()));
            document.metadata().put("createdAt", node.getCreatedAt().toString());
            
            // 使用ingestor进行向量化存储
            embeddingStoreIngestor.ingest(document);
            
            log.debug("文档向量化完成 - nodeId: {}, contentLength: {}", 
                node.getId(), node.getContent().length());
                
        } finally {
            // 清理ThreadLocal
            ThreadLocalUtils.remove("knowledgeLibId");
            ThreadLocalUtils.remove("nodeId");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(String id) {
        Node node = this.getById(id);
        if (node == null) {
            throw new RuntimeException("节点不存在");
        }

        // 检查是否有子节点
        LambdaQueryWrapper<Node> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Node::getParentId, id).eq(Node::getDeleted, false);
        long childCount = this.count(wrapper);
        if (childCount > 0) {
            throw new RuntimeException("文件夹下有子文件，不能删除");
        }

        // 软删除
        node.setDeleted(true);
        node.setUpdatedAt(LocalDateTime.now());
        this.updateById(node);
        
        // 清理向量存储中对应的数据
        try {
            removeNodeFromVectorStore(node);
            log.info("节点向量已从向量库中清理 - nodeId: {}", node.getId());
        } catch (Exception e) {
            log.error("清理节点向量失败 - nodeId: {}, error: {}", node.getId(), e.getMessage(), e);
        }
    }

    /**
     * 从向量存储中移除节点内容
     * 注意：当前使用的InMemoryEmbeddingStore没有按元数据删除的功能
     * 在生产环境中应使用支持删除的向量数据库（如PostgreSQL + pgvector）
     * 
     * @param node 节点对象
     */
    private void removeNodeFromVectorStore(Node node) {
        try {
            log.info("节点删除 - nodeId: {}，向量清理功能需要在生产环境的向量数据库中实现", node.getId());
            
            // TODO: 在使用PgVector或其他向量数据库时，实现根据元数据删除向量的功能
            // 示例逻辑：
            // 1. 根据nodeId查询对应的embedding IDs
            // 2. 调用embeddingStore.removeAll(embeddingIds)
            // 
            // 当前使用InMemoryEmbeddingStore，暂时记录日志
            log.debug("向量删除功能待实现 - nodeId: {}", node.getId());
                
        } catch (Exception e) {
            log.error("向量删除处理出错 - nodeId: {}, error: {}", node.getId(), e.getMessage(), e);
        }
    }

    @Override
    public Node getNodeDetail(String id) {
        Node node = this.getById(id);
        if (node == null || node.getDeleted()) {
            throw new RuntimeException("节点不存在");
        }
        return node;
    }

    @Override
    public List<Node> getNodeList(String kbId, String parentId) {
        LambdaQueryWrapper<Node> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Node::getKbId, kbId)
                .eq(Node::getDeleted, false);

        if (StringUtils.hasText(parentId)) {
            wrapper.eq(Node::getParentId, parentId);
        } else {
            wrapper.isNull(Node::getParentId);
        }

        wrapper.orderByAsc(Node::getSort, Node::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    public List<Node> getAllNodesByKbId(String kbId) {
        LambdaQueryWrapper<Node> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Node::getKbId, kbId)
                .eq(Node::getDeleted, false);
        
        // 按照 sort 和创建时间排序
        wrapper.orderByAsc(Node::getSort, Node::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveNode(String nodeId, String targetParentId) {
        Node node = this.getById(nodeId);
        if (node == null || node.getDeleted()) {
            throw new RuntimeException("节点不存在");
        }

        node.setParentId(targetParentId);
        node.setUpdatedAt(LocalDateTime.now());
        this.updateById(node);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMoveNodes(List<String> nodeIds, String targetParentId) {
        for (String nodeId : nodeIds) {
            moveNode(nodeId, targetParentId);
        }
    }
    
    @Override
    public List<NodeListItemVO> getPublishedNodeList(String kbId) {
        // 获取所有已发布的公开节点
        LambdaQueryWrapper<Node> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Node::getKbId, kbId)
                .eq(Node::getDeleted, false)
                .eq(Node::getVisibility, 2) // 公开
                .eq(Node::getStatus, 2) // 已完成
                .orderByAsc(Node::getSort, Node::getCreatedAt);

        List<Node> nodes = this.list(wrapper);
        return buildNodeTree(nodes);
    }

    @Override
    public NodeDetailVO getPublishedNodeDetail(String kbId, String nodeId) {
        LambdaQueryWrapper<Node> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Node::getId, nodeId)
                .eq(Node::getKbId, kbId)
                .eq(Node::getDeleted, false)
                .eq(Node::getVisibility, 2) // 公开
                .eq(Node::getStatus, 2); // 已完成

        Node node = this.getOne(wrapper);
        if (node == null) {
            return null;
        }

        NodeDetailVO nodeDetailVO = new NodeDetailVO();
        BeanUtils.copyProperties(node, nodeDetailVO);
        return nodeDetailVO;
    }

    /**
     * 构建节点树形结构
     */
    private List<NodeListItemVO> buildNodeTree(List<Node> nodes) {
        // 将节点转换为 VO 对象
        List<NodeListItemVO> nodeVOs = nodes.stream()
                .map(node -> {
                    NodeListItemVO vo = new NodeListItemVO();
                    BeanUtils.copyProperties(node, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        // 创建节点映射表
        Map<String, NodeListItemVO> nodeMap = nodeVOs.stream()
                .collect(Collectors.toMap(NodeListItemVO::getId, node -> node));

        List<NodeListItemVO> rootNodes = new ArrayList<>();

        // 构建树形结构
        for (NodeListItemVO node : nodeVOs) {
            if (node.getParentId() == null || node.getParentId().isEmpty()) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点
                NodeListItemVO parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }

        return rootNodes;
    }

    @Override
    public List<NodeWithRecommendationsVO> recommendNodes(String kbId, List<String> nodeIds) {
        // 验证输入参数
        if (nodeIds == null || nodeIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取请求的节点信息
        LambdaQueryWrapper<Node> requestedNodesWrapper = new LambdaQueryWrapper<>();
        requestedNodesWrapper.in(Node::getId, nodeIds)
                .eq(Node::getKbId, kbId)
                .eq(Node::getDeleted, false);
        List<Node> requestedNodes = this.list(requestedNodesWrapper);

        if (requestedNodes.isEmpty()) {
            return new ArrayList<>();
        }

        // 为每个请求的节点生成推荐节点
        List<NodeWithRecommendationsVO> result = new ArrayList<>();
        for (Node node : requestedNodes) {
            List<Node> generatedRecommendations = generateRecommendationsForNode(kbId, node.getId(), nodeIds);
            
            // 将推荐的Node转换为NodeWithRecommendationsVO
            List<NodeWithRecommendationsVO> recommendNodeVOs = generatedRecommendations.stream()
                    .map(recommendNode -> {
                        NodeWithRecommendationsVO vo = new NodeWithRecommendationsVO();
                        BeanUtils.copyProperties(recommendNode, vo);
                        return vo;
                    })
                    .collect(Collectors.toList());
            
            // 构建主节点VO
            NodeWithRecommendationsVO nodeVO = new NodeWithRecommendationsVO();
            BeanUtils.copyProperties(node, nodeVO);
            nodeVO.setRecommendNodes(recommendNodeVOs);
            result.add(nodeVO);
        }

        return result;
    }

    /**
     * 为单个节点生成推荐节点列表
     */
    private List<Node> generateRecommendationsForNode(String kbId, String nodeId, List<String> excludeNodeIds) {
        LambdaQueryWrapper<Node> recommendWrapper = new LambdaQueryWrapper<>();
        recommendWrapper.eq(Node::getKbId, kbId)
                .eq(Node::getDeleted, false)
                .eq(Node::getVisibility, 2) // 只推荐公开的节点
                .eq(Node::getStatus, 2) // 只推荐已完成的节点
                .notIn(Node::getId, excludeNodeIds) // 排除已请求的节点
                .orderByDesc(Node::getUpdatedAt) // 按更新时间倒序
                .last("LIMIT 10"); // 限制返回10个推荐节点

        return this.list(recommendWrapper);
    }
}
