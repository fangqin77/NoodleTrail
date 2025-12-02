package com.example.noodletrail.heritage.controller;

import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.heritage.dto.HeritageDTO;
import com.example.noodletrail.heritage.entity.ShaanxiIntangibleHeritage;
import com.example.noodletrail.heritage.experience.dto.HeritageExperienceDTO;
import com.example.noodletrail.heritage.experience.entity.HeritageExperience;
import com.example.noodletrail.heritage.experience.service.HeritageExperienceService;
import com.example.noodletrail.heritage.mapper.HeritageFavoriteMapper;
import com.example.noodletrail.heritage.service.ShaanxiIntangibleHeritageService;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/heritages")
public class ShaanxiIntangibleHeritageController {

    private final ShaanxiIntangibleHeritageService heritageService;
    private final HeritageExperienceService heritageExperienceService;
    private final HeritageFavoriteMapper heritageFavoriteMapper;
    private final WxUserService wxUserService;

    public ShaanxiIntangibleHeritageController(ShaanxiIntangibleHeritageService heritageService,
                                               HeritageExperienceService heritageExperienceService,
                                               HeritageFavoriteMapper heritageFavoriteMapper,
                                               WxUserService wxUserService) {
        this.heritageService = heritageService;
        this.heritageExperienceService = heritageExperienceService;
        this.heritageFavoriteMapper = heritageFavoriteMapper;
        this.wxUserService = wxUserService;
    }

