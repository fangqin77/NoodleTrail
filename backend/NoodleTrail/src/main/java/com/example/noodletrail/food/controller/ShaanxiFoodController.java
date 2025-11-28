package com.example.noodletrail.food.controller;

import com.example.noodletrail.food.entity.ShaanxiFood;
import com.example.noodletrail.food.service.ShaanxiFoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
public class ShaanxiFoodController {

    private final ShaanxiFoodService shaanxiFoodService;

    public ShaanxiFoodController(ShaanxiFoodService shaanxiFoodService) {
        this.shaanxiFoodService = shaanxiFoodService;
    }

    // 原有搜索：支持 name/tag
    @GetMapping("/search")
    public ResponseEntity<List<ShaanxiFood>> search(@RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String tag) {
        List<Map<String, Object>> mapData = shaanxiFoodService.search(name, tag);
        List<ShaanxiFood> result = convertMapToShaanxiFood(mapData);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<List<ShaanxiFood>> searchByName(@RequestParam String name) {
        List<Map<String, Object>> mapData = shaanxiFoodService.search(name, null);
        List<ShaanxiFood> result = convertMapToShaanxiFood(mapData);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-tag")
    public ResponseEntity<List<ShaanxiFood>> searchByTag(@RequestParam String tag) {
        List<Map<String, Object>> mapData = shaanxiFoodService.search(null, tag);
        List<ShaanxiFood> result = convertMapToShaanxiFood(mapData);
        return ResponseEntity.ok(result);
    }

    // 新增：按 id/名称/标签组合查询
    @GetMapping("/query")
    public ResponseEntity<List<ShaanxiFood>> queryByConditions(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String foodName,
            @RequestParam(required = false) String tag) {
        List<ShaanxiFood> foods = shaanxiFoodService.queryByConditions(id, foodName, tag);
        return ResponseEntity.ok(foods);
    }

    // 新增：按城市查询
    @GetMapping("/city")
    public ResponseEntity<List<ShaanxiFood>> getByCity(@RequestParam String city) {
        List<ShaanxiFood> foods = shaanxiFoodService.getByCity(city);
        return ResponseEntity.ok(foods);
    }

    // 新增：按城市 + 标签查询
    @GetMapping("/city-tag")
    public ResponseEntity<List<ShaanxiFood>> getByCityAndTag(@RequestParam String city,
                                                             @RequestParam String tag) {
        List<ShaanxiFood> foods = shaanxiFoodService.getByCityAndTag(city, tag);
        return ResponseEntity.ok(foods);
    }

    private List<ShaanxiFood> convertMapToShaanxiFood(List<Map<String, Object>> mapList) {
        List<ShaanxiFood> foodList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            ShaanxiFood food = new ShaanxiFood();
            if (map.get("id") != null) {
                food.setId(((Number) map.get("id")).intValue());
            }
            if (map.get("foodName") != null) {
                food.setFoodName((String) map.get("foodName"));
            }
            if (map.get("imageUrl") != null) {
                food.setImageUrl((String) map.get("imageUrl"));
            }
            if (map.get("history") != null) {
                food.setHistory((String) map.get("history"));
            }
            if (map.get("introduction") != null) {
                food.setIntroduction((String) map.get("introduction"));
            }
            if (map.get("features") != null) {
                food.setFeatures((String) map.get("features"));
            }
            if (map.get("tag") != null) {
                food.setTag((String) map.get("tag"));
            }
            if (map.get("city") != null) {
                food.setCity((String) map.get("city"));
            }
            foodList.add(food);
        }
        return foodList;
    }
}
