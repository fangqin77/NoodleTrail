package com.example.noodletrail.heritage.mapper;

import com.example.noodletrail.heritage.entity.HeritageFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeritageFavoriteMapper {

    void insert(HeritageFavorite favorite);

    int deleteByUserAndHeritage(@Param("userId") String userId,
                                @Param("heritageId") Integer heritageId);

    int countByUserAndHeritage(@Param("userId") String userId,
                               @Param("heritageId") Integer heritageId);

    List<Integer> selectHeritageIdsByUser(@Param("userId") String userId);
}
