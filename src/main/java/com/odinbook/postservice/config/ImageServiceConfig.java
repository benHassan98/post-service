package com.odinbook.postservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.odinbook.service.ImageService;

import io.minio.MinioClient;

@Configuration
public class ImageServiceConfig {
  @Bean
  ImageService imageService(MinioClient minioClient) {
    return new ImageService(minioClient);
  }

}
