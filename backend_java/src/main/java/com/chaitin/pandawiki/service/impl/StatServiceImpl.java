package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.dto.stat.*;
import com.chaitin.pandawiki.mapper.StatMapper;
import com.chaitin.pandawiki.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 统计服务实现类
 * 
 * @author chaitin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    
    private final StatMapper statMapper;

    @Override
    public void recordPageVisit(String kbId, String nodeId, Integer scene, String sessionId,
                               String clientIp, String userAgent, String referer) {
        
        log.info("记录页面访问: kbId={}, nodeId={}, scene={}, sessionId={}, clientIp={}, userAgent={}, referer={}", 
                kbId, nodeId, scene, sessionId, clientIp, userAgent, referer);
        
        // TODO: 这里可以实现实际的统计数据记录逻辑
        // 比如保存到数据库、发送到消息队列等
        // 目前仅记录日志
    }

    @Override
    public List<HotPageResponse> getHotPages(String kbId) {
        log.info("获取热门页面统计: kbId={}", kbId);
        
        try {
            List<HotPageResponse> hotPages = statMapper.getHotPages(kbId);
            
            // 根据场景类型设置页面名称
            for (HotPageResponse page : hotPages) {
                if (page.getNodeName() == null || page.getNodeName().isEmpty()) {
                    switch (page.getScene()) {
                        case 1:
                            page.setNodeName("欢迎页");
                            break;
                        case 3:
                            page.setNodeName("问答页");
                            break;
                        case 4:
                            page.setNodeName("登录页");
                            break;
                        default:
                            page.setNodeName("未知页面");
                            break;
                    }
                }
            }
            
            return hotPages;
        } catch (Exception e) {
            log.error("获取热门页面统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RefererHostResponse> getRefererHosts(String kbId) {
        log.info("获取热门来源站点统计: kbId={}", kbId);
        
        try {
            return statMapper.getRefererHosts(kbId);
        } catch (Exception e) {
            log.error("获取热门来源站点统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public BrowserStatsResponse getBrowserStats(String kbId) {
        log.info("获取浏览器和操作系统统计: kbId={}", kbId);
        
        try {
            List<TrendData> browserStats = statMapper.getBrowserStats(kbId);
            List<TrendData> osStats = statMapper.getOsStats(kbId);
            
            BrowserStatsResponse response = new BrowserStatsResponse();
            response.setBrowser(browserStats);
            response.setOs(osStats);
            
            return response;
        } catch (Exception e) {
            log.error("获取浏览器和操作系统统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            BrowserStatsResponse response = new BrowserStatsResponse();
            response.setBrowser(new ArrayList<>());
            response.setOs(new ArrayList<>());
            return response;
        }
    }

    @Override
    public CountStatsResponse getCount(String kbId) {
        log.info("获取总计统计: kbId={}", kbId);
        
        try {
            CountStatsResponse count = statMapper.getCount(kbId);
            if (count == null) {
                count = new CountStatsResponse();
                count.setIpCount(0L);
                count.setSessionCount(0L);
                count.setPageVisitCount(0L);
                count.setConversationCount(0L);
            }
            
            // 获取对话数量
            Long conversationCount = statMapper.getConversationCount(kbId);
            count.setConversationCount(conversationCount != null ? conversationCount : 0L);
            
            return count;
        } catch (Exception e) {
            log.error("获取总计统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            CountStatsResponse count = new CountStatsResponse();
            count.setIpCount(0L);
            count.setSessionCount(0L);
            count.setPageVisitCount(0L);
            count.setConversationCount(0L);
            return count;
        }
    }

    @Override
    public List<InstantCountResponse> getInstantCount(String kbId) {
        log.info("获取即时访问统计: kbId={}", kbId);
        
        try {
            return statMapper.getInstantCount(kbId);
        } catch (Exception e) {
            log.error("获取即时访问统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<InstantPageResponse> getInstantPages(String kbId) {
        log.info("获取即时页面访问列表: kbId={}", kbId);
        
        try {
            List<InstantPageResponse> pages = statMapper.getInstantPages(kbId);
            
            // 根据场景类型设置页面名称
            for (InstantPageResponse page : pages) {
                if (page.getNodeName() == null || page.getNodeName().isEmpty()) {
                    switch (page.getScene()) {
                        case 1:
                            page.setNodeName("欢迎页");
                            break;
                        case 3:
                            page.setNodeName("问答页");
                            break;
                        case 4:
                            page.setNodeName("登录页");
                            break;
                        default:
                            page.setNodeName("未知页面");
                            break;
                    }
                }
            }
            
            return pages;
        } catch (Exception e) {
            log.error("获取即时页面访问列表失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Integer> getGeoCount(String kbId) {
        log.info("获取地理位置统计: kbId={}", kbId);
        
        try {
            return statMapper.getGeoCount(kbId);
        } catch (Exception e) {
            log.error("获取地理位置统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public List<ConversationDistributionResponse> getConversationDistribution(String kbId) {
        log.info("获取对话分布统计: kbId={}", kbId);
        
        try {
            return statMapper.getConversationDistribution(kbId);
        } catch (Exception e) {
            log.error("获取对话分布统计失败: kbId={}, error={}", kbId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
