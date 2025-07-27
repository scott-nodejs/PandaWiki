package com.chaitin.pandawiki.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Callable;

/**
 * 异步处理配置类
 * 主要用于SSE聊天接口的异步处理，完全独立于Shiro安全框架
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements WebMvcConfigurer {

    /**
     * 创建SSE专用线程池
     * 独立于系统其他线程池，避免Shiro SecurityManager问题
     */
    @Bean("sseTaskExecutor")
    public AsyncTaskExecutor sseTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("sse-chat-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // 设置拒绝策略：调用线程执行
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // 初始化线程池
        executor.initialize();
        
        log.info("SSE专用线程池已初始化: 核心线程数={}, 最大线程数={}, 队列容量={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * 配置异步支持
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置异步请求超时时间（30分钟）
        configurer.setDefaultTimeout(30 * 60 * 1000L);
        
        // 设置SSE专用线程池
        configurer.setTaskExecutor(sseTaskExecutor());
        
        // 注册自定义的CallableProcessingInterceptor
        configurer.registerCallableInterceptors(new SseCallableProcessingInterceptor());
        
        log.info("异步支持配置完成：超时时间=30分钟，使用SSE专用线程池");
    }

    /**
     * SSE专用的Callable处理拦截器
     * 避免在异步处理过程中出现Shiro相关问题
     */
    private static class SseCallableProcessingInterceptor implements CallableProcessingInterceptor {
        
        @Override
        public <T> void beforeConcurrentHandling(org.springframework.web.context.request.NativeWebRequest request, 
                                                Callable<T> task) throws Exception {
            log.debug("SSE异步处理开始：{}", request.getDescription(false));
        }

        @Override
        public <T> void preProcess(org.springframework.web.context.request.NativeWebRequest request, 
                                 Callable<T> task) throws Exception {
            log.debug("SSE异步预处理：{}", request.getDescription(false));
        }

        @Override
        public <T> void postProcess(org.springframework.web.context.request.NativeWebRequest request, 
                                  Callable<T> task, Object concurrentResult) throws Exception {
            log.debug("SSE异步后处理：{}", request.getDescription(false));
        }

        @Override
        public <T> Object handleTimeout(org.springframework.web.context.request.NativeWebRequest request, 
                                      Callable<T> task) throws Exception {
            log.warn("SSE异步处理超时：{}", request.getDescription(false));
            return null;
        }

        @Override
        public <T> Object handleError(org.springframework.web.context.request.NativeWebRequest request, 
                                    Callable<T> task, Throwable ex) throws Exception {
            log.error("SSE异步处理异常：{}, 错误：{}", request.getDescription(false), ex.getMessage());
            return null;
        }

        @Override
        public <T> void afterCompletion(org.springframework.web.context.request.NativeWebRequest request, 
                                      Callable<T> task) throws Exception {
            log.debug("SSE异步处理完成：{}", request.getDescription(false));
        }
    }
} 