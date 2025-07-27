package com.chaitin.pandawiki.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chaitin.pandawiki.entity.App;
import com.chaitin.pandawiki.model.vo.AppInfoVO;

import java.util.List;

/**
 * 应用服务接口
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param app 应用信息
     * @return 应用ID
     */
    String createApp(App app);

    /**
     * 更新应用
     *
     * @param app 应用信息
     */
    void updateApp(App app);


    void updateSetting(String id, JSONObject setting);

    /**
     * 删除应用
     *
     * @param id 应用ID
     */
    void deleteApp(String id);

    /**
     * 获取知识库下的应用列表
     *
     * @param kbId 知识库ID
     * @return 应用列表
     */
    List<App> getAppList(String kbId);

    /**
     * 获取知识库下指定类型的应用
     *
     * @param kbId 知识库ID
     * @param type 应用类型
     * @return 应用列表
     */
    List<App> getAppListByType(String kbId, Integer type);

    /**
     * 获取Web应用信息
     *
     * @param kbId 知识库ID
     * @return Web应用信息
     */
    AppInfoVO getWebAppInfo(String kbId);

    /**
     * 获取Widget应用信息
     *
     * @param kbId 知识库ID
     * @return Widget应用信息
     */
    AppInfoVO getWidgetAppInfo(String kbId);
}
