package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.common.exception.BizException;
import com.rent.managesystem.entity.HouseImage;
import com.rent.managesystem.mapper.HouseImageMapper;
import com.rent.managesystem.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件上传服务实现类
 *
 * <p>核心功能：
 * <ul>
 *   <li>本地磁盘存储：按日期分目录，UUID 重命名防冲突</li>
 *   <li>格式校验：仅允许 jpg/png/webp</li>
 *   <li>数量限制：单房源最多 9 张图片</li>
 *   <li>封面管理：支持设置/切换封面图</li>
 * </ul>
 *
 * @author 连梓祺 & 团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final HouseImageMapper houseImageMapper;

    /** 上传根目录（相对于项目运行路径） */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 允许上传的图片格式 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "webp"
    );

    /** 单房源最大图片数量 */
    private static final int MAX_IMAGES_PER_HOUSE = 9;

    /**
     * 上传单张房源图片
     *
     * <p>流程：
     * <ol>
     *   <li>校验文件非空、格式、大小</li>
     *   <li>检查该房源图片数量是否超限</li>
     *   <li>按日期创建存储目录（uploads/house/yyyy/MM/）</li>
     *   <li>UUID 重命名后写入磁盘</li>
     *   <li>记录到 house_image 表</li>
     *   <li>返回可访问的 URL 路径</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadImage(MultipartFile file, String houseId, boolean isCover) {
        // 1. 基本校验
        validateFile(file);

        // 2. 检查图片数量限制（有 houseId 时才检查）
        if (houseId != null && !houseId.isEmpty()) {
            long currentCount = houseImageMapper.selectCount(
                new LambdaQueryWrapper<HouseImage>().eq(HouseImage::getHouseId, houseId)
            );
            if (currentCount >= MAX_IMAGES_PER_HOUSE) {
                throw new BizException(ErrorCode.HOUSE_IMAGE_LIMIT);
            }
        }

        // 3. 确定存储路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path dirPath = Paths.get(uploadDir, "house", datePath).toAbsolutePath();

        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", dirPath, e);
            throw new BizException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 4. 生成文件名并写入
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path filePath = dirPath.resolve(newFileName);

        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            throw new BizException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 5. 计算可访问的 URL 路径（对应 WebMvcConfig 的资源映射）
        // 存储路径: uploads/house/2026/05/xxx.jpg（项目根目录下）
        // URL路径:   /api/uploads/house/2026/05/xxx.jpg（经Vite代理到后端）
        String imageUrl = "/api/uploads/house/" + datePath + "/" + newFileName;

        // 6. 如果有关联房源，写入数据库记录
        if (houseId != null && !houseId.isEmpty()) {
            // 如果设为封面，先取消其他封面
            if (isCover) {
                clearCoverFlag(houseId);
            }

            HouseImage imageRecord = new HouseImage();
            imageRecord.setHouseId(houseId);
            imageRecord.setImageUrl(imageUrl);

            // 查询当前最大排序号
            Integer maxSort = getMaxSortOrder(houseId);
            imageRecord.setSortOrder(maxSort + 1);
            imageRecord.setIsCover(isCover ? 1 : 0);

            houseImageMapper.insert(imageRecord);
            log.info("图片上传成功: houseId={}, url={}, isCover={}", houseId, imageUrl, isCover);
        } else {
            log.info("临时图片上传成功: url={}", imageUrl);
        }

        return imageUrl;
    }

    /**
     * 批量上传图片
     *
     * <p>第一张自动设为封面图。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> uploadImages(List<MultipartFile> files, String houseId) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        if (files.size() > MAX_IMAGES_PER_HOUSE) {
            throw new BizException(ErrorCode.HOUSE_IMAGE_LIMIT);
        }

        List<String> urls = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null || file.isEmpty()) continue;

            boolean isCover = (i == 0); // 第一张默认为封面
            String url = uploadImage(file, houseId, isCover);
            urls.add(url);
        }

        return urls;
    }

    /**
     * 根据房源ID获取所有图片URL（按排序号升序）
     */
    @Override
    public List<String> getHouseImageUrls(String houseId) {
        List<HouseImage> images = houseImageMapper.selectList(
            new LambdaQueryWrapper<HouseImage>()
                .eq(HouseImage::getHouseId, houseId)
                .orderByAsc(HouseImage::getSortOrder)
                .orderByAsc(HouseImage::getImageId)
        );
        return images.stream()
            .map(HouseImage::getImageUrl)
            .collect(Collectors.toList());
    }

    /**
     * 将已上传的临时图片关联到指定房源
     *
     * <p>发布新房源时使用：先上传图片拿到 URL，发布后用此方法关联。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void linkImageToHouse(String imageUrl, String houseId, boolean isCover) {
        // 检查是否已经关联过（防止重复关联）
        Long existingCount = houseImageMapper.selectCount(
            new LambdaQueryWrapper<HouseImage>()
                .eq(HouseImage::getHouseId, houseId)
                .eq(HouseImage::getImageUrl, imageUrl)
        );
        if (existingCount > 0) {
            log.info("图片已关联过，跳过: url={}, houseId={}", imageUrl, houseId);
            return;
        }

        // 如果设为封面，先取消其他封面标记
        if (isCover) {
            clearCoverFlag(houseId);
        }

        // 获取当前最大排序号
        Integer maxSort = getMaxSortOrder(houseId);

        HouseImage imageRecord = new HouseImage();
        imageRecord.setHouseId(houseId);
        imageRecord.setImageUrl(imageUrl);
        imageRecord.setSortOrder(maxSort + 1);
        imageRecord.setIsCover(isCover ? 1 : 0);

        houseImageMapper.insert(imageRecord);
        log.info("图片关联成功: houseId={}, url={}, isCover={}", houseId, imageUrl, isCover);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验上传文件的格式和大小
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING.getCode(), "请选择要上传的文件");
        }

        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_SUPPORT);
        }

        // 大小限制由 spring.servlet.multipart.max-file-size 控制（已配置10MB）
        // 此处额外做日志记录
        long sizeMB = file.getSize() / 1024 / 1024;
        log.debug("文件校验通过: name={}, size={}MB, ext={}", originalName, sizeMB, extension);
    }

    /**
     * 从原始文件名中提取扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 取消某房源的所有封面标记
     */
    private void clearCoverFlag(String houseId) {
        LambdaQueryWrapper<HouseImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseImage::getHouseId, houseId)
               .eq(HouseImage::getIsCover, 1);

        HouseImage updateEntity = new HouseImage();
        updateEntity.setIsCover(0);
        houseImageMapper.update(updateEntity, wrapper);
    }

    /**
     * 获取当前房源的最大排序号
     */
    private Integer getMaxSortOrder(String houseId) {
        LambdaQueryWrapper<HouseImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseImage::getHouseId, houseId)
               .orderByDesc(HouseImage::getSortOrder)
               .last("LIMIT 1");

        HouseImage last = houseImageMapper.selectOne(wrapper);
        return (last != null && last.getSortOrder() != null) ? last.getSortOrder() : -1;
    }
}
