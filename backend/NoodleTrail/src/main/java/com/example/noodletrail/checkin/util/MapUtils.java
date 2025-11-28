package com.example.noodletrail.checkin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MapUtils {

    @Value("${amap.key:}")
    private String mapKey;

    @Value("${amap.reverse-geocode-url:https://restapi.amap.com/v3/geocode/regeo}")
    private String reverseGeocodeUrl;

    @Value("${amap.radius:500}")
    private int radius;

    @Value("${amap.extensions:all}")
    private String extensions;

    private final RestTemplate restTemplate;

    public MapUtils() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 高德地图逆地理编码：经纬度 → 详细地址（含门店/POI）
     * 高德 location 参数格式：经度,纬度
     */
    public Map<String, Object> reverseGeocode(double longitude, double latitude) {
        Map<String, Object> resultData = new HashMap<>();

        if (mapKey == null || mapKey.isEmpty()) {
            return resultData;
        }

        String url = String.format(
                "%s?location=%s,%s&key=%s&radius=%d&extensions=%s",
                reverseGeocodeUrl,
                longitude,  // 高德：先经度
                latitude,   // 后纬度
                mapKey,
                radius,
                extensions
        );

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map body = response.getBody();
        if (body == null) {
            return resultData;
        }

        Object statusObj = body.get("status");
        if (statusObj != null && "1".equals(statusObj.toString())) {
            Object regeocodeObj = body.get("regeocode");
            if (regeocodeObj instanceof Map) {
                Map<String, Object> regeocode = (Map<String, Object>) regeocodeObj;

                Object formattedAddress = regeocode.get("formatted_address");
                if (formattedAddress != null) {
                    resultData.put("formattedAddress", formattedAddress);
                }

                Object poisObj = regeocode.get("pois");
                if (poisObj instanceof Iterable) {
                    List<Map<String, Object>> formattedPoiList = new ArrayList<>();
                    for (Object o : (Iterable<?>) poisObj) {
                        if (o instanceof Map) {
                            Map<String, Object> poi = (Map<String, Object>) o;
                            Map<String, Object> formattedPoi = new HashMap<>();
                            formattedPoi.put("title", poi.get("name"));
                            formattedPoi.put("address", poi.get("address"));

                            Object locationObj = poi.get("location");
                            Map<String, Object> locationMap = new HashMap<>();
                            if (locationObj instanceof String) {
                                String[] lngLat = ((String) locationObj).split(",");
                                if (lngLat.length == 2) {
                                    try {
                                        locationMap.put("lng", Double.parseDouble(lngLat[0]));
                                        locationMap.put("lat", Double.parseDouble(lngLat[1]));
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }
                            formattedPoi.put("location", locationMap);

                            formattedPoiList.add(formattedPoi);
                        }
                    }
                    if (!formattedPoiList.isEmpty()) {
                        resultData.put("poiList", formattedPoiList);
                    }
                }
            }
        }

        return resultData;
    }
}
