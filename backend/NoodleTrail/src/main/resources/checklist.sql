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

 Date: 01/12/2025 21:07:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for checklist
-- ----------------------------
DROP TABLE IF EXISTS `checklist`;
CREATE TABLE `checklist`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户唯一标识（微信openid）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '清单标题',
  `template_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联模板ID（NULL表示自定义清单）',
  `items` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '清单项（JSON格式，例：[{\"name\":\"拍照\",\"status\":true},{\"name\":\"打卡\",\"status\":false}]）',
  `list_date` date NULL DEFAULT NULL COMMENT '清单业务日期（YYYY-MM-DD）',
  `order_index` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '同一天内的顺序（1-3）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_date`(`user_id` ASC, `list_date` ASC) USING BTREE,
  CONSTRAINT `fk_checklist_template` FOREIGN KEY (`template_id`) REFERENCES `checklist_template` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户清单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of checklist
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
