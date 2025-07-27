

export const PageStatus = {
  1: {
    label: '正在处理',
    color: '#3248F2',
    bgcolor: '#EBEFFE',
  },
  2: {
    label: '已学习',
    color: '#82DDAF',
    bgcolor: '#F2FBF7',
  },
  3: {
    label: '处理失败',
    color: '#FE4545',
    bgcolor: '#FEECEC',
  },
}

export const PluginType = {
  1: '内置工具',
  2: '自定义工具',
}

export const IconMap = {
  'gpt-4o': 'icon-chatgpt',
  'deepseek-r1': 'icon-deepseek',
  'deepseek-v3-0324': 'icon-deepseek'
}

export const AppType = {
  1: {
    label: 'Wiki 站点',
  },
  2: {
    label: '网页挂件',
  },
  3: {
    label: '钉钉机器人',
  },
  4: {
    label: '飞书机器人',
  },
  5: {
    label: '企业微信机器人',
  },
  6: {
    label: '企业微信客服',
  },
  7: {
    label: 'Discord 机器人'
  }
}

export const AnswerStatus = {
  1: '正在为您查找结果',
  2: '正在思考',
  3: '正在回答',
  4: '',
  5: '等待工具确认运行',
}

export const PageType = {
  1: '在线网页',
  2: '离线文件',
  3: '自定义文档',
}

export const VersionMap = {
  free: {
    label: '免费版',
    offlineFileSize: 5
  },
  contributor: {
    label: '社区贡献者版',
    offlineFileSize: 10
  },
  pro: {
    label: '专业版',
    offlineFileSize: 20
  },
  business: {
    label: '商业版',
    offlineFileSize: 20
  },
  enterprise: {
    label: '旗舰版',
    offlineFileSize: 20
  },
}

export const ModelProvider = {
  // 驼峰格式（原有）
  BaiZhiCloud: {
    label: 'BaiZhiCloud',
    cn: '百智云',
    icon: 'icon-baizhiyunlogo',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://model-square.app.baizhi.cloud/token',
    defaultBaseUrl: 'https://model-square.app.baizhi.cloud/v1',
  },
  DeepSeek: {
    label: 'DeepSeek',
    cn: '',
    icon: 'icon-deepseek',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://platform.deepseek.com/api_keys',
    defaultBaseUrl: 'https://api.deepseek.com/v1',
  },
  Hunyuan: {
    label: 'Hunyuan',
    cn: '腾讯混元',
    icon: 'icon-tengxunhunyuan',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://console.cloud.tencent.com/hunyuan/start',
    defaultBaseUrl: 'https://api.hunyuan.cloud.tencent.com/v1',
  },
  BaiLian: {
    label: 'BaiLian',
    cn: '阿里云百炼',
    icon: 'icon-aliyunbailian',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://bailian.console.aliyun.com/?tab=model#/api-key',
    defaultBaseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
  },
  Volcengine: {
    label: 'Volcengine',
    cn: '火山引擎',
    icon: 'icon-huoshanyinqing',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey',
    defaultBaseUrl: 'https://ark.cn-beijing.volces.com/api/v3',
  },
  OpenAI: {
    label: 'OpenAI',
    cn: '',
    icon: 'icon-chatgpt',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://platform.openai.com/api-keys',
    defaultBaseUrl: 'https://api.openai.com/v1',
  },
  Ollama: {
    label: 'Ollama',
    cn: '',
    icon: 'icon-ollama',
    urlWrite: true,
    secretRequired: false,
    customHeader: true,
    modelDocumentUrl: '',
    defaultBaseUrl: 'http://127.0.0.1:11434',
  },
  SiliconFlow: {
    label: 'SiliconFlow',
    cn: '硅基流动',
    icon: 'icon-a-ziyuan2',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://cloud.siliconflow.cn/account/ak',
    defaultBaseUrl: 'https://api.siliconflow.cn/v1',
  },
  Moonshot: {
    label: 'Moonshot',
    cn: '月之暗面',
    icon: 'icon-Kim',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://platform.moonshot.cn/console/api-keys',
    defaultBaseUrl: 'https://api.moonshot.cn/v1',
  },
  AzureOpenAI: {
    label: 'AzureOpenAI',
    cn: 'Azure OpenAI',
    icon: 'icon-azure',
    urlWrite: true,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://portal.azure.com/#view/Microsoft_Azure_ProjectOxford/CognitiveServicesHub/~/OpenAI',
    defaultBaseUrl: 'https://<resource_name>.openai.azure.com',
  },
  Other: {
    label: 'Other',
    cn: '其他',
    icon: 'icon-a-AIshezhi',
    urlWrite: true,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: '',
    defaultBaseUrl: '',
  },
  
  // 大写下划线格式（后端返回格式）- 映射到相同的配置
  BAIZHI_CLOUD: {
    label: 'BaiZhiCloud',
    cn: '百智云',
    icon: 'icon-baizhiyunlogo',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://model-square.app.baizhi.cloud/token',
    defaultBaseUrl: 'https://model-square.app.baizhi.cloud/v1',
  },
  DEEPSEEK: {
    label: 'DeepSeek',
    cn: '',
    icon: 'icon-deepseek',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://platform.deepseek.com/api_keys',
    defaultBaseUrl: 'https://api.deepseek.com/v1',
  },
  HUNYUAN: {
    label: 'Hunyuan',
    cn: '腾讯混元',
    icon: 'icon-tengxunhunyuan',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://console.cloud.tencent.com/hunyuan/start',
    defaultBaseUrl: 'https://api.hunyuan.cloud.tencent.com/v1',
  },
  BAILIAN: {
    label: 'BaiLian',
    cn: '阿里云百炼',
    icon: 'icon-aliyunbailian',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://bailian.console.aliyun.com/?tab=model#/api-key',
    defaultBaseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
  },
  VOLCENGINE: {
    label: 'Volcengine',
    cn: '火山引擎',
    icon: 'icon-huoshanyinqing',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey',
    defaultBaseUrl: 'https://ark.cn-beijing.volces.com/api/v3',
  },
  OPENAI: {
    label: 'OpenAI',
    cn: '',
    icon: 'icon-chatgpt',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://platform.openai.com/api-keys',
    defaultBaseUrl: 'https://api.openai.com/v1',
  },
  OLLAMA: {
    label: 'Ollama',
    cn: '',
    icon: 'icon-ollama',
    urlWrite: true,
    secretRequired: false,
    customHeader: true,
    modelDocumentUrl: '',
    defaultBaseUrl: 'http://127.0.0.1:11434',
  },
  SILICON_FLOW: {
    label: 'SiliconFlow',
    cn: '硅基流动',
    icon: 'icon-a-ziyuan2',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://cloud.siliconflow.cn/account/ak',
    defaultBaseUrl: 'https://api.siliconflow.cn/v1',
  },
  MOONSHOT: {
    label: 'Moonshot',
    cn: '月之暗面',
    icon: 'icon-Kim',
    urlWrite: false,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://platform.moonshot.cn/console/api-keys',
    defaultBaseUrl: 'https://api.moonshot.cn/v1',
  },
  AZURE_OPENAI: {
    label: 'AzureOpenAI',
    cn: 'Azure OpenAI',
    icon: 'icon-azure',
    urlWrite: true,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: 'https://portal.azure.com/#view/Microsoft_Azure_ProjectOxford/CognitiveServicesHub/~/OpenAI',
    defaultBaseUrl: 'https://<resource_name>.openai.azure.com',
  },
  OTHER: {
    label: 'Other',
    cn: '其他',
    icon: 'icon-a-AIshezhi',
    urlWrite: true,
    secretRequired: true,
    customHeader: false,
    modelDocumentUrl: '',
    defaultBaseUrl: '',
  }
}

export const MAC_SYMBOLS = {
  ctrl: "⌘",
  alt: "⌥",
  shift: "⇧",
}

export const chartColor = ['#3082FF', '#FFD268', '#9E68FC', '#3248F2', '#63CFC3', '#FF5576']