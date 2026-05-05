package com.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步与线程池配置
 * <p>
 * 为匹配评分、协同过滤计算、行为记录三类场景提供独立线程池，
 * 避免慢任务互相影响，也避免耗尽 Tomcat 的 servlet 线程。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 匹配评分线程池 —— 用于 {@code MatchService} 中候选人并行评分
     * <p>
     * 任务特点：CPU + Redis IO 混合，单个任务毫秒级，并发量取决于搜索分页大小。
     * 核心线程常驻避免频繁创建，队列用于削峰。
     */
    @Bean("matchScoringExecutor")
    public ThreadPoolTaskExecutor matchScoringExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix("match-score-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 协同过滤计算线程池 —— 用于相似用户检索和评分预测
     * <p>
     * 任务特点：DB + CPU 混合，单个任务 10-100ms，并发度与候选用户数相关。
     */
    @Bean("cfComputeExecutor")
    public ThreadPoolTaskExecutor cfComputeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(128);
        executor.setThreadNamePrefix("cf-compute-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        executor.initialize();
        return executor;
    }

    /**
     * 行为记录线程池 —— 用于异步持久化用户行为
     * <p>
     * 任务特点：纯 IO（DB 写入 + Redis 清理 + Stream 发布），允许丢弃。
     * 使用 DiscardPolicy：队列满时直接丢弃，不阻塞用户请求。
     */
    @Bean("behaviorAsyncExecutor")
    public ThreadPoolTaskExecutor behaviorAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("behavior-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
