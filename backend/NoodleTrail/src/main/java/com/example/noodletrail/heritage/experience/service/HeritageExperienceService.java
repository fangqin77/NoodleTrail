package com.example.noodletrail.heritage.experience.service;

import com.example.noodletrail.heritage.experience.dto.HeritageExperienceDTO;
import com.example.noodletrail.heritage.experience.entity.HeritageExperience;

import java.util.List;

public interface HeritageExperienceService {

    Long create(HeritageExperience experience);

    List<HeritageExperienceDTO> listByUser(String userId, int page, int size);

    HeritageExperienceDTO findByIdAndUser(Long id, String userId);

    void delete(Long id, String userId);

    boolean hasExperience(String userId, Integer heritageId);

    /**
     * 取消指定非遗的体验标记（按用户 + heritageId 删除所有体验记录）
     */
    void deleteByUserAndHeritage(String userId, Integer heritageId);
}
