package com.example.noodletrail.heritage.experience.mapper;

import com.example.noodletrail.heritage.experience.entity.HeritageExperience;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeritageExperienceMapper {

    void insert(HeritageExperience experience);

    List<HeritageExperience> selectByUser(@Param("userId") String userId,
                                          @Param("offset") int offset,
                                          @Param("size") int size);

    HeritageExperience selectByIdAndUser(@Param("id") Long id,
                                         @Param("userId") String userId);

    int deleteByIdAndUser(@Param("id") Long id,
                          @Param("userId") String userId);
}