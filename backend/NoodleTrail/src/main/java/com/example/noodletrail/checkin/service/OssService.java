package com.example.noodletrail.checkin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class OssService {

    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + suffix;
        // TODO: 替换为真实的对象存储上传逻辑，并返回可访问的 URL
        return "https://example.com/checkin/" + fileName;
    }
}
