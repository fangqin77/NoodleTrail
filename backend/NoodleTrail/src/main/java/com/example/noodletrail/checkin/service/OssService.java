package com.example.noodletrail.checkin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@Service
public class OssService {

    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!validImageExt(originalFilename)) {
            throw new RuntimeException("不支持的图片格式");
        }
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + suffix;
        // TODO: 替换为真实的对象存储上传逻辑，并返回可访问的 URL（https 且后缀合法）
        return "https://example.com/checkin/" + fileName;
    }

    private boolean validImageExt(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.matches(".*\\.(png|jpg|jpeg|webp|gif|bmp)$");
    }
}