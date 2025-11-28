package com.example.noodletrail.heritage.service.impl;

import com.example.noodletrail.heritage.entity.ShaanxiIntangibleHeritage;
import com.example.noodletrail.heritage.mapper.ShaanxiIntangibleHeritageMapper;
import com.example.noodletrail.heritage.service.ShaanxiIntangibleHeritageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShaanxiIntangibleHeritageServiceImpl implements ShaanxiIntangibleHeritageService {

    private final ShaanxiIntangibleHeritageMapper heritageMapper;

    public ShaanxiIntangibleHeritageServiceImpl(ShaanxiIntangibleHeritageMapper heritageMapper) {
        this.heritageMapper = heritageMapper;
    }

    @Override
    public List<ShaanxiIntangibleHeritage> getAll() {
        return heritageMapper.findAll();
    }

    @Override
    public List<ShaanxiIntangibleHeritage> getByType(String heritageType) {
        return heritageMapper.findByType(heritageType);
    }

    @Override
    public List<ShaanxiIntangibleHeritage> searchByKeyword(String keyword) {
        return heritageMapper.searchByKeyword(keyword);
    }
}
