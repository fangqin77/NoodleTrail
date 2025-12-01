package com.example.noodletrail.checkin.controller;

import com.example.noodletrail.checkin.dto.CheckinDTO;
import com.example.noodletrail.checkin.service.CheckinService;
import com.example.noodletrail.checkin.service.OssService;
import com.example.noodletrail.checkin.util.MapUtils;
import com.example.noodletrail.checkin.vo.CheckinVO;
import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;
    private final MapUtils mapUtils;
    private final OssService ossService;
    private final WxUserService wxUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public ApiResponse<Map<String, Object>> getLocation(@RequestParam double longitude,
                                                        @RequestParam double latitude) {
        Map<String, Object> locationData = mapUtils.reverseGeocode(longitude, latitude);
        return ApiResponse.ok(locationData);
    }

    /**
     * JSON 提交打卡：图片 URL 已预先上传
     * Body 示例：{
     *   "locationName": "华阴老腔表演馆",
     *   "longitude": 108.123,
     *   "latitude": 34.123,
     *   "content": "今天在这里打卡！",
     *   "images": ["https://...", "https://..."]
     * }
     */
    @PostMapping("/add")
    public ApiResponse<Map<String, Object>> addJson(@RequestBody Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();
        String locationName = (String) body.get("locationName");
        Double longitude = toDouble(body.get("longitude"));
        Double latitude = toDouble(body.get("latitude"));
        String content = body.get("content") != null ? body.get("content").toString() : null;

        String imageUrls = null;
        Object imagesObj = body.get("images");
        if (imagesObj instanceof List<?> list) {
            List<String> urls = new ArrayList<>();
            for (Object o : list) {
                if (o != null) {
                    urls.add(o.toString());
                }
            }
            if (!urls.isEmpty()) {
                imageUrls = String.join(",", urls);
            }
        }

        CheckinDTO dto = new CheckinDTO();
        dto.setUserId(userOpenid);
        dto.setContent(content);
        dto.setLocationName(locationName);
        dto.setLongitude(longitude);
        dto.setLatitude(latitude);
        dto.setImageUrls(imageUrls);

        Integer id = checkinService.addCheckin(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return ApiResponse.ok(data);
    }

    /**
     * multipart 提交打卡：直接上传图片 + JSON 文本
     * data 字段为 JSON 字符串，结构同 addJson
     */
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> addMultipart(@RequestPart("data") String jsonData,
                                                         @RequestPart(value = "images", required = false) List<MultipartFile> images)
            throws Exception {
        String userOpenid = getCurrentUserOpenid();

        Map<String, Object> body = objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {
        });
        String locationName = (String) body.get("locationName");
        Double longitude = toDouble(body.get("longitude"));
        Double latitude = toDouble(body.get("latitude"));
        String content = body.get("content") != null ? body.get("content").toString() : null;

        List<String> urlList = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = ossService.upload(file);
                urlList.add(url);
            }
        }
        String imageUrls = urlList.isEmpty() ? null : String.join(",", urlList);

        CheckinDTO dto = new CheckinDTO();
        dto.setUserId(userOpenid);
        dto.setContent(content);
        dto.setLocationName(locationName);
        dto.setLongitude(longitude);
        dto.setLatitude(latitude);
        dto.setImageUrls(imageUrls);

        Integer id = checkinService.addCheckin(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return ApiResponse.ok(data);
    }

    /**
     * 获取当前用户打卡列表
     */
    @GetMapping("/me")
    public ApiResponse<List<CheckinVO>> myCheckins(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        String userOpenid = getCurrentUserOpenid();
        List<CheckinVO> list = checkinService.getUserCheckins(userOpenid, page, size);
        return ApiResponse.ok(list);
    }

    /**
     * 获取单条打卡详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CheckinVO> getOne(@PathVariable Integer id) {
        String userOpenid = getCurrentUserOpenid();
        CheckinVO vo = checkinService.getCheckinByIdAndUser(id, userOpenid);
        return ApiResponse.ok(vo);
    }

    /**
     * 删除当前用户的一条打卡记录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Integer id) {
        String userOpenid = getCurrentUserOpenid();
        checkinService.deleteCheckin(id, userOpenid);
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        return ApiResponse.ok(data);
    }

    private String getCurrentUserOpenid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        Object principal = authentication.getPrincipal();
        Integer userId;
        if (principal instanceof Long l) {
            userId = l.intValue();
        } else if (principal instanceof Integer i) {
            userId = i;
        } else {
            userId = Integer.parseInt(principal.toString());
        }
        WxUser user = wxUserService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getOpenid();
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}