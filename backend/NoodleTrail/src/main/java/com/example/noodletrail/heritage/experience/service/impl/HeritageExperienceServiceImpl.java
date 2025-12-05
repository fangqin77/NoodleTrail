package com.example.noodletrail.heritage.experience.service.impl;

import com.example.noodletrail.heritage.experience.dto.HeritageExperienceDTO;
import com.example.noodletrail.heritage.experience.entity.HeritageExperience;
import com.example.noodletrail.heritage.experience.mapper.HeritageExperienceMapper;
import com.example.noodletrail.heritage.experience.service.HeritageExperienceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class HeritageExperienceServiceImpl implements HeritageExperienceService {

    private final HeritageExperienceMapper mapper;

    public HeritageExperienceServiceImpl(HeritageExperienceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long create(HeritageExperience experience) {
        if (experience == null) {
            throw new IllegalArgumentException("体验记录不能为空");
        }
        if (experience.getTitle() == null || experience.getTitle().trim().isEmpty()) {
            experience.setTitle("体验记录");
        }
        LocalDateTime now = LocalDateTime.now();
        experience.setCreateTime(now);
        experience.setUpdateTime(now);
        mapper.insert(experience);
        return experience.getId();
    }

    @Override
    public List<HeritageExperienceDTO> listByUser(String userId, int page, int size) {
        int offset = (page - 1) * size;
        List<HeritageExperience> list = mapper.selectByUser(userId, offset, size);
        List<HeritageExperienceDTO> result = new ArrayList<>();
        for (HeritageExperience e : list) {
            result.add(toDto(e));
        }
        return result;
    }

    @Override
    public HeritageExperienceDTO findByIdAndUser(Long id, String userId) {
        HeritageExperience e = mapper.selectByIdAndUser(id, userId);
        if (e == null) {
            return null;
        }
        return toDto(e);
    }

    @Override
    public void delete(Long id, String userId) {
        int rows = mapper.deleteByIdAndUser(id, userId);
        if (rows == 0) {
            throw new RuntimeException("体验记录不存在或无权限");
        }
    }

    @Override
    public boolean hasExperience(String userId, Integer heritageId) {
        if (heritageId == null) {
            return false;
        }
        int count = mapper.countByUserAndHeritage(userId, heritageId);
        return count > 0;
    }

    @Override
    public void deleteByUserAndHeritage(String userId, Integer heritageId) {
        if (heritageId == null) {
            throw new RuntimeException("heritageId 不能为空");
        }
        mapper.deleteByUserAndHeritage(userId, heritageId);
    }

    private HeritageExperienceDTO toDto(HeritageExperience e) {
        HeritageExperienceDTO dto = new HeritageExperienceDTO();
        dto.setId(e.getId());
        dto.setHeritageId(e.getHeritageId());
        dto.setTitle(e.getTitle());
        dto.setContent(e.getContent());
        dto.setRating(e.getRating());
        dto.setLocationName(e.getLocationName());
        dto.setLongitude(e.getLongitude());
        dto.setLatitude(e.getLatitude());
        if (e.getImageUrls() != null && !e.getImageUrls().isEmpty()) {
            dto.setImages(Arrays.asList(e.getImageUrls().split(",")));
        } else {
            dto.setImages(Collections.emptyList());
        }
        dto.setCreateTime(e.getCreateTime());
        return dto;
    }
}