    // 1. 基础功能：返回所有非遗信息（支持简单分页）
    @GetMapping
    public ApiResponse<List<HeritageDTO>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ShaanxiIntangibleHeritage> all = heritageService.getAll();
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int fromIndex = (page - 1) * size;
        if (fromIndex >= all.size()) {
            return ApiResponse.ok(Collections.emptyList());
        }
        int toIndex = Math.min(fromIndex + size, all.size());
        List<ShaanxiIntangibleHeritage> pageList = all.subList(fromIndex, toIndex);
        return ApiResponse.ok(toDtoList(pageList));
    }

    // 2. 分类展示：按非遗分类查询
    @GetMapping("/type")
    public ApiResponse<List<HeritageDTO>> getByType(@RequestParam String heritageType) {
        List<ShaanxiIntangibleHeritage> list = heritageService.getByType(heritageType);
        return ApiResponse.ok(toDtoList(list));
    }

    // 3. 模糊搜索：按关键词模糊匹配名称 / 历史渊源 / 内容介绍
    @GetMapping("/search")
    public ApiResponse<List<HeritageDTO>> searchByKeyword(@RequestParam String keyword) {
        List<ShaanxiIntangibleHeritage> list = heritageService.searchByKeyword(keyword);
        return ApiResponse.ok(toDtoList(list));
    }

    // 4. 详情：根据 id 查询单条非遗信息
    @GetMapping("/detail")
    public ApiResponse<HeritageDTO> detail(@RequestParam Integer id) {
        ShaanxiIntangibleHeritage entity = heritageService.getById(id);
        HeritageDTO dto = entity != null ? toDto(entity) : null;
        return ApiResponse.ok(dto);
    }

    // 标记非遗为已体验
    @PostMapping("/{id}/experience")
    public ApiResponse<Map<String, Object>> markAsExperienced(@PathVariable("id") Integer heritageId) {
        String userOpenid = getCurrentUserOpenid();
        boolean experienced = heritageExperienceService.hasExperience(userOpenid, heritageId);
        if (!experienced) {
            HeritageExperience experience = new HeritageExperience();
            experience.setUserId(userOpenid);
            experience.setHeritageId(heritageId);
            experience.setTitle(null);
            experience.setContent(null);
            experience.setRating(null);
            experience.setLocationName(null);
            experience.setLongitude(null);
            experience.setLatitude(null);
            experience.setImageUrls(null);
            heritageExperienceService.create(experience);
            experienced = true;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("experienced", experienced);
        data.put("isExperienced", experienced);
        return ApiResponse.ok(data);
    }

    // 获取体验状态.
    @GetMapping("/{id}/experience")
    public ApiResponse<Map<String, Object>> getExperienceStatus(@PathVariable("id") Integer heritageId) {
        String userOpenid = getCurrentUserOpenid();
        boolean experienced = heritageExperienceService.hasExperience(userOpenid, heritageId);
        Map<String, Object> data = new HashMap<>();
        data.put("experienced", experienced);
        data.put("isExperienced", experienced);
        return ApiResponse.ok(data);
    }

    // 获取我的体验列表
    @GetMapping("/experiences")
    public ApiResponse<List<HeritageExperienceDTO>> getMyExperiences(@RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "100") int size) {
        String userOpenid = getCurrentUserOpenid();
        List<HeritageExperienceDTO> list = heritageExperienceService.listByUser(userOpenid, page, size);
        return ApiResponse.ok(list);
    }

    // 收藏 / 取消收藏非遗
    @PostMapping("/{id}/favorite")
    public ApiResponse<Map<String, Object>> toggleFavorite(@PathVariable("id") Integer heritageId,
                                                           @RequestBody(required = false) Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();
        boolean favoriteFlag = true;
        if (body != null && body.get("favorite") != null) {
            Object fv = body.get("favorite");
            if (fv instanceof Boolean b) {
                favoriteFlag = b;
            } else {
                favoriteFlag = Boolean.parseBoolean(fv.toString());
            }
        }
        int count = heritageFavoriteMapper.countByUserAndHeritage(userOpenid, heritageId);
        if (favoriteFlag) {
            if (count == 0) {
                com.example.noodletrail.heritage.entity.HeritageFavorite favorite = new com.example.noodletrail.heritage.entity.HeritageFavorite();
                favorite.setUserId(userOpenid);
                favorite.setHeritageId(heritageId);
                favorite.setCreateTime(LocalDateTime.now());
                heritageFavoriteMapper.insert(favorite);
            }
        } else {
            if (count > 0) {
                heritageFavoriteMapper.deleteByUserAndHeritage(userOpenid, heritageId);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("favorite", favoriteFlag);
        data.put("isFavorite", favoriteFlag);
        return ApiResponse.ok(data);
    }

    // 获取收藏状态
    @GetMapping("/{id}/favorite")
    public ApiResponse<Map<String, Object>> getFavoriteStatus(@PathVariable("id") Integer heritageId) {
        String userOpenid = getCurrentUserOpenid();
        boolean favorite = heritageFavoriteMapper.countByUserAndHeritage(userOpenid, heritageId) > 0;
        Map<String, Object> data = new HashMap<>();
        data.put("favorite", favorite);
        data.put("isFavorite", favorite);
        return ApiResponse.ok(data);
    }

    // 获取我的收藏列表
    @GetMapping("/favorites")
    public ApiResponse<List<HeritageDTO>> getMyFavorites() {
        String userOpenid = getCurrentUserOpenid();
        List<Integer> ids = heritageFavoriteMapper.selectHeritageIdsByUser(userOpenid);
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.ok(Collections.emptyList());
        }
        List<HeritageDTO> result = new ArrayList<>();
        for (Integer id : ids) {
            ShaanxiIntangibleHeritage entity = heritageService.getById(id);
            if (entity != null) {
                result.add(toDto(entity));
            }
        }
        return ApiResponse.ok(result);
    }

    private List<HeritageDTO> toDtoList(List<ShaanxiIntangibleHeritage> list) {
        List<HeritageDTO> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (ShaanxiIntangibleHeritage h : list) {
            result.add(toDto(h));
        }
        return result;
    }

    private HeritageDTO toDto(ShaanxiIntangibleHeritage h) {
        HeritageDTO dto = new HeritageDTO();
        dto.setId(h.getId());
        dto.setName(h.getHeritageName());
        dto.setCategory(h.getHeritageType());
        dto.setImageUrl(h.getImageUrl());
        dto.setVideoUrl(h.getVideoUrl());
        dto.setHistoricalOrigin(h.getHistoricalOrigin());
        dto.setContentIntroduction(h.getContentIntroduction());
        return dto;
    }

    private String getCurrentUserOpenid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        Object principal = authentication.getPrincipal();
        Integer userId;
        if (principal instanceof Long l) {
            userId = l.intValue();
        } else if (principal instanceof Integer i) {
            userId = i;
        } else {
            userId = Integer.parseInt(principal.toString());
        }
        WxUser user = wxUserService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getOpenid();
    }
}
