package com.chaitin.pandawiki.service;

import com.chaitin.pandawiki.dto.CreateReleaseRequest;
import com.chaitin.pandawiki.dto.GetReleaseListRequest;
import com.chaitin.pandawiki.dto.ReleaseListResponse;

/**
 * 发布版本服务接口
 */
public interface ReleaseService {
    
    /**
     * 创建发布版本
     *
     * @param request 发布请求
     * @return 发布版本ID
     */
    String createRelease(CreateReleaseRequest request);
    
    /**
     * 获取发布列表
     *
     * @param request 获取请求
     * @return 发布列表
     */
    ReleaseListResponse getReleaseList(GetReleaseListRequest request);
} 