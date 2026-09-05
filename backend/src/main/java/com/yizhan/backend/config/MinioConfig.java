package com.yizhan.backend.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * MinIO 客户端配置：应用启动完成后自动创建 bucket，带重试。
 * （旧版在 @PostConstruct 里调用 minioClient() 造成循环引用，已重写）
 */
@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.bucket-name}")
    private String bucketName;

    private final MinioClient client;

    public MinioConfig(@Value("${minio.endpoint}") String endpoint,
                       @Value("${minio.access-key}") String accessKey,
                       @Value("${minio.secret-key}") String secretKey) {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public MinioClient minioClient() {
        return client;
    }

    /**
     * 应用完全就绪后创建 bucket；MinIO 容器可能比后端晚几秒就绪，故重试 5 次。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initBucket() {
        new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                    if (!exists) {
                        client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                        log.info("MinIO bucket 创建成功: {}", bucketName);
                    } else {
                        log.info("MinIO bucket 已存在: {}", bucketName);
                    }
                    return;
                } catch (Exception e) {
                    log.warn("MinIO bucket 初始化第 {}/5 次失败: {}", i, e.getMessage());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            log.error("MinIO bucket 初始化最终失败，图片上传功能将不可用，请检查 minio 容器");
        }, "minio-bucket-init").start();
    }
}
