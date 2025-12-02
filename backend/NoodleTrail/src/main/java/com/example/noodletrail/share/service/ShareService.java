package com.example.noodletrail.share.service;

import com.example.noodletrail.share.dto.ShareRecordDTO;
import com.example.noodletrail.share.dto.WechatSignVO;

public interface ShareService {

    /**
     * 记录分享行为
     */
    void recordShare(String userId, ShareRecordDTO dto);

    /**
     * 获取微信分享签名
     */
    WechatSignVO getWechatShareSign(String url);

    /**
     * 判断当前用户是否分享过指定内容
     */
    boolean hasShared(String userId, Long targetId, String targetType);
}
