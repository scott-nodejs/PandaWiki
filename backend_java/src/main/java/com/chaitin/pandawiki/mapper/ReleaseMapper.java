package com.chaitin.pandawiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaitin.pandawiki.entity.Release;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发布版本Mapper
 */
@Mapper
public interface ReleaseMapper extends BaseMapper<Release> {
} 