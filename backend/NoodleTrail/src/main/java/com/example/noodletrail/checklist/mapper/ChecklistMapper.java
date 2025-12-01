package com.example.noodletrail.checklist.mapper;

import com.example.noodletrail.checklist.entity.Checklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChecklistMapper {

    void insert(Checklist checklist);

    List<Checklist> selectByUser(@Param("userId") String userId);

    Checklist selectByIdAndUser(@Param("id") Long id,
                                @Param("userId") String userId);

    int updateByIdAndUser(Checklist checklist);

    int deleteByIdAndUser(@Param("id") Long id,
                          @Param("userId") String userId);
}