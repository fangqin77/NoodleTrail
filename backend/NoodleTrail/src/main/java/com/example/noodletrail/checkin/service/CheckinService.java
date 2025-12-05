package com.example.noodletrail.checkin.service;

import com.example.noodletrail.checkin.dto.CheckinDTO;
import com.example.noodletrail.checkin.vo.CheckinVO;

import java.util.List;

public interface CheckinService {

    CheckinVO addCheckin(CheckinDTO dto);

    CheckinVO updateCheckinImages(Integer id, String userId, java.util.List<String> newImageUrls);

    List<CheckinVO> getUserCheckins(String userId, int page, int size);

    List<CheckinVO> getAllCheckins(int page, int size, String city);

    CheckinVO getCheckinByIdAndUser(Integer id, String userId);

    void deleteCheckin(Integer id, String userId);

    int countUserCheckins(String userId);
}