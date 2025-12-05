package com.example.noodletrail.checkin.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class OssService {

    /**
     * 上传图片到本地 uploads/checkin 目录，并返回可直接访问的完整 URL
     */
    public String upload(MultipartFile file, HttpServletRequest request) {
        String originalFilename = file.getOriginalFilename();
        if (!validImageExt(originalFilename)) {
            throw new RuntimeException("不支持的图片格式");
        }
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + suffix;

        // 保存到本地目录：./uploads/checkin/（绝对路径，确保目录存在）
        Path uploadBase = Paths.get("uploads", "checkin").toAbsolutePath();
        try {
            Files.createDirectories(uploadBase);
        } catch (IOException e) {
            throw new RuntimeException("创建上传目录失败: " + uploadBase, e);
        }

        Path dest = uploadBase.resolve(fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("保存图片失败: " + dest, e);
        }

        // 组装可访问的完整 URL，如 http://host:port/uploads/checkin/xxx.jpg
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder base = new StringBuilder();
        base.append(scheme).append("://").append(host);
        if (!("http".equalsIgnoreCase(scheme) && port == 80)
                && !("https".equalsIgnoreCase(scheme) && port == 443)) {
            base.append(":" ).append(port);
        }
        if (contextPath != null && !contextPath.isBlank() && !"/".equals(contextPath)) {
            if (!contextPath.startsWith("/")) {
                base.append('/');
            }
            base.append(contextPath);
        }
        if (base.charAt(base.length() - 1) != '/') {
            base.append('/');
        }
        base.append("uploads/checkin/").append(fileName);
        return base.toString();
    }

    private boolean validImageExt(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.matches(".*\\.(png|jpg|jpeg|webp|gif|bmp)$");
    }
}