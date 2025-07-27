package com.chaitin.pandawiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaitin.pandawiki.entity.UserAccess;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户访问Mapper
 */
@Mapper
public interface UserAccessMapper extends BaseMapper<UserAccess> {
} 