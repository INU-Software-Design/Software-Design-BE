package com.neeis.neeis.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FcmConfig {

        private final ClassPathResource firebaseResource;
        private final String projectId;

        public FcmConfig(
                @Value("${fcm.file_path}") String firebaseFilePath,
                @Value("${fcm.project_id}") String projectId) {

            this.firebaseResource = new ClassPathResource(firebaseFilePath);
            this.projectId = projectId;
        }

        // 빈 생성 전 firebase 초기화
        @PostConstruct
        public void init() throws IOException {
            FirebaseOptions option = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(firebaseResource.getInputStream()))
                    .setProjectId(projectId)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(option);
            }
        }

        // fcm 푸시 알림을 보내는 객체
        @Bean
        FirebaseMessaging firebaseMessaging() {
            return FirebaseMessaging.getInstance(firebaseApp());
        }

        //firebase sdk의 인스턴스 역할
        @Bean
        FirebaseApp firebaseApp() {
            return FirebaseApp.getInstance();
        }
}

