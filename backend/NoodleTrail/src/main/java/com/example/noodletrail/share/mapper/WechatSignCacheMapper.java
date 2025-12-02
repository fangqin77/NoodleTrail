package com.example.noodletrail.share.mapper;

import com.example.noodletrail.share.entity.WechatSignCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WechatSignCacheMapper {

    WechatSignCache selectValidCache(@Param("appid") String appid);

    void upsertCache(WechatSignCache cache);
}
