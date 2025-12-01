package com.example.noodletrail.food.controller;

import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.food.dto.FoodDTO;
import com.example.noodletrail.food.entity.ShaanxiFood;
import com.example.noodletrail.food.service.ShaanxiFoodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
public class ShaanxiFoodController {

    private final ShaanxiFoodService shaanxiFoodService;

    public ShaanxiFoodController(ShaanxiFoodService shaanxiFoodService) {
        this.shaanxiFoodService = shaanxiFoodService;
    }

    // 搜索：支持 name/tag
    @GetMapping("/search")
    public ApiResponse<List<FoodDTO>> search(@RequestParam(required = false) String name,
                                             @RequestParam(required = false) String tag) {
        List<ShaanxiFood> foods = shaanxiFoodService.searchFoods(name, tag);
        return ApiResponse.ok(toDtoList(foods));
    }

    // 按名称搜索
    @GetMapping("/search/by-name")
    public ApiResponse<List<FoodDTO>> searchByName(@RequestParam String name) {
        List<ShaanxiFood> foods = shaanxiFoodService.searchFoods(name, null);
        return ApiResponse.ok(toDtoList(foods));
    }

    // 按标签搜索
    @GetMapping("/search/by-tag")
    public ApiResponse<List<FoodDTO>> searchByTag(@RequestParam String tag) {
        List<ShaanxiFood> foods = shaanxiFoodService.searchFoods(null, tag);
        return ApiResponse.ok(toDtoList(foods));
    }

    // 按 id/名称/标签组合查询
    @GetMapping("/query")
    public ApiResponse<List<FoodDTO>> queryByConditions(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String foodName,
            @RequestParam(required = false) String tag) {
        List<ShaanxiFood> foods = shaanxiFoodService.queryByConditions(id, foodName, tag);
        return ApiResponse.ok(toDtoList(foods));
    }

    // 按城市查询
    @GetMapping("/city")
    public ApiResponse<List<FoodDTO>> getByCity(@RequestParam String city) {
        List<ShaanxiFood> foods = shaanxiFoodService.getByCity(city);
        return ApiResponse.ok(toDtoList(foods));
    }

    // 按城市 + 标签查询
    @GetMapping("/city-tag")
    public ApiResponse<List<FoodDTO>> getByCityAndTag(@RequestParam String city,
                                                      @RequestParam String tag) {
        List<ShaanxiFood> foods = shaanxiFoodService.getByCityAndTag(city, tag);
        return ApiResponse.ok(toDtoList(foods));
    }

    // 按特色标签筛选（非遗美食、老字号、网红打卡、本地人推荐等）
    // 前端调用示例：/api/foods/feature-tags?tags=非遗美食&tags=网红打卡
    @GetMapping("/feature-tags")
    public ApiResponse<List<FoodDTO>> getByFeatureTags(
            @RequestParam(value = "tags", required = false) List<String> tags) {
        List<ShaanxiFood> foods = shaanxiFoodService.getByFeatureTags(tags);
        return ApiResponse.ok(toDtoList(foods));
    }

    // 获取所有可用的基础标签和特色标签
    @GetMapping("/tags")
    public ApiResponse<Map<String, List<String>>> getTags() {
        List<String> tags = shaanxiFoodService.getAllTags();
        List<String> featureTags = shaanxiFoodService.getAllFeatureTags();
        Map<String, List<String>> data = new HashMap<>();
        data.put("tags", tags);
        data.put("featureTags", featureTags);
        return ApiResponse.ok(data);
    }

    private List<FoodDTO> toDtoList(List<ShaanxiFood> list) {
        List<FoodDTO> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (ShaanxiFood food : list) {
            result.add(toDto(food));
        }
        return result;
    }

    private FoodDTO toDto(ShaanxiFood food) {
        FoodDTO dto = new FoodDTO();
        dto.setId(food.getId());
        dto.setFoodName(food.getFoodName());
        dto.setImageUrl(food.getImageUrl());
        dto.setHistory(food.getHistory());
        dto.setIntroduction(food.getIntroduction());
        dto.setFeatures(food.getFeatures());
        dto.setTag(food.getTag());
        dto.setCity(food.getCity());
        dto.setFeatureTags(food.getFeatureTags());
        return dto;
    }
}