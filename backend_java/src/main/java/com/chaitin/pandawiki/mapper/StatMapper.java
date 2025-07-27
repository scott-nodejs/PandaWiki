package com.chaitin.pandawiki.mapper;

import com.chaitin.pandawiki.dto.stat.*;
import com.chaitin.pandawiki.model.stat.NodeAccessStat;
import com.chaitin.pandawiki.model.stat.UserAccessStat;
import com.chaitin.pandawiki.model.stat.ConversationStat;
import com.chaitin.pandawiki.model.stat.ModelUsageStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计查询接口
 */
@Mapper
public interface StatMapper {

    /**
     * 获取节点访问统计数据
     *
     * @param kbId 知识库ID
     * @param nodeId 节点ID
     * @param fromDate 开始日期
     * @param toDate 结束日期
     * @return 节点访问统计列表
     */
    List<NodeAccessStat> getNodeAccessStats(
            @Param("kbId") String kbId,
            @Param("nodeId") String nodeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * 获取用户访问统计数据
     *
     * @param kbId 知识库ID
     * @param fromDate 开始日期
     * @param toDate 结束日期
     * @return 用户访问统计列表
     */
    List<UserAccessStat> getUserAccessStats(
            @Param("kbId") String kbId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * 获取会话统计数据
     *
     * @param kbId 知识库ID
     * @param fromDate 开始日期
     * @param toDate 结束日期
     * @return 会话统计列表
     */
    List<ConversationStat> getConversationStats(
            @Param("kbId") String kbId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * 获取模型使用统计数据
     *
     * @param kbId 知识库ID
     * @param fromDate 开始日期
     * @param toDate 结束日期
     * @return 模型使用统计列表
     */
    List<ModelUsageStat> getModelUsageStats(
            @Param("kbId") String kbId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
    
    /**
     * 获取热门页面统计
     *
     * @param kbId 知识库ID
     * @return 热门页面列表
     */
    List<HotPageResponse> getHotPages(@Param("kbId") String kbId);
    
    /**
     * 获取热门来源站点统计
     *
     * @param kbId 知识库ID
     * @return 来源站点列表
     */
    List<RefererHostResponse> getRefererHosts(@Param("kbId") String kbId);
    
    /**
     * 获取浏览器统计
     *
     * @param kbId 知识库ID
     * @return 浏览器统计列表
     */
    List<TrendData> getBrowserStats(@Param("kbId") String kbId);
    
    /**
     * 获取操作系统统计
     *
     * @param kbId 知识库ID
     * @return 操作系统统计列表
     */
    List<TrendData> getOsStats(@Param("kbId") String kbId);
    
    /**
     * 获取总计统计
     *
     * @param kbId 知识库ID
     * @return 总计统计信息
     */
    CountStatsResponse getCount(@Param("kbId") String kbId);
    
    /**
     * 获取对话数量
     *
     * @param kbId 知识库ID
     * @return 对话数量
     */
    Long getConversationCount(@Param("kbId") String kbId);
    
    /**
     * 获取即时访问统计（最近1小时，每分钟）
     *
     * @param kbId 知识库ID
     * @return 即时访问统计列表
     */
    List<InstantCountResponse> getInstantCount(@Param("kbId") String kbId);
    
    /**
     * 获取即时页面访问列表（最新10个页面）
     *
     * @param kbId 知识库ID
     * @return 即时页面访问列表
     */
    List<InstantPageResponse> getInstantPages(@Param("kbId") String kbId);
    
    /**
     * 获取地理位置统计（最近24小时）
     *
     * @param kbId 知识库ID
     * @return 地理位置统计
     */
    Map<String, Integer> getGeoCount(@Param("kbId") String kbId);
    
    /**
     * 获取对话分布统计（最近24小时）
     *
     * @param kbId 知识库ID
     * @return 对话分布统计列表
     */
    List<ConversationDistributionResponse> getConversationDistribution(@Param("kbId") String kbId);
} 