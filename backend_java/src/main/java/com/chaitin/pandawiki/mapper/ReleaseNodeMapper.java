package com.chaitin.pandawiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaitin.pandawiki.entity.ReleaseNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发布节点关联Mapper
 */
@Mapper
public interface ReleaseNodeMapper extends BaseMapper<ReleaseNode> {
} 