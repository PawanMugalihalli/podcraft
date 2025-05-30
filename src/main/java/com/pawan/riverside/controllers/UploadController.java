package com.pawan.riverside.controllers;

import com.pawan.riverside.service.CloudStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class UploadController {



    private final CloudStorageService cloudStorageService;

    public UploadController(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    @PostMapping("/uploadVideo")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException, IOException {
        String url = cloudStorageService.uploadFile(file);

        return ResponseEntity.ok(url); // return public video URL
    }
}

