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

@Slf4j
@Service
public class DeepFMInferenceService {

    private static final String MODEL_PATH = "campus_deepfm.onnx";
    private static final int FEATURE_DIM = 8;
    private static final int HASH_MOD = 10000;

    public static final int VOCAB_USER_ID = 1;
    public static final int VOCAB_TUTOR_ID = 6823;
    public static final int VOCAB_UNIVERSITY_NAME = 1;
    public static final int VOCAB_TEACH_SUBJECTS = 4531;
    public static final int VOCAB_CAN_ONLINE = 493;

    private OrtEnvironment env;
    private OrtSession session;
    private volatile boolean modelReady = false;

    @PostConstruct
    public void init() {
        try {
            env = OrtEnvironment.getEnvironment();

            ClassPathResource resource = new ClassPathResource(MODEL_PATH);
            byte[] modelBytes;
            try (InputStream is = resource.getInputStream()) {
                modelBytes = is.readAllBytes();
            }

            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            opts.setIntraOpNumThreads(2);
            opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            session = env.createSession(modelBytes, opts);

            modelReady = true;
            log.info("✅ DeepFM ONNX 模型加载成功，输入: {}, 输出: {}",
                    session.getInputNames(), session.getOutputNames());

            warmUp();
        } catch (Exception e) {
            log.error("❌ DeepFM ONNX 模型加载失败，推荐将降级到规则排序: {}", e.getMessage(), e);
            modelReady = false;
        }
    }

    private void warmUp() {
        try {
            float[][] dummyFeatures = new float[1][FEATURE_DIM];
            float[] result = predictScores(dummyFeatures);
            if (result != null) {
                log.info("✅ DeepFM 模型预热完成，预热输出: {}", result[0]);
            }
        } catch (Exception e) {
            log.warn("DeepFM 模型预热失败(不影响运行): {}", e.getMessage());
        }
    }

    public boolean isModelReady() {
        return modelReady;
    }

    public String getModelPath() {
        return MODEL_PATH;
    }

    public int getFeatureDim() {
        return FEATURE_DIM;
    }

    private static final int[] VOCAB_SIZES = {
            VOCAB_USER_ID, VOCAB_TUTOR_ID, VOCAB_UNIVERSITY_NAME,
            VOCAB_TEACH_SUBJECTS, VOCAB_CAN_ONLINE, 0, 0, 0
    };

    public float[] predictScores(float[][] features) {
        if (!modelReady || session == null) {
            log.warn("DeepFM 模型不可用，无法执行推理");
            return null;
        }

        int batchSize = features.length;
        if (batchSize == 0) {
            return new float[0];
        }

        OnnxTensor inputTensor = null;
        OrtSession.Result result = null;

        try {
            float[] flatFeatures = new float[batchSize * FEATURE_DIM];
            for (int i = 0; i < batchSize; i++) {
                for (int j = 0; j < FEATURE_DIM; j++) {
                    float val = features[i][j];
                    if (j < VOCAB_SIZES.length && VOCAB_SIZES[j] > 1) {
                        int intVal = (int) val;
                        if (intVal >= VOCAB_SIZES[j]) {
                            val = intVal % VOCAB_SIZES[j];
                        }
                    }
                    flatFeatures[i * FEATURE_DIM + j] = val;
                }
            }

            long[] shape = {batchSize, FEATURE_DIM};
            inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(flatFeatures), shape);

            String inputName = session.getInputNames().iterator().next();

            result = session.run(Collections.singletonMap(inputName, inputTensor));

            float[] scores = new float[batchSize];
            Object outputValue = result.get(0).getValue();

            if (outputValue instanceof float[][]) {
                float[][] output2D = (float[][]) outputValue;
                for (int i = 0; i < batchSize; i++) {
                    scores[i] = output2D[i][0];
                }
            } else if (outputValue instanceof float[]) {
                scores = (float[]) outputValue;
            } else {
                log.warn("DeepFM 模型输出格式未知: {}", outputValue.getClass().getName());
                return null;
            }

            log.debug("DeepFM 推理完成，batch_size={}", batchSize);
            return scores;

        } catch (Exception e) {
            log.error("DeepFM 推理异常: {}", e.getMessage(), e);
            return null;
        } finally {
            if (inputTensor != null) {
                try { inputTensor.close(); } catch (Exception ignored) {}
            }
            if (result != null) {
                try { result.close(); } catch (Exception ignored) {}
            }
        }
    }

    public static int hashFeature(Long value) {
        if (value == null) return 0;
        return (value.hashCode() & 0x7FFFFFFF) % HASH_MOD;
    }

    public static int hashFeature(String value) {
        if (value == null) return 0;
        return (value.hashCode() & 0x7FFFFFFF) % HASH_MOD;
    }

    public static int hashFeature(Long value, int vocabSize) {
        if (vocabSize <= 1) return 0;
        if (value == null) return 0;
        return (value.hashCode() & 0x7FFFFFFF) % vocabSize;
    }

    public static int hashFeature(Integer value, int vocabSize) {
        if (vocabSize <= 1) return 0;
        if (value == null) return 0;
        return (value.hashCode() & 0x7FFFFFFF) % vocabSize;
    }

    public static int hashFeature(String value, int vocabSize) {
        if (vocabSize <= 1) return 0;
        if (value == null) return 0;
        return (value.hashCode() & 0x7FFFFFFF) % vocabSize;
    }

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
    }
}
