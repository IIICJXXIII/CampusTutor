package com.campus.module.match.service;

import ai.onnxruntime.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;

/**
 * DeepFM 深度学习推理服务
 * 加载 ONNX 模型，接收特征矩阵，输出预估点击率得分
 */
@Slf4j
@Service
public class DeepFMInferenceService {

    /** ONNX 模型文件在 classpath 中的路径 */
    private static final String MODEL_PATH = "campus_deepfm.onnx";

    /** 每条样本的特征维度（8 个特征） */
    private static final int FEATURE_DIM = 8;

    private OrtEnvironment env;
    private OrtSession session;

    /** 模型是否加载成功 */
    private volatile boolean modelReady = false;

    /**
     * 应用启动时加载 ONNX 模型
     */
    @PostConstruct
    public void init() {
        try {
            env = OrtEnvironment.getEnvironment();

            // 从 classpath 读取模型文件
            ClassPathResource resource = new ClassPathResource(MODEL_PATH);
            byte[] modelBytes;
            try (InputStream is = resource.getInputStream()) {
                modelBytes = is.readAllBytes();
            }

            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            // 设置线程数，避免推理时占用过多 CPU
            opts.setIntraOpNumThreads(2);
            session = env.createSession(modelBytes, opts);

            modelReady = true;
            log.info("✅ DeepFM ONNX 模型加载成功，输入: {}, 输出: {}",
                    session.getInputNames(), session.getOutputNames());
        } catch (Exception e) {
            log.error("❌ DeepFM ONNX 模型加载失败，推荐将降级到规则排序: {}", e.getMessage(), e);
            modelReady = false;
        }
    }

    /**
     * 判断模型是否可用
     */
    public boolean isModelReady() {
        return modelReady;
    }

    /**
     * 批量预测得分
     *
     * @param features 特征矩阵，shape = [N, 8]
     *                 每行特征顺序：[user_id, tutor_id, university_name, teach_subjects,
     *                               can_online, expect_price, rating, order_count]
     * @return 每个样本的预估得分数组，长度为 N；推理失败时返回 null
     */
    public float[] predictScores(float[][] features) {
        if (!modelReady || session == null) {
            log.warn("DeepFM 模型不可用，无法执行推理");
            return null;
        }

        int batchSize = features.length;
        if (batchSize == 0) {
            return new float[0];
        }

        try {
            // 1. 将 float[][] 展平为一维数组，构建 OnnxTensor
            float[] flatFeatures = new float[batchSize * FEATURE_DIM];
            for (int i = 0; i < batchSize; i++) {
                System.arraycopy(features[i], 0, flatFeatures, i * FEATURE_DIM, FEATURE_DIM);
            }

            long[] shape = {batchSize, FEATURE_DIM};
            OnnxTensor inputTensor = OnnxTensor.createTensor(
                    env, FloatBuffer.wrap(flatFeatures), shape);

            // 2. 获取模型第一个输入名称
            String inputName = session.getInputNames().iterator().next();

            // 3. 执行推理
            OrtSession.Result result = session.run(
                    Collections.singletonMap(inputName, inputTensor));

            // 4. 解析输出（取第一个输出张量）
            float[] scores = new float[batchSize];
            Object outputValue = result.get(0).getValue();

            if (outputValue instanceof float[][]) {
                // 输出 shape = [N, 1] 的情况
                float[][] output2D = (float[][]) outputValue;
                for (int i = 0; i < batchSize; i++) {
                    scores[i] = output2D[i][0];
                }
            } else if (outputValue instanceof float[]) {
                // 输出 shape = [N] 的情况
                scores = (float[]) outputValue;
            } else {
                log.warn("DeepFM 模型输出格式未知: {}", outputValue.getClass().getName());
                return null;
            }

            // 5. 释放资源
            inputTensor.close();
            result.close();

            log.debug("DeepFM 推理完成，batch_size={}", batchSize);
            return scores;

        } catch (Exception e) {
            log.error("DeepFM 推理异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 应用关闭时释放 ONNX 资源
     */
    @PreDestroy
    public void destroy() {
        try {
            if (session != null) {
                session.close();
                log.info("ONNX Session 已关闭");
            }
        } catch (Exception e) {
            log.warn("关闭 ONNX Session 异常: {}", e.getMessage());
        }
        // OrtEnvironment 是全局单例，通常无需手动关闭
    }
}
