package com.example.noodletrail.checkin.mapper;

import com.example.noodletrail.checkin.entity.UserCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CheckinMapper {

    void insert(UserCheckin checkin);

    List<UserCheckin> selectByUserId(@Param("userId") String userId,
                                     @Param("offset") int offset,
                                     @Param("size") int size);
}
