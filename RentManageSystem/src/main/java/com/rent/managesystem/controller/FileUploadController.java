package com.rent.managesystem.controller;

import com.rent.managesystem.common.Result;
import com.rent.managesystem.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * <p>提供房源图片的上传、查询接口。
 *
 * <pre>{@code
 * // 上传单张图片
 * POST /api/upload/image
 * FormData: file=图片文件, houseId=房源ID(可选), isCover=是否封面(可选)
 *
 * // 批量上传图片
 * POST /api/upload/images/batch
 * FormData: files=多张图片文件, houseId=房源ID
 *
 * // 查询房源图片列表
 * GET /api/upload/images/{houseId}
 * }</pre>
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 上传单张房源图片
     *
     * <p>支持两种使用场景：
     * <ul>
     *   <li>发布房源时上传：不传 houseId，返回临时 URL</li>
     *   <li>编辑房源时补充：传入 houseId，自动关联记录</li>
     * </ul>
     *
     * @param file 图片文件（必填）
     * @param houseId 房源ID（选填）
     * @param isCover 是否设为封面图（选填，默认 false）
     * @return 图片访问URL
     */
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "houseId", required = false) String houseId,
            @RequestParam(value = "isCover", defaultValue = "false") boolean isCover) {

        String userId = getCurrentUserId();
        logUpload(file, houseId, userId);

        String imageUrl = fileUploadService.uploadImage(file, houseId, isCover);

        Map<String, Object> data = new HashMap<>();
        data.put("url", imageUrl);
        data.put("filename", file.getOriginalFilename());

        return Result.success(data);
    }

    /**
     * 批量上传房源图片（最多9张）
     *
     * <p>第一张图片自动设为封面图。
     *
     * @param files 图片文件列表
     * @param houseId 房源ID（必填，用于关联记录）
     * @return 所有图片的 URL 列表
     */
    @PostMapping("/images/batch")
    public Result<Map<String, Object>> batchUploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("houseId") String houseId) {

        if (files == null || files.isEmpty()) {
            return Result.error(com.rent.managesystem.common.ErrorCode.PARAM_MISSING.getCode(), "请选择要上传的文件");
        }

        String userId = getCurrentUserId();
        log.info("批量上传: userId={}, houseId={}, 文件数={}", userId, houseId, files.size());

        List<String> urls = fileUploadService.uploadImages(files, houseId);

        Map<String, Object> data = new HashMap<>();
        data.put("urls", urls);
        data.put("count", urls.size());

        return Result.success(data);
    }

    /**
     * 获取某房源的所有图片URL列表
     *
     * @param houseId 房源ID
     * @return 图片URL列表 + 封面URL
     */
    @GetMapping("/images/{houseId}")
    public Result<Map<String, Object>> getHouseImages(@PathVariable String houseId) {
        List<String> allUrls = fileUploadService.getHouseImageUrls(houseId);

        Map<String, Object> data = new HashMap<>();
        data.put("list", allUrls);
        data.put("cover", allUrls.isEmpty() ? null : allUrls.get(0));
        data.put("count", allUrls.size());

        return Result.success(data);
    }

    /**
     * 将已上传的临时图片关联到指定房源
     *
     * <p>用于"发布新房源"场景：用户在发布页先上传图片（此时还没有 houseId），
     * 发布成功拿到 houseId 后，再调用此接口将图片记录关联到该房源。
     *
     * @param imageUrl 图片URL（上传时返回的）
     * @param houseId  房源ID
     * @param isCover  是否设为封面图
     */
    @PostMapping("/link-image")
    public Result<Void> linkImageToHouse(
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam("houseId") String houseId,
            @RequestParam(value = "isCover", defaultValue = "false") boolean isCover) {

        log.info("关联图片: houseId={}, url={}, isCover={}", houseId, imageUrl, isCover);
        fileUploadService.linkImageToHouse(imageUrl, houseId, isCover);
        return Result.success();
    }

    /**
     * 获取当前登录用户ID
     */
    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private void logUpload(MultipartFile file, String houseId, String userId) {
        log.info("图片上传请求: userId={}, houseId={}, fileName={}, size={}KB",
            userId,
            houseId != null ? houseId : "(临时上传)",
            file.getOriginalFilename(),
            file.getSize() / 1024
        );
    }
}
