package com.example.noodletrail.checklist.mapper;

import com.example.noodletrail.checklist.entity.ChecklistTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChecklistTemplateMapper {

    List<ChecklistTemplate> selectAll();

    ChecklistTemplate selectById(@Param("id") Long id);
}