package com.example.noodletrail.checkin.controller;

import com.example.noodletrail.checkin.dto.CheckinDTO;
import com.example.noodletrail.checkin.service.CheckinService;
import com.example.noodletrail.checkin.service.OssService;
import com.example.noodletrail.checkin.util.MapUtils;
import com.example.noodletrail.checkin.vo.CheckinVO;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;
    private final MapUtils mapUtils;
    private final OssService ossService;
    private final WxUserService wxUserService;

    public CheckinController(CheckinService checkinService,
                             MapUtils mapUtils,
                             OssService ossService,
                             WxUserService wxUserService) {
        this.checkinService = checkinService;
        this.mapUtils = mapUtils;
        this.ossService = ossService;
        this.wxUserService = wxUserService;
    }

    /**
     * 解析经纬度为详细地址（含门店/POI 列表）
     */
    @GetMapping("/get-location")
    public ResponseEntity<Map<String, Object>> getLocation(@RequestParam double longitude,
                                                           @RequestParam double latitude) {
        Map<String, Object> locationData = mapUtils.reverseGeocode(longitude, latitude);
        return ResponseEntity.ok(locationData);
    }

    /**
     * 提交打卡（图片 + 文案 + 定位），关联微信用户 openid
     */
    @PostMapping("/add")
    public ResponseEntity<String> addCheckin(@RequestParam String openid,
                                             @RequestParam(required = false) String content,
                                             @RequestParam String locationName,
                                             @RequestParam double longitude,
                                             @RequestParam double latitude,
                                             @RequestParam(required = false) MultipartFile[] images) {
        WxUser user = wxUserService.getByOpenid(openid);
        if (user == null) {
            return ResponseEntity.badRequest().body("用户未登录，请先授权");
        }

        String imageUrls = "";
        if (images != null && images.length > 0) {
            List<String> urlList = new ArrayList<>();
            for (MultipartFile file : images) {
                String url = ossService.upload(file);
                urlList.add(url);
            }
            imageUrls = String.join(",", urlList);
        }

        CheckinDTO dto = new CheckinDTO();
        dto.setUserId(openid);
        dto.setContent(content);
        dto.setLocationName(locationName);
        dto.setLongitude(longitude);
        dto.setLatitude(latitude);
        dto.setImageUrls(imageUrls);

        checkinService.addCheckin(dto);
        return ResponseEntity.ok("打卡成功");
    }

    /**
     * 获取用户打卡列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CheckinVO>> getUserCheckins(@PathVariable String userId,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        List<CheckinVO> list = checkinService.getUserCheckins(userId, page, size);
        return ResponseEntity.ok(list);
    }
}
