package com.example.noodletrail.checkin.service;

import com.example.noodletrail.checkin.dto.CheckinDTO;
import com.example.noodletrail.checkin.vo.CheckinVO;

import java.util.List;

public interface CheckinService {

    Integer addCheckin(CheckinDTO dto);

    List<CheckinVO> getUserCheckins(String userId, int page, int size);

    CheckinVO getCheckinByIdAndUser(Integer id, String userId);

    void deleteCheckin(Integer id, String userId);

    int countUserCheckins(String userId);
}