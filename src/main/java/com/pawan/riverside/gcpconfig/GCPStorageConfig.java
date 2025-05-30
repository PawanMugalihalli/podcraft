package com.pawan.riverside.gcpconfig;

import org.springframework.beans.factory.annotation.Value;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GCPStorageConfig {

    @Value("${gcp.credentials.location}")
    private String credentialsPath;

    @Value("${gcp.project.id}")
    private String projectId;

    @Bean
    public Storage storage() throws IOException {
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(credentialsPath.replace("classpath:", ""));
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(ServiceAccountCredentials.fromStream(serviceAccount))
                .build()
                .getService();
    }
}

