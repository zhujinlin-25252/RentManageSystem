/*
 Navicat Premium Dump SQL

 Source Server         : lianziqi59710
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : rent_manage

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 23/06/2026 22:10:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for contract_info
-- ----------------------------
DROP TABLE IF EXISTS `contract_info`;
CREATE TABLE `contract_info`  (
  `contract_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合同UUID',
  `contract_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合同编号（人类可读唯一编号）',
  `house_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房源ID',
  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租客ID',
  `landlord_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房东ID',
  `start_date` date NOT NULL COMMENT '租赁起始日期',
  `end_date` date NOT NULL COMMENT '租赁终止日期',
  `duration_months` int NOT NULL COMMENT '租赁总月数',
  `monthly_rent` decimal(10, 2) NOT NULL COMMENT '月租金快照（签约时锁定）',
  `deposit_amount` decimal(10, 2) NOT NULL COMMENT '押金总额',
  `total_amount` decimal(12, 2) NOT NULL COMMENT '合同总金额',
  `payment_method` tinyint NOT NULL DEFAULT 0 COMMENT '支付周期：0=月付，1=季付，2=半年付，3=年付',
  `content_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '合同正文内容（Markdown格式）',
  `tenant_signature` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租客电子签名URL',
  `landlord_signature` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房东电子签名URL',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=草稿，1=待租客签，2=待房东签，3=生效中，4=已到期，5=已解除',
  `pdf_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'PDF版合同URL',
  `signed_at` datetime NULL DEFAULT NULL COMMENT '签署完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`contract_id`) USING BTREE,
  UNIQUE INDEX `uk_contract_no`(`contract_no` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_landlord_id`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_status_date`(`status` ASC, `start_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租赁合同信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contract_info
-- ----------------------------

-- ----------------------------
-- Table structure for expense_record
-- ----------------------------
DROP TABLE IF EXISTS `expense_record`;
CREATE TABLE `expense_record`  (
  `expense_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '费用记录UUID',
  `expense_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '费用编号（唯一）',
  `house_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房屋ID',
  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租客ID（缴费责任人）',
  `expense_type` tinyint NOT NULL COMMENT '费用类型：1=水费，2=电费，3=燃气费，4=物业费，5=网费，6=其他',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额（元）',
  `period_start` date NOT NULL COMMENT '计费周期起始日',
  `period_end` date NOT NULL COMMENT '计费周期终止日',
  `reading_previous` decimal(10, 2) NULL DEFAULT NULL COMMENT '上期读数',
  `reading_current` decimal(10, 2) NULL DEFAULT NULL COMMENT '本期读数',
  `unit_price` decimal(8, 4) NULL DEFAULT NULL COMMENT '单价',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待缴，1=已缴，2=作废',
  `payment_order_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联的支付订单ID',
  `paid_at` datetime NULL DEFAULT NULL COMMENT '实际缴纳时间',
  `creator_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '录入人ID（通常是房东）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`expense_id`) USING BTREE,
  UNIQUE INDEX `uk_expense_no`(`expense_no` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_expense_type_status`(`expense_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '费用记录表（水电气物业等）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of expense_record
-- ----------------------------

-- ----------------------------
-- Table structure for house_image
-- ----------------------------
DROP TABLE IF EXISTS `house_image`;
CREATE TABLE `house_image`  (
  `image_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片记录ID（自增主键）',
  `house_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属房源ID → house_info.house_id',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL（本地路径或OSS地址）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前）',
  `is_cover` tinyint NOT NULL DEFAULT 0 COMMENT '是否为封面图：0=否，1=是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源图片表（一个房源对应多张图片）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of house_image
-- ----------------------------
INSERT INTO `house_image` VALUES (1, '2056359978864717826', '/uploads/house/2026/05/53ce0b2e5e2140a08cd6e1d1a0007885.jpg', 0, 1, '2026-05-18 21:26:03');
INSERT INTO `house_image` VALUES (2, '2065000005752578049', '/api/uploads/house/2026/06/a5ab9e1ca8a243c38f76d37318eb05f4.jpg', 0, 1, '2026-06-11 17:15:42');
INSERT INTO `house_image` VALUES (3, '2065023378771546114', '/api/uploads/house/2026/06/de38677e7f224b3ab3b8bc63f76753c0.jpg', 0, 1, '2026-06-11 18:48:35');
INSERT INTO `house_image` VALUES (4, '2065000005752578049', '/api/uploads/house/2026/06/45911ea5e8e8486dace5233d548085cb.jpg', 1, 0, '2026-06-11 18:59:57');

-- ----------------------------
-- Table structure for house_info
-- ----------------------------
DROP TABLE IF EXISTS `house_info`;
CREATE TABLE `house_info`  (
  `house_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房源UUID',
  `landlord_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房东用户ID → user_info.user_id',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `house_type` tinyint NOT NULL COMMENT '房源类型：1=整租，2=合租',
  `area` decimal(8, 2) NOT NULL COMMENT '建筑面积（㎡）',
  `rooms` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '房间数（室）',
  `halls` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '厅数',
  `bathrooms` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '卫生间数',
  `floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层描述（如\"6层/18层\"）',
  `orientation` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '朝向（东/南/西/北/南北/东南等）',
  `rent_monthly` decimal(10, 2) NOT NULL COMMENT '月租金（元）',
  `deposit` decimal(10, 2) NULL DEFAULT NULL COMMENT '押金（元）',
  `payment_type` tinyint NOT NULL DEFAULT 0 COMMENT '推荐支付方式：0=月付，1=季付，2=半年付，3=年付',
  `facilities` json NULL COMMENT '配套设施JSON数组',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房源标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细描述（Markdown格式）',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待审核，1=已发布，2=已出租，3=下架，4=审核拒绝',
  `audit_user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注/拒绝原因',
  `view_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏人数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`house_id`) USING BTREE,
  INDEX `idx_landlord_id`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_location_search`(`city` ASC, `district` ASC, `status` ASC, `rent_monthly` ASC) USING BTREE,
  INDEX `idx_rent_monthly`(`rent_monthly` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of house_info
-- ----------------------------
INSERT INTO `house_info` VALUES ('2053521043478253569', 'usr-landlord-001', '广东', '广州', '海珠', '广州塔尖', NULL, NULL, 1, 200.00, 1, 0, 1, '', '', 1.00, 1.00, 0, '[\"bed\"]', '超级精装猪窝', '', '', 1, NULL, NULL, NULL, 19, 0, '2026-05-11 01:02:24', '2026-05-18 20:58:58', 0);
INSERT INTO `house_info` VALUES ('2056359978864717826', 'usr-landlord-001', '广东', '广州', 'meizhou', '广州塔尖', NULL, NULL, 1, 5.00, 1, 0, 1, '', '南北', 1.00, 111.00, 3, '[\"wifi\", \"ac\", \"washer\", \"fridge\", \"heater\", \"tv\", \"kitchen\", \"balcony\", \"parking\", \"elevator\", \"wardrobe\", \"bed\"]', '123456', '123', '/uploads/house/2026/05/53ce0b2e5e2140a08cd6e1d1a0007885.jpg', 2, 'sys-admin-001', '2026-05-19 15:27:00', NULL, 33, 0, '2026-05-18 21:03:19', '2026-05-19 15:47:23', 0);
INSERT INTO `house_info` VALUES ('2056365700679692289', 'usr-landlord-001', '广东', '广州', '12345', '广州塔尖', NULL, NULL, 1, 5.00, 1, 0, 1, '', '', 1.00, NULL, 0, '[\"wifi\", \"ac\", \"wardrobe\", \"bed\"]', '22222222', '', '/uploads/house/2026/05/53ce0b2e5e2140a08cd6e1d1a0007885.jpg', 1, NULL, NULL, NULL, 6, 0, '2026-05-18 21:26:03', '2026-05-19 15:46:24', 1);
INSERT INTO `house_info` VALUES ('2065000005752578049', 'usr-landlord-001', '北京市', '市辖区', '东城区', '222', NULL, NULL, 1, 222.00, 1, 0, 1, '', '', 222.00, 222.00, 0, '[]', '22222', '222222', '/api/uploads/house/2026/06/a5ab9e1ca8a243c38f76d37318eb05f4.jpg', 2, 'sys-admin-001', '2026-06-11 19:00:23', '房源图片模糊或不清晰', 8, 0, '2026-06-11 17:15:42', '2026-06-11 17:15:42', 0);
INSERT INTO `house_info` VALUES ('2065023378771546114', 'usr-landlord-001', '天津市', '市辖区', '河东区', '333', NULL, NULL, 1, 333.00, 1, 0, 1, '', '', 333.00, NULL, 0, '[]', '333333', '333333', '/api/uploads/house/2026/06/de38677e7f224b3ab3b8bc63f76753c0.jpg', 2, 'sys-admin-001', '2026-06-11 18:59:24', NULL, 19, 0, '2026-06-11 18:48:34', '2026-06-11 18:48:34', 0);

-- ----------------------------
-- Table structure for notice_info
-- ----------------------------
DROP TABLE IF EXISTS `notice_info`;
CREATE TABLE `notice_info`  (
  `notice_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告UUID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告正文（支持Markdown富文本）',
  `notice_type` tinyint NOT NULL DEFAULT 0 COMMENT '类型：0=系统公告，1=活动通知，2=紧急通知',
  `scope` tinyint NOT NULL DEFAULT 0 COMMENT '发布范围：0=全体用户，1=指定区域，2=指定房屋',
  `scope_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '范围具体值（区域名或房屋ID列表JSON）',
  `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否，1=是',
  `publisher_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布人ID',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '阅读次数统计',
  `publish_at` datetime NOT NULL COMMENT '发布时间（支持定时发布）',
  `expired_at` datetime NULL DEFAULT NULL COMMENT '过期时间（NULL表示永不过期）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=草稿，1=已发布，2=已撤回',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`notice_id`) USING BTREE,
  INDEX `idx_publish_at`(`publish_at` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice_info
-- ----------------------------
INSERT INTO `notice_info` VALUES ('notice-welcome-001', '🏠 欢迎使用青年品质租房管理系统', '本系统致力于为广大青年群体提供**高品质、透明化**的租房服务。\n\n## 我们提供\n\n- 📋 **房源浏览与搜索**：按区域、价格、户型筛选心仪房源\n- 📝 **在线签约**：电子合同签署，安全便捷\n- 💰 **费用管理**：水电物业在线缴费，账单一目了然\n- 🔧 **报修服务**：一键报修，快速响应\n\n如有任何问题，请联系在线客服。', 0, 0, NULL, 0, 'sys-admin-001', 0, '2026-05-11 00:43:58', NULL, 1, '2026-05-11 00:43:58', '2026-05-11 00:43:58');

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志自增ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作用户ID（匿名接口可为空）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作用户名（冗余存一份，防止用户删除后无法追溯）',
  `module` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作模块：auth/user/house/contract/payment/repair/expense/notice/admin',
  `action` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作动作：login/logout/register/create/update/delete/audit/sign/pay/publish/offshelf...',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方法全限定名',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求URL路径',
  `http_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'HTTP动词：GET/POST/PUT/DELETE',
  `ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端IP地址',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数（敏感字段已脱敏后的JSON）',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应结果摘要（调试用）',
  `execution_time` bigint NULL DEFAULT NULL COMMENT '接口执行耗时（毫秒）',
  `success` tinyint NOT NULL DEFAULT 1 COMMENT '是否成功：0=失败，1=成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息（仅失败时记录）',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端User-Agent标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志产生时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_module_action`(`module` ASC, `action` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作审计日志表（合规必备，不设外键）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for payment_order
-- ----------------------------
DROP TABLE IF EXISTS `payment_order`;
CREATE TABLE `payment_order`  (
  `order_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单UUID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号（唯一）',
  `contract_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联合同ID',
  `payer_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '付款人ID',
  `payee_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收款人ID',
  `order_type` tinyint NOT NULL COMMENT '订单类型：1=押金，2=租金，3=物业费，4=水电费，5=其他',
  `amount` decimal(10, 2) NOT NULL COMMENT '订单金额（元）',
  `period_start` date NULL DEFAULT NULL COMMENT '费用周期起始日',
  `period_end` date NULL DEFAULT NULL COMMENT '费用周期终止日',
  `pay_channel` tinyint NULL DEFAULT NULL COMMENT '支付渠道：1=支付宝，2=微信支付，3=银行卡转账',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '第三方交易流水号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待支付，1=支付中，2=已支付，3=已退款，4=已取消',
  `paid_at` datetime NULL DEFAULT NULL COMMENT '实际支付成功时间',
  `expire_at` datetime NOT NULL COMMENT '订单超时时间（30分钟未支付自动取消）',
  `refund_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '已退款金额',
  `refund_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退款原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_contract_id`(`contract_id` ASC) USING BTREE,
  INDEX `idx_payer_id`(`payer_id` ASC) USING BTREE,
  INDEX `idx_status_create`(`status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment_order
-- ----------------------------
INSERT INTO `payment_order` VALUES ('2069328349570772994', 'PO202606231554591283', NULL, 'usr-tenant-001', 'usr-landlord-001', 1, 333.00, NULL, NULL, NULL, NULL, 2, '2026-06-23 15:55:16', '2026-06-24 15:54:59', NULL, NULL, '2026-06-23 15:54:59', '2026-06-23 15:54:59');
INSERT INTO `payment_order` VALUES ('2069336208643706882', 'PO202606231626131247', NULL, 'usr-tenant-001', 'usr-landlord-001', 1, 444.00, NULL, NULL, NULL, NULL, 2, '2026-06-23 16:26:29', '2026-06-24 16:26:13', NULL, NULL, '2026-06-23 16:26:13', '2026-06-23 16:26:13');

-- ----------------------------
-- Table structure for rent_order
-- ----------------------------
DROP TABLE IF EXISTS `rent_order`;
CREATE TABLE `rent_order`  (
  `order_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单UUID（雪花算法）',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号（业务唯一，如 RO202605190001）',
  `payment_order_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联支付单ID',
  `house_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房源ID → house_info.house_id',
  `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租客用户ID → user_info.user_id',
  `landlord_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房东用户ID → user_info.user_id',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '订单标题（房源标题）',
  `rent_monthly` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '月租金（元）',
  `deposit` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '押金（元）',
  `start_date` date NOT NULL COMMENT '租期开始日期',
  `end_date` date NOT NULL COMMENT '租期结束日期',
  `total_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额（元）',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '联系电话',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '备注/留言',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待确认 1=已确认/租住中 2=已完成 3=已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_landlord_id`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租房订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rent_order
-- ----------------------------
INSERT INTO `rent_order` VALUES ('2056655156498370562', 'RO202605191636140001', NULL, '2056359978864717826', 'usr-tenant-001', 'usr-landlord-001', '123456', 1.00, 111.00, '2026-05-19', '2026-05-20', 112.00, 'lzq', '13812344321', '', 1, '2026-05-19 16:36:15', '2026-05-19 16:36:15', 0);
INSERT INTO `rent_order` VALUES ('2069327191661813761', 'RO202606231550230001', NULL, '2065000005752578049', 'usr-tenant-001', 'usr-landlord-001', '22222', 222.00, 222.00, '2026-06-23', '2026-06-29', 444.00, '123', '13612312312', '', 3, '2026-06-23 15:50:23', '2026-06-23 15:50:23', 0);
INSERT INTO `rent_order` VALUES ('2069327941829898242', 'RO202606231553220001', '2069328349570772994', '2065023378771546114', 'usr-tenant-001', 'usr-landlord-001', '333333', 333.00, 0.00, '2026-06-29', '2026-06-30', 333.00, '123', '13922333322', '', 2, '2026-06-23 15:53:22', '2026-06-23 15:53:22', 0);
INSERT INTO `rent_order` VALUES ('2069335969228640258', 'RO202606231625160001', '2069336208643706882', '2065000005752578049', 'usr-tenant-001', 'usr-landlord-001', '22222', 222.00, 222.00, '2026-06-23', '2026-06-30', 444.00, '123', '13612341234', '', 2, '2026-06-23 16:25:16', '2026-06-23 16:25:16', 0);

-- ----------------------------
-- Table structure for repair_ticket
-- ----------------------------
DROP TABLE IF EXISTS `repair_ticket`;
CREATE TABLE `repair_ticket`  (
  `ticket_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单UUID',
  `ticket_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单编号（唯一）',
  `house_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房屋ID',
  `reporter_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报修人ID（通常是租客）',
  `handler_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理人ID（客服或维修人员）',
  `problem_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题分类（如：水电、门窗、家电、管道、其他）',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报修标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题描述（支持图文混排Markdown）',
  `images` json NULL COMMENT '现场照片URL数组JSON',
  `priority` tinyint NOT NULL DEFAULT 1 COMMENT '优先级：1=普通（48h内响应），2=紧急（24h），3=非常紧急（4h）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待处理，1=处理中，2=已完成，3=已关闭',
  `handler_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理回复内容',
  `resolved_at` datetime NULL DEFAULT NULL COMMENT '解决完成时间',
  `closed_at` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `satisfaction` tinyint NULL DEFAULT NULL COMMENT '满意度评分（1-5分，NULL表示未评价）',
  `feedback` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评价反馈文字',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_id`) USING BTREE,
  UNIQUE INDEX `uk_ticket_no`(`ticket_no` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_handler_status`(`handler_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '报修工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_ticket
-- ----------------------------

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收藏用户ID',
  `house_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被收藏的房源ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_house`(`user_id` ASC, `house_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏关系表（多对多中间表）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------
INSERT INTO `user_favorite` VALUES (1, 'usr-tenant-001', '2065023378771546114', '2026-06-23 15:14:56');

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户UUID（雪花算法生成）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号（中国大陆11位）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱地址',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt加密后的密码哈希值',
  `payment_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付密码(BCrypt)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '用户昵称',
  `real_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名（AES加密存储）',
  `id_card` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号（AES加密存储）',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像图片URL',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别：0=未知，1=男，2=女',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色：0=租客，1=房东，2=管理员，3=客服',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '账户状态：0=未激活，1=正常，2=禁用',
  `is_verified` tinyint NOT NULL DEFAULT 0 COMMENT '实名认证：0=未认证，1=已认证',
  `verify_time` datetime NULL DEFAULT NULL COMMENT '认证通过时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP（兼容IPv6）',
  `login_fail_count` int NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `login_lock_until` datetime NULL DEFAULT NULL COMMENT '账户锁定截止时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0=正常，1=已删除',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_role_status`(`role` ASC, `status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES ('2062146733819920386', '13812341234', NULL, '$2a$10$xXkFb5Pu57p1C94tvocHOuKnO8shShRkA80G6yqrW8b7456485JTS', NULL, '59710', NULL, NULL, NULL, 0, 0, 1, 0, NULL, '2026-06-03 20:17:53', NULL, 0, NULL, '2026-06-03 20:17:49', '2026-06-03 20:17:49', 0);
INSERT INTO `user_info` VALUES ('sys-admin-001', '13800000000', 'admin@rent.com', '$2b$10$TG2mTmxRfMrENfP79YKIeuGxRjAnPYsxz8nluVxxwig4aRvTXb4p6', NULL, '系统管理员', NULL, NULL, NULL, 0, 2, 1, 0, NULL, '2026-06-23 21:40:35', NULL, 0, NULL, '2026-05-11 00:43:58', '2026-05-11 00:51:37', 0);
INSERT INTO `user_info` VALUES ('usr-kefu-001', '13833333333', 'kefu@rent.com', '$2b$10$TG2mTmxRfMrENfP79YKIeuGxRjAnPYsxz8nluVxxwig4aRvTXb4p6', NULL, '小王客服', NULL, NULL, NULL, 0, 3, 1, 0, NULL, '2026-05-19 15:27:49', NULL, 0, NULL, '2026-05-11 00:43:58', '2026-05-11 00:51:34', 0);
INSERT INTO `user_info` VALUES ('usr-landlord-001', '13822222222', 'lisi@rent.com', '$2b$10$TG2mTmxRfMrENfP79YKIeuGxRjAnPYsxz8nluVxxwig4aRvTXb4p6', NULL, '李房东', NULL, NULL, NULL, 0, 1, 1, 0, NULL, '2026-06-23 21:40:47', NULL, 0, NULL, '2026-05-11 00:43:58', '2026-05-11 00:51:31', 0);
INSERT INTO `user_info` VALUES ('usr-tenant-001', '13811111111', '992500702@qq.com', '$2b$10$TG2mTmxRfMrENfP79YKIeuGxRjAnPYsxz8nluVxxwig4aRvTXb4p6', '$2a$10$KQ7rZgWztDo5zC8JFalcWe.3U.JLJhRAljwZQf4DpT82Y5tflrPeO', '张三', NULL, NULL, NULL, 1, 0, 1, 0, NULL, '2026-06-23 21:42:52', NULL, 0, NULL, '2026-05-11 00:43:58', '2026-06-23 16:25:49', 0);

SET FOREIGN_KEY_CHECKS = 1;
