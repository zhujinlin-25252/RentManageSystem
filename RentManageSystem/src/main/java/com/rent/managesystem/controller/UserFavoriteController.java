package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.common.exception.BizException;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.dto.FavoriteVO;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.HouseInfo;
import com.rent.managesystem.entity.UserFavorite;
import com.rent.managesystem.service.HouseService;
import com.rent.managesystem.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户收藏控制器
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;
    private final HouseService houseService;

    /** 收藏房源 */
    @PostMapping("/{houseId}")
    public Result<Void> addFavorite(@PathVariable String houseId) {
        String userId = getCurrentUserId();

        // 检查是否已收藏（唯一约束 uk_user_house 会拦截重复，但提前检查体验更好）
        long count = userFavoriteService.lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getHouseId, houseId)
                .count();

        if (count > 0) {
            throw new BizException(200, "该房源已在收藏中");
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setHouseId(houseId);
        boolean saved = userFavoriteService.save(favorite);
        return saved ? Result.success() : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    /** 取消收藏 */
    @DeleteMapping("/{houseId}")
    public Result<Void> removeFavorite(@PathVariable String houseId) {
        String userId = getCurrentUserId();
        boolean removed = userFavoriteService.lambdaUpdate()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getHouseId, houseId)
                .remove();
        return removed ? Result.success() : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    /** 检查是否已收藏某房源 */
    @GetMapping("/check/{houseId}")
    public Result<Boolean> checkFavorited(@PathVariable String houseId) {
        String userId = getCurrentUserId();
        long count = userFavoriteService.lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getHouseId, houseId)
                .count();
        return Result.success(count > 0);
    }

    /** 我的收藏列表（含房源信息） */
    @GetMapping("/list")
    public Result<PageResult<FavoriteVO>> myFavorites(PageQuery query) {
        String userId = getCurrentUserId();
        Page<UserFavorite> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<UserFavorite> favPage = userFavoriteService.lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime)
                .page(page);

        // 批量查询关联房源信息
        Set<String> houseIds = favPage.getRecords().stream()
                .map(UserFavorite::getHouseId)
                .collect(Collectors.toSet());
        Map<String, HouseInfo> houseMap = Map.of();
        if (!houseIds.isEmpty()) {
            List<HouseInfo> houses = houseService.listByIds(houseIds);
            houseMap = houses.stream()
                    .collect(Collectors.toMap(HouseInfo::getHouseId, h -> h));
        }

        // 组装 FavoriteVO
        Map<String, HouseInfo> finalHouseMap = houseMap;
        List<FavoriteVO> vos = favPage.getRecords().stream().map(fav -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(fav.getId());
            vo.setHouseId(fav.getHouseId());
            vo.setCreateTime(fav.getCreateTime());
            HouseInfo h = finalHouseMap.get(fav.getHouseId());
            if (h != null) {
                vo.setTitle(h.getTitle());
                vo.setCoverImage(h.getCoverImage());
                vo.setAddress(h.getAddress());
                vo.setCity(h.getCity());
                vo.setDistrict(h.getDistrict());
                vo.setRentMonthly(h.getRentMonthly());
                vo.setArea(h.getArea());
                vo.setRooms(h.getRooms());
                vo.setHalls(h.getHalls());
                vo.setOrientation(h.getOrientation());
                vo.setStatus(h.getStatus());
            }
            return vo;
        }).collect(Collectors.toList());

        PageResult<FavoriteVO> result = new PageResult<>();
        result.setRecords(vos);
        result.setTotal(favPage.getTotal());
        result.setPage(favPage.getCurrent());
        result.setSize(favPage.getSize());
        result.setPages(favPage.getPages());
        return Result.success(result);
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
