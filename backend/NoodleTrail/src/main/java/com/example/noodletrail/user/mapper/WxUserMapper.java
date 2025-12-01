package com.example.noodletrail.user.mapper;

import com.example.noodletrail.user.entity.WxUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WxUserMapper {

    WxUser selectByOpenid(@Param("openid") String openid);

    WxUser selectById(@Param("id") Integer id);

    int insert(WxUser user);

    int updateById(WxUser user);
}