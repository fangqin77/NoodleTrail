package com.example.noodletrail.food.service.impl;

import com.example.noodletrail.food.entity.ShaanxiFood;
import com.example.noodletrail.food.mapper.ShaanxiFoodMapper;
import com.example.noodletrail.food.service.ShaanxiFoodService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 陕西美食服务实现类
 */
@Service
public class ShaanxiFoodServiceImpl implements ShaanxiFoodService {

    private final ShaanxiFoodMapper shaanxiFoodMapper;

    public ShaanxiFoodServiceImpl(ShaanxiFoodMapper shaanxiFoodMapper) {
        this.shaanxiFoodMapper = shaanxiFoodMapper;
    }

    @Override
    public List<ShaanxiFood> searchFoods(String name, String tag) {
        return shaanxiFoodMapper.searchByNameOrTag(name, tag);
    }

    @Override
    public List<Map<String, Object>> search(String name, String tag) {
        return shaanxiFoodMapper.searchByNameOrTagAsMap(name, tag);
    }

    @Override
    public List<ShaanxiFood> queryByConditions(Integer id, String foodName, String tag) {
        return shaanxiFoodMapper.queryByConditions(id, foodName, tag);
    }

    @Override
    public List<ShaanxiFood> getByCity(String city) {
        return shaanxiFoodMapper.selectByCity(city);
    }

    @Override
    public List<ShaanxiFood> getByCityAndTag(String city, String tag) {
        return shaanxiFoodMapper.selectByCityAndTag(city, tag);
    }

    @Override
    public List<ShaanxiFood> getByFeatureTags(List<String> featureTags) {
        if (featureTags == null || featureTags.isEmpty()) {
            // 未指定特色标签时，返回全部美食列表
            return shaanxiFoodMapper.searchByNameOrTag(null, null);
        }
        return shaanxiFoodMapper.selectByFeatureTags(featureTags);
    }

    @Override
    public List<String> getAllTags() {
        List<String> raw = shaanxiFoodMapper.findAllTags();
        if (raw == null) {
            return new ArrayList<>();
        }
        return raw.stream()
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllFeatureTags() {
        List<String> rawList = shaanxiFoodMapper.findAllFeatureTagsRaw();
        if (rawList == null || rawList.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> set = new HashSet<>();
        for (String raw : rawList) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            // 按常见分隔符切分：逗号、顿号等
            String[] parts = raw.split("[，,、]");
            for (String p : parts) {
                if (p != null) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        set.add(trimmed);
                    }
                }
            }
        }
        return set.stream().sorted().collect(Collectors.toList());
    }
}