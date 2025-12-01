package com.example.noodletrail.heritage.controller;

import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.heritage.dto.HeritageDTO;
import com.example.noodletrail.heritage.entity.ShaanxiIntangibleHeritage;
import com.example.noodletrail.heritage.service.ShaanxiIntangibleHeritageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/heritages")
public class ShaanxiIntangibleHeritageController {

    private final ShaanxiIntangibleHeritageService heritageService;

    public ShaanxiIntangibleHeritageController(ShaanxiIntangibleHeritageService heritageService) {
        this.heritageService = heritageService;
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
}