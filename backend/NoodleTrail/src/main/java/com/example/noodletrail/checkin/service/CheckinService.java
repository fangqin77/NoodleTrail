package com.example.noodletrail.checkin.service;

import com.example.noodletrail.checkin.dto.CheckinDTO;
import com.example.noodletrail.checkin.vo.CheckinVO;

import java.util.List;

public interface CheckinService {

    void addCheckin(CheckinDTO dto);

    List<CheckinVO> getUserCheckins(String userId, int page, int size);
}
