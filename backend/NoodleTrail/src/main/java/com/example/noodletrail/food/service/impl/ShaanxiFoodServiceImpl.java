package com.example.noodletrail.food.service.impl;

import com.example.noodletrail.food.entity.ShaanxiFood;
import com.example.noodletrail.food.mapper.ShaanxiFoodMapper;
import com.example.noodletrail.food.service.ShaanxiFoodService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
}
