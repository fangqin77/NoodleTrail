package com.example.noodletrail.checkin.service.impl;

import com.example.noodletrail.checkin.dto.CheckinDTO;
import com.example.noodletrail.checkin.entity.UserCheckin;
import com.example.noodletrail.checkin.mapper.CheckinMapper;
import com.example.noodletrail.checkin.service.CheckinService;
import com.example.noodletrail.checkin.vo.CheckinVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;

    public CheckinServiceImpl(CheckinMapper checkinMapper) {
        this.checkinMapper = checkinMapper;
    }

    @Override
    public void addCheckin(CheckinDTO dto) {
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(dto.getUserId());
        checkin.setContent(dto.getContent());
        checkin.setLocationName(dto.getLocationName());
        checkin.setLongitude(dto.getLongitude());
        checkin.setLatitude(dto.getLatitude());
        checkin.setImageUrls(dto.getImageUrls());
        checkinMapper.insert(checkin);
    }

    @Override
    public List<CheckinVO> getUserCheckins(String userId, int page, int size) {
        int offset = (page - 1) * size;
        List<UserCheckin> list = checkinMapper.selectByUserId(userId, offset, size);
        List<CheckinVO> result = new ArrayList<>();
        for (UserCheckin c : list) {
            CheckinVO vo = new CheckinVO();
            vo.setId(c.getId());
            vo.setContent(c.getContent());
            vo.setLocationName(c.getLocationName());
            vo.setCreateTime(c.getCreateTime());
            if (c.getImageUrls() != null && !c.getImageUrls().isEmpty()) {
                vo.setImageUrls(c.getImageUrls().split(","));
            } else {
                vo.setImageUrls(new String[0]);
            }
            result.add(vo);
        }
        return result;
    }
}
