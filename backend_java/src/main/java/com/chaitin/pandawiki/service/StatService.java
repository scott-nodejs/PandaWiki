package com.chaitin.pandawiki.service;

import com.chaitin.pandawiki.dto.stat.*;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 * 
 * @author chaitin
 */
public interface StatService {
    
    /**
     * 记录页面访问
     * 
     * @param kbId 知识库ID
     * @param nodeId 节点ID
     * @param scene 场景类型
     * @param sessionId 会话ID
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @param referer 来源页面
     */
    void recordPageVisit(String kbId, String nodeId, Integer scene, String sessionId, 
                        String clientIp, String userAgent, String referer);
    
    /**
     * 获取热门页面统计
     * 
     * @param kbId 知识库ID
     * @return 热门页面列表
     */
    List<HotPageResponse> getHotPages(String kbId);
    
    /**
     * 获取热门来源站点统计
     * 
     * @param kbId 知识库ID
     * @return 来源站点列表
     */
    List<RefererHostResponse> getRefererHosts(String kbId);
    
    /**
     * 获取浏览器和操作系统统计
     * 
     * @param kbId 知识库ID
     * @return 浏览器统计信息
     */
    BrowserStatsResponse getBrowserStats(String kbId);
    
    /**
     * 获取总计统计
     * 
     * @param kbId 知识库ID
     * @return 总计统计信息
     */
    CountStatsResponse getCount(String kbId);
    
    /**
     * 获取即时访问统计（最近1小时，每分钟）
     * 
     * @param kbId 知识库ID
     * @return 即时访问统计列表
     */
    List<InstantCountResponse> getInstantCount(String kbId);
    
    /**
     * 获取即时页面访问列表（最新10个页面）
     * 
     * @param kbId 知识库ID
     * @return 即时页面访问列表
     */
    List<InstantPageResponse> getInstantPages(String kbId);
    
    /**
     * 获取地理位置统计（最近24小时）
     * 
     * @param kbId 知识库ID
     * @return 地理位置统计
     */
    Map<String, Integer> getGeoCount(String kbId);
    
    /**
     * 获取对话分布统计（最近24小时）
     * 
     * @param kbId 知识库ID
     * @return 对话分布统计列表
     */
    List<ConversationDistributionResponse> getConversationDistribution(String kbId);
} 