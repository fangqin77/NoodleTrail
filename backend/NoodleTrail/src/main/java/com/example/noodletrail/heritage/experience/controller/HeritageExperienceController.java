package com.example.noodletrail.heritage.experience.controller;

import com.example.noodletrail.checkin.service.OssService;
import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.heritage.experience.dto.HeritageExperienceDTO;
import com.example.noodletrail.heritage.experience.entity.HeritageExperience;
import com.example.noodletrail.heritage.experience.service.HeritageExperienceService;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/heritage/experience")
public class HeritageExperienceController {

    private final HeritageExperienceService heritageExperienceService;
    private final WxUserService wxUserService;
    private final OssService ossService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HeritageExperienceController(HeritageExperienceService heritageExperienceService,
                                        WxUserService wxUserService,
                                        OssService ossService) {
        this.heritageExperienceService = heritageExperienceService;
        this.wxUserService = wxUserService;
        this.ossService = ossService;
    }

    @PostMapping("/add")
    public ApiResponse<Map<String, Object>> addJson(@RequestBody Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();

        Integer heritageId = toInteger(body.get("heritageId"));
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        Integer rating = toInteger(body.get("rating"));
        String locationName = body.get("locationName") != null ? body.get("locationName").toString() : null;
        Double longitude = toDouble(body.get("longitude"));
        Double latitude = toDouble(body.get("latitude"));

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

        HeritageExperience experience = new HeritageExperience();
        experience.setUserId(userOpenid);
        experience.setHeritageId(heritageId);
        experience.setTitle(title);
        experience.setContent(content);
        experience.setRating(rating);
        experience.setLocationName(locationName);
        experience.setLongitude(longitude);
        experience.setLatitude(latitude);
        experience.setImageUrls(imageUrls);

        Long id = heritageExperienceService.create(experience);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return ApiResponse.ok(data);
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> addMultipart(@RequestPart("data") String jsonData,
                                                         @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                                         HttpServletRequest request)
            throws Exception {
        String userOpenid = getCurrentUserOpenid();

        Map<String, Object> body = objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {
        });
        Integer heritageId = toInteger(body.get("heritageId"));
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        Integer rating = toInteger(body.get("rating"));
        String locationName = body.get("locationName") != null ? body.get("locationName").toString() : null;
        Double longitude = toDouble(body.get("longitude"));
        Double latitude = toDouble(body.get("latitude"));

        List<String> urlList = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = ossService.upload(file, request);
                urlList.add(url);
            }
        }
        String imageUrls = urlList.isEmpty() ? null : String.join(",", urlList);

        HeritageExperience experience = new HeritageExperience();
        experience.setUserId(userOpenid);
        experience.setHeritageId(heritageId);
        experience.setTitle(title);
        experience.setContent(content);
        experience.setRating(rating);
        experience.setLocationName(locationName);
        experience.setLongitude(longitude);
        experience.setLatitude(latitude);
        experience.setImageUrls(imageUrls);

        Long id = heritageExperienceService.create(experience);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return ApiResponse.ok(data);
    }

    @GetMapping("/me")
    public ApiResponse<List<HeritageExperienceDTO>> myExperiences(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        String userOpenid = getCurrentUserOpenid();
        List<HeritageExperienceDTO> list = heritageExperienceService.listByUser(userOpenid, page, size);
        return ApiResponse.ok(list);
    }

    @GetMapping("/{id}")
    public ApiResponse<HeritageExperienceDTO> getOne(@PathVariable Long id) {
        String userOpenid = getCurrentUserOpenid();
        HeritageExperienceDTO dto = heritageExperienceService.findByIdAndUser(id, userOpenid);
        return ApiResponse.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        String userOpenid = getCurrentUserOpenid();
        heritageExperienceService.delete(id, userOpenid);
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        return ApiResponse.ok(data);
    }

    // 标记体验（简化接口）：POST /api/heritage/experience/{id}/experience
    @PostMapping("/{id}/experience")
    public ApiResponse<Map<String, Object>> markExperience(@PathVariable("id") Integer heritageId) {
        String userOpenid = getCurrentUserOpenid();
        boolean experienced = heritageExperienceService.hasExperience(userOpenid, heritageId);
        if (!experienced) {
            HeritageExperience experience = new HeritageExperience();
            experience.setUserId(userOpenid);
            experience.setHeritageId(heritageId);
            // 具体标题如果未传，由 Service 统一补默认值，避免数据库 NOT NULL 冲突
            heritageExperienceService.create(experience);
            experienced = true;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("experienced", experienced);
        data.put("isExperienced", experienced);
        return ApiResponse.ok(data);
    }

    // 获取体验状态：GET /api/heritage/experience/{id}/experience
    @GetMapping("/{id}/experience")
    public ApiResponse<Map<String, Object>> getExperienceStatus(@PathVariable("id") Integer heritageId) {
        String userOpenid = getCurrentUserOpenid();
        boolean experienced = heritageExperienceService.hasExperience(userOpenid, heritageId);
        Map<String, Object> data = new HashMap<>();
        data.put("experienced", experienced);
        data.put("isExperienced", experienced);
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

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
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