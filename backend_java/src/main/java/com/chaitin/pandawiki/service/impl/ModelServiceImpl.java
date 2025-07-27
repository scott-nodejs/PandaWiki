package com.chaitin.pandawiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaitin.pandawiki.entity.Model;
import com.chaitin.pandawiki.mapper.ModelMapper;
import com.chaitin.pandawiki.service.ModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 模型服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createModel(Model model) {
        model.setId(UUID.randomUUID().toString());
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        save(model);
        return model.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModel(Model model) {
        model.setUpdatedAt(LocalDateTime.now());
        updateById(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(String id) {
        removeById(id);
    }

    @Override
    public List<Model> getModelList(Model.ModelType type) {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Model::getType, type);
        }
        wrapper.orderByDesc(Model::getIsActive)
                .orderByDesc(Model::getUpdatedAt);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setModelActive(String id) {
        // 获取模型信息
        Model model = getById(id);
        if (model == null) {
            throw new IllegalArgumentException("模型不存在");
        }

        // 将同类型的其他模型设置为非活跃
        LambdaUpdateWrapper<Model> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Model::getType, model.getType())
                .set(Model::getIsActive, false);
        update(wrapper);

        // 设置当前模型为活跃
        model.setIsActive(true);
        model.setUpdatedAt(LocalDateTime.now());
        updateById(model);
    }
}
