package com.example.noodletrail.checkin.service.impl;

import com.example.noodletrail.checkin.dto.CheckinDTO;
import com.example.noodletrail.checkin.entity.UserCheckin;
import com.example.noodletrail.checkin.mapper.CheckinMapper;
import com.example.noodletrail.checkin.service.CheckinService;
import com.example.noodletrail.checkin.vo.CheckinVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;

    public CheckinServiceImpl(CheckinMapper checkinMapper) {
        this.checkinMapper = checkinMapper;
    }

    @Override
    public CheckinVO addCheckin(CheckinDTO dto) {
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(dto.getUserId());
        checkin.setContent(dto.getContent());
        checkin.setLocationName(dto.getLocationName());
        checkin.setCity(dto.getCity());
        checkin.setLongitude(dto.getLongitude());
        checkin.setLatitude(dto.getLatitude());
        checkin.setImageUrls(dto.getImageUrls());
        checkinMapper.insert(checkin);
        return toVo(checkin);
    }

    @Override
    public List<CheckinVO> getUserCheckins(String userId, int page, int size) {
        int offset = (page - 1) * size;
        List<UserCheckin> list = checkinMapper.selectByUserId(userId, offset, size);
        List<CheckinVO> result = new ArrayList<>();
        for (UserCheckin c : list) {
            result.add(toVo(c));
        }
        return result;
    }

    @Override
    public List<CheckinVO> getAllCheckins(int page, int size, String city) {
        int offset = (page - 1) * size;
        List<UserCheckin> list;
        if (city != null && !city.isBlank()) {
            list = checkinMapper.selectAllByCity(city, offset, size);
        } else {
            list = checkinMapper.selectAll(offset, size);
        }
        List<CheckinVO> result = new ArrayList<>();
        for (UserCheckin c : list) {
            result.add(toVo(c));
        }
        return result;
    }

    @Override
    public CheckinVO updateCheckinImages(Integer id, String userId, List<String> newImageUrls) {
        UserCheckin existing = checkinMapper.selectByIdAndUser(id, userId);
        if (existing == null) {
            throw new RuntimeException("打卡记录不存在或无权限");
        }
        List<String> merged = new ArrayList<>();
        if (existing.getImageUrls() != null && !existing.getImageUrls().isEmpty()) {
            merged.addAll(Arrays.asList(existing.getImageUrls().split(",")));
        }
        if (newImageUrls != null) {
            for (String url : newImageUrls) {
                if (url != null && !url.isBlank()) {
                    merged.add(url);
                }
            }
        }
        String joined = merged.isEmpty() ? null : String.join(",", merged);
        existing.setImageUrls(joined);
        checkinMapper.updateImageUrls(id, userId, joined);
        return toVo(existing);
    }

    @Override
    public CheckinVO getCheckinByIdAndUser(Integer id, String userId) {
        UserCheckin c = checkinMapper.selectByIdAndUser(id, userId);
        if (c == null) {
            return null;
        }
        return toVo(c);
    }

    @Override
    public void deleteCheckin(Integer id, String userId) {
        int rows = checkinMapper.deleteByIdAndUser(id, userId);
        if (rows == 0) {
            throw new RuntimeException("打卡记录不存在或无权限");
        }
    }

    @Override
    public int countUserCheckins(String userId) {
        return checkinMapper.countByUserId(userId);
    }

    private CheckinVO toVo(UserCheckin c) {
        CheckinVO vo = new CheckinVO();
        vo.setId(c.getId());
        vo.setContent(c.getContent());
        vo.setLocationName(c.getLocationName());
        vo.setCity(c.getCity());
        vo.setLongitude(c.getLongitude());
        vo.setLatitude(c.getLatitude());
        vo.setCreateTime(c.getCreateTime());
        if (c.getImageUrls() != null && !c.getImageUrls().isEmpty()) {
            vo.setImageUrls(c.getImageUrls().split(","));
        } else {
            vo.setImageUrls(new String[0]);
        }
        return vo;
    }
}