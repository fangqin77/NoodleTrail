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

 Date: 01/12/2025 21:04:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for wx_user
-- ----------------------------
DROP TABLE IF EXISTS `wx_user`;
CREATE TABLE `wx_user`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键ID（自增）',
  `openid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '微信用户唯一标识（小程序端获取，必存）',
  `unionid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信开放平台唯一标识（多端互通时用）',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信头像URL',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0=未知，1=男，2=女',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在城市',
  `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在国家',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在省份',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次登录时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息更新时间',
  `last_login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE COMMENT 'openid唯一索引',
  INDEX `idx_unionid`(`unionid` ASC) USING BTREE COMMENT 'unionid索引（多端互通）'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '微信小程序用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wx_user
-- ----------------------------
INSERT INTO `wx_user` VALUES (1, 'oJ16z7ccJ0iXAVs3t9z_8RA2MYGs', NULL, '晚夏初九', 'http://tmp/YOPw6j6nChCH9822a7948c1fd300de9fea3c67a5d3ff.jpeg', NULL, NULL, NULL, NULL, '2025-11-25 16:18:34', '2025-12-01 19:13:22', '2025-12-01 19:13:22');
INSERT INTO `wx_user` VALUES (2, 'mock_0b31UIGa1r6fJK02JNIa1i5JuW11UIGd', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-01 14:39:52', '2025-12-01 14:39:52', '2025-12-01 14:39:52');
INSERT INTO `wx_user` VALUES (3, 'mock_011g3E000aBcDe123456', NULL, '测试用户', 'https://example.com/avatar.jpg', 1, '西安', '中国', '陕西', '2025-12-01 14:58:18', '2025-12-01 15:40:43', '2025-12-01 15:40:43');
INSERT INTO `wx_user` VALUES (4, 'mock_', NULL, '测试用户', 'https://example.com/avatar.jpg', 1, '北京', '中国', '北京', '2025-12-01 15:00:43', '2025-12-01 15:40:21', '2025-12-01 15:40:21');

SET FOREIGN_KEY_CHECKS = 1;
