/*
 Navicat Premium Dump SQL

 Source Server         : project
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : shan

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 01/12/2025 21:04:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_checkin
-- ----------------------------
DROP TABLE IF EXISTS `user_checkin`;
CREATE TABLE `user_checkin`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '打卡记录ID（自增主键）',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户唯一标识（小程序openid）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '打卡文案（用户自定义）',
  `location_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '定位名称（如：华阴老腔表演馆、兵马俑景区三号坑门店）',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '打卡所在城市（如：西安、商洛）',
  `longitude` decimal(10, 6) NOT NULL COMMENT '经度（支持门店级精度）',
  `latitude` decimal(10, 6) NOT NULL COMMENT '纬度（支持门店级精度）',
  `image_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '打卡图片URL（多张用逗号分隔，如：url1,url2,url3）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间（自动填充）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '按用户查询打卡记录',
  INDEX `idx_location`(`longitude` ASC, `latitude` ASC) USING BTREE COMMENT '按地理位置查询附近打卡'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户打卡表（简化版，支持门店级定位）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_checkin
-- ----------------------------
INSERT INTO `user_checkin` VALUES (1, 'oJ16z7ccJ0iXAVs3t9z_8RA2MYGs', '111', '石家庄市裕华区塔南路169号', 114.531220, 38.006100, 'https://example.com/checkin/c3ef1428-c5df-4ae1-b317-cead3404dff0.png', '2025-11-25 16:18:35');
INSERT INTO `user_checkin` VALUES (2, 'oJ16z7ccJ0iXAVs3t9z_8RA2MYGs', '222', '石家庄市裕华区人民政府', 114.531220, 38.006100, 'https://example.com/checkin/37115686-7b0b-4571-81dd-07262105e2e0.png', '2025-11-25 16:19:43');
INSERT INTO `user_checkin` VALUES (3, 'oJ16z7ccJ0iXAVs3t9z_8RA2MYGs', '222', '石家庄市裕华区人民政府', 114.531220, 38.006100, 'https://example.com/checkin/00cc5b71-16b8-4eed-b8c1-805c3df7e473.png', '2025-11-25 16:19:43');
INSERT INTO `user_checkin` VALUES (4, 'oJ16z7ccJ0iXAVs3t9z_8RA2MYGs', '这家是我和同事们一致认为的西安面馆TOP 1！💯量大便宜又好吃，以前在新长安广场上班的时候，每次去南窑头吃饭都想要来一碗🤤简直是打工人的续命食堂～ 我每次必点招牌三合一，一上桌闻到味道就食指大开🥵，肉粒超大块，炖得软烂入味，配上番茄鸡蛋卤子绝了，面条较为湿软，拌开也不会粘连', '赵家面馆', 108.884964, 34.216685, 'https://example.com/checkin/c93488ce-41c4-451a-968c-eba9d5e074b4.jpg', '2025-11-25 16:33:45');

SET FOREIGN_KEY_CHECKS = 1;
