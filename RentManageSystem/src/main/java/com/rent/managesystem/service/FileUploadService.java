package com.rent.managesystem.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 文件上传服务接口
 *
 * <p>处理房源图片的上传、存储、查询等操作。
 *
 * @author 连梓祺 & 团队
 */
public interface FileUploadService {

    /**
     * 上传单张房源图片
     *
     * @param file 图片文件（支持 jpg/png/webp，最大 10MB）
     * @param houseId 所属房源ID（可选，用于关联记录）
     * @param isCover 是否设为封面图
     * @return 可访问的图片 URL 路径
     */
    String uploadImage(MultipartFile file, String houseId, boolean isCover);

    /**
     * 批量上传房源图片（最多9张）
     *
     * @param files 图片文件列表
     * @param houseId 所属房源ID
     * @return 所有图片的 URL 列表
     */
    List<String> uploadImages(List<MultipartFile> files, String houseId);

    /**
     * 根据房源ID获取所有图片URL列表
     */
    List<String> getHouseImageUrls(String houseId);

    /**
     * 将已上传的临时图片关联到指定房源
     *
     * @param imageUrl 图片URL
     * @param houseId  房源ID
     * @param isCover  是否设为封面图
     */
    void linkImageToHouse(String imageUrl, String houseId, boolean isCover);
}
