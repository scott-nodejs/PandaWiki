package com.chaitin.pandawiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaitin.pandawiki.entity.Node;
import org.apache.ibatis.annotations.Mapper;

/**
 * 节点Mapper接口
 */
@Mapper
public interface NodeMapper extends BaseMapper<Node> {
} 