package com.chaitin.pandawiki.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chaitin.pandawiki.entity.Model;

import java.util.List;

/**
 * 模型服务接口
 */
public interface ModelService extends IService<Model> {
    
    /**
     * 创建模型
     *
     * @param model 模型信息
     * @return 模型ID
     */
    String createModel(Model model);
    
    /**
     * 更新模型
     *
     * @param model 模型信息
     */
    void updateModel(Model model);
    
    /**
     * 删除模型
     *
     * @param id 模型ID
     */
    void deleteModel(String id);
    
    /**
     * 获取模型列表
     *
     * @param type 模型类型
     * @return 模型列表
     */
    List<Model> getModelList(Model.ModelType type);
    
    /**
     * 设置模型为活跃状态
     *
     * @param id 模型ID
     */
    void setModelActive(String id);
} 