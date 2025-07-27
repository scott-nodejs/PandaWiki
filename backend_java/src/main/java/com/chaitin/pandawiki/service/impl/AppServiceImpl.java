package com.chaitin.pandawiki.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaitin.pandawiki.entity.App;
import com.chaitin.pandawiki.entity.KnowledgeBase;
import com.chaitin.pandawiki.mapper.AppMapper;
import com.chaitin.pandawiki.model.vo.AppInfoVO;
import com.chaitin.pandawiki.model.vo.NodeWithRecommendationsVO;
import com.chaitin.pandawiki.service.AppService;
import com.chaitin.pandawiki.service.KnowledgeBaseService;
import com.chaitin.pandawiki.service.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final KnowledgeBaseService knowledgeBaseService;

    private final NodeService nodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createApp(App app) {
        app.setId(UUID.randomUUID().toString());
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        save(app);
        return app.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApp(App app) {
        app.setUpdatedAt(LocalDateTime.now());
        updateById(app);
    }

    @Override
    public void updateSetting(String id, JSONObject setting) {
        JSONObject settings = setting.getJSONObject("settings");
        String jsonString = JSONObject.toJSONString(settings);
        LambdaUpdateWrapper<App> updateWrapper = Wrappers.<App>lambdaUpdate().eq(App::getId, id)
                .set(App::getSettings, jsonString);
        this.update(updateWrapper);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApp(String id) {
        removeById(id);
    }

    @Override
    public List<App> getAppList(String kbId) {
        LambdaQueryWrapper<App> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(App::getKbId, kbId)
                .orderByDesc(App::getUpdatedAt);
        return list(wrapper);
    }

    @Override
    public List<App> getAppListByType(String kbId, Integer type) {
        LambdaQueryWrapper<App> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(App::getKbId, kbId)
                .eq(App::getType, type)
                .orderByDesc(App::getUpdatedAt);
        return list(wrapper);
    }

    @Override
    public AppInfoVO getWebAppInfo(String kbId) {
        // 获取知识库信息
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("知识库不存在");
        }

        // 查找Web类型的应用
        List<App> webApps = getAppList(kbId);

        Map<Integer, App> appMap = webApps.stream().collect(Collectors.toMap(App::getType, f -> f));
        App app = appMap.get(1);
        return buildAppInfoVO(knowledgeBase, app);
    }

    @Override
    public AppInfoVO getWidgetAppInfo(String kbId) {
        // 获取知识库信息
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("知识库不存在");
        }

        // 查找Widget类型的应用
        List<App> widgetApps = getAppListByType(kbId, 2);
        App widgetApp = widgetApps.isEmpty() ? null : widgetApps.get(0);

        return buildAppInfoVO(knowledgeBase, widgetApp);
    }

    /**
     * 构建应用信息VO
     */
    private AppInfoVO buildAppInfoVO(KnowledgeBase knowledgeBase, App app) {
        AppInfoVO appInfoVO = new AppInfoVO();
        appInfoVO.setKbId(knowledgeBase.getId());
        appInfoVO.setName(knowledgeBase.getName());
        appInfoVO.setDescription(knowledgeBase.getDescription());

        // 构建设置信息
        AppInfoVO.Settings settings = new AppInfoVO.Settings();

        if (app != null && app.getSettings() != null) {
            String appSettings = app.getSettings();
            JSONObject settingJson = JSONObject.parseObject(appSettings);
            if (app.getType() == 1) {
                settings.setTitle(settingJson.getString("title") != null ? settingJson.getString("title") : knowledgeBase.getName());
                settings.setWelcome_str(settingJson.getString("welcome_str") != null ? settingJson.getString("welcome_str") : knowledgeBase.getDescription());
                settings.setIcon(settingJson.getString("icon"));
                settings.setSearch_placeholder(settingJson.getString("search_placeholder"));
                settings.setRecommend_questions(settingJson.getJSONArray("recommend_questions"));
                settings.setTheme_and_style(settingJson.getJSONObject("theme_and_style"));
                settings.setCatalog_settings(settingJson.getJSONObject("catalog_settings"));
                settings.setFooter_settings(settingJson.getJSONObject("footer_settings"));
                settings.setTheme_mode(settingJson.getString("theme_mode"));
                // 添加head_code和body_code处理
                settings.setHeadCode(settingJson.getString("head_code"));
                settings.setBodyCode(settingJson.getString("body_code"));
                JSONArray recommendNodeIds = settingJson.getJSONArray("recommend_node_ids");
                if(!recommendNodeIds.isEmpty()) {
                    List<String> nodes = new ArrayList<>();
                    for (Object recommendNodeId : recommendNodeIds) {
                        nodes.add((String) recommendNodeId);
                    }
                    List<NodeWithRecommendationsVO> nodeWithRecommendationsVOS =
                            nodeService.recommendNodes(app.getKbId(), nodes);
                    appInfoVO.setRecommend_nodes(nodeWithRecommendationsVOS);
                }
                AppInfoVO.WidgetBotSettings widgetSettings = new AppInfoVO.WidgetBotSettings();
                // 如果widget_bot_settings为空，提供默认配置
                widgetSettings.setIs_open(true);
                widgetSettings.setTheme_mode("dark");
                widgetSettings.setBtn_text("在线客服");
                widgetSettings.setBtn_logo("");

                settings.setWidgetBotSettings(widgetSettings);
            }
            if(app.getType() == 2){
                JSONObject widgetBotSettings = settingJson.getJSONObject("widget_bot_settings");
                AppInfoVO.WidgetBotSettings widgetSettings = new AppInfoVO.WidgetBotSettings();

                // 写死默认配置，确保widget能显示
                if (widgetBotSettings == null || widgetBotSettings.isEmpty()) {
                    // 如果widget_bot_settings为空，提供默认配置
                    widgetSettings.setIs_open(true);
                    widgetSettings.setTheme_mode("dark");
                    widgetSettings.setBtn_text("在线客服");
                    widgetSettings.setBtn_logo("");
                } else {
                    // 使用配置的值，但确保is_open有默认值
                    Boolean isOpen = widgetBotSettings.getBoolean("is_open");
                    widgetSettings.setIs_open(isOpen != null ? isOpen : true);
                    widgetSettings.setTheme_mode(widgetBotSettings.getString("theme_mode") != null ?
                                                  widgetBotSettings.getString("theme_mode") : "dark");
                    widgetSettings.setBtn_text(widgetBotSettings.getString("btn_text") != null ?
                                                widgetBotSettings.getString("btn_text") : "在线客服");
                    widgetSettings.setBtn_logo(widgetBotSettings.getString("btn_logo") != null ?
                                                widgetBotSettings.getString("btn_logo") : "");
                }

                settings.setWidgetBotSettings(widgetSettings);
            }
        } else {
            // 默认设置
            settings.setTitle(knowledgeBase.getName());

            if (app != null && app.getType() == 2) {
                AppInfoVO.WidgetBotSettings widgetSettings = new AppInfoVO.WidgetBotSettings();
                widgetSettings.setIs_open(false); // 没有配置则关闭
                settings.setWidgetBotSettings(widgetSettings);
            }
        }

        appInfoVO.setSettings(settings);
        return appInfoVO;
    }
}
