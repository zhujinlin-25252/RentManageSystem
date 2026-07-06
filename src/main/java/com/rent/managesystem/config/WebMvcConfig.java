package com.rent.managesystem.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Web MVC 扩展配置
 *
 * <p>功能：
 * <ul>
 *   <li>静态资源映射：将上传目录映射为可访问的 URL</li>
 *   <li>启动时自动创建上传目录（避免首次上传时找不到路径）</li>
 * </ul>
 *
 * <pre>{@code
 * 文件存储: ./uploads/house/2026/05/image.jpg
 * 访问URL:  http://localhost:8080/api/uploads/house/2026/05/image.jpg
 * }</pre>
 *
 * @author 连梓祺 & 团队
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 上传根目录（与 FileUploadServiceImpl 使用同一配置值） */
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * 配置静态资源映射 — 将文件系统路径映射到 URL
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /api/uploads/** 映射到 uploads/ 目录
        // file: 前缀表示从文件系统加载（不是 classpath）
        String location = "file:" + toAbsolutePath(uploadDir) + "/";
        log.info("静态资源映射: /api/uploads/** → {}", location);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    /**
     * 应用启动后自动创建上传目录（含子目录结构）
     *
     * <p>确保首次使用前目录已存在，避免上传时报 "Directory not found" 错误。
     */
    @PostConstruct
    public void initUploadDirectories() {
        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath();

            // 创建主目录
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("已创建上传主目录: {}", basePath);
            }

            // 预创建常用子目录（按年月分目录）
            String yearMonth = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy/MM")
            );
            Path houseDir = basePath.resolve("house").resolve(yearMonth);
            Files.createDirectories(houseDir);

            log.info("✅ 上传目录就绪: {} (实际位置: {}", uploadDir, basePath.toAbsolutePath());
        } catch (Exception e) {
            log.error("❌ 初始化上传目录失败!", e);
        }
    }

    /**
     * 将相对路径转为绝对路径（兼容 Windows/Linux）
     */
    private String toAbsolutePath(String path) {
        return Paths.get(path).toAbsolutePath().normalize().toString();
    }
}
