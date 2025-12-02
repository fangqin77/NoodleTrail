package com.example.noodletrail.share.mapper;

import com.example.noodletrail.share.entity.ShareRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShareRecordMapper {

    void insert(ShareRecord record);

    int countByUserAndTarget(@Param("userId") String userId,
                             @Param("targetType") String targetType,
                             @Param("targetId") Long targetId);
}
