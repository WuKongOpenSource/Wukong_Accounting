/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : wk_open_finance

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 24/10/2023 20:41:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for wk_admin_file
-- ----------------------------
DROP TABLE IF EXISTS `wk_admin_file`;
CREATE TABLE `wk_admin_file`  (
  `file_id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '附件名称',
  `size` bigint NOT NULL COMMENT '附件大小（字节）',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件真实路径',
  `file_type` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT 'file' COMMENT '文件类型,file,img',
  `type` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '1 本地 2 阿里云oss',
  `source` int NULL DEFAULT NULL COMMENT '来源 0 默认 1 admin 2 crm 3 work 4 oa 5 进销存 6 hrm',
  `batch_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '批次id',
  `is_public` int NULL DEFAULT 0 COMMENT '1 公有访问 0 私有访问',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人ID',
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `batch_id`(`batch_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '附件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_admin_file
-- ----------------------------

-- ----------------------------
-- Table structure for wk_admin_menu
-- ----------------------------
DROP TABLE IF EXISTS `wk_admin_menu`;
CREATE TABLE `wk_admin_menu`  (
  `menu_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` int UNSIGNED NULL DEFAULT 0 COMMENT '上级菜单ID',
  `menu_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单名称',
  `realm` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '权限标识',
  `realm_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限URL',
  `realm_module` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属模块',
  `menu_type` int NULL DEFAULT NULL COMMENT '菜单类型  1目录 2 菜单 3 按钮 4特殊',
  `sort` int UNSIGNED NULL DEFAULT 0 COMMENT '排序（同级有效）',
  `status` int NULL DEFAULT 1 COMMENT '状态 1 启用 0 禁用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单说明',
  `project_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '1普通项目 2敏捷项目',
  PRIMARY KEY (`menu_id`) USING BTREE,
  INDEX `menu_id`(`menu_id` ASC) USING BTREE,
  INDEX `parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `realm`(`realm` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1341 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_admin_menu
-- ----------------------------
INSERT INTO `wk_admin_menu` VALUES (3, 0, '全部', 'manage', NULL, NULL, 1, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (943, 0, '全部', 'finance', NULL, NULL, 1, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (944, 943, '科目', 'subject', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (945, 943, '币别', 'currency', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (946, 943, '凭证字', 'voucherWord', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (947, 943, '系统参数', 'systemParam', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (948, 943, '财务初始余', 'financialInitial', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (949, 943, '凭证', 'voucher', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (950, 943, '凭证汇总表', 'voucherSummary', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (951, 943, '总账', 'generalLedger', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (952, 943, '明细账', 'subLedger', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (953, 943, '科目余额表', 'accountBalance', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (954, 943, '多栏账', 'multiColumn', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (955, 943, '资产负债表', 'balanceSheet', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (956, 943, '利润表', 'profit', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (957, 943, '现金流量表', 'cashFlow', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (958, 943, '结账', 'checkOut', NULL, NULL, 1, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (959, 944, '编辑', 'update', NULL, NULL, 3, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (960, 944, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (961, 944, '新增', 'save', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (962, 944, '导入', 'import', NULL, NULL, 3, 3, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (963, 944, '删除', 'delete', NULL, NULL, 3, 4, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (964, 944, '导出', 'export', NULL, NULL, 3, 5, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (965, 945, '编辑', 'update', NULL, NULL, 3, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (966, 945, '新增', 'save', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (967, 945, '删除', 'delete', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (968, 946, '编辑', 'update', NULL, NULL, 3, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (969, 946, '新增', 'save', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (970, 946, '删除', 'delete', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (971, 946, '排序', 'sort', NULL, NULL, 3, 3, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (972, 947, '编辑', 'update', NULL, NULL, 3, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (973, 947, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (974, 948, '编辑', 'update', NULL, NULL, 3, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (975, 948, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (976, 948, '导出', 'export', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (977, 949, '新增', 'save', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (978, 949, '编辑', 'update', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (979, 949, '查看', 'read', NULL, NULL, 3, 3, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (980, 949, '删除', 'delete', NULL, NULL, 3, 4, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (981, 949, '导出', 'export', NULL, NULL, 3, 5, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (982, 949, '打印', 'print', NULL, NULL, 3, 6, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (983, 949, '导入', 'import', NULL, NULL, 3, 7, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (984, 949, '插入', 'insert', NULL, NULL, 3, 8, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (985, 949, '整理', 'arrangement', NULL, NULL, 3, 9, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (986, 950, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (987, 950, '导出', 'export', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (989, 951, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (990, 951, '导出', 'export', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (992, 952, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (993, 952, '导出', 'export', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (995, 953, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (996, 953, '导出', 'export', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (998, 954, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (999, 954, '导出', 'export', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1000, 955, '查看', 'read', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1001, 955, '打印', 'print', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1002, 955, '导出', 'export', NULL, NULL, 3, 3, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1003, 955, '编辑', 'update', NULL, NULL, 3, 4, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1004, 956, '编辑', 'update', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1005, 956, '查看', 'read', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1006, 956, '导出', 'export', NULL, NULL, 3, 3, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1007, 956, '打印', 'print', NULL, NULL, 3, 4, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1008, 957, '打印', 'print', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1009, 957, '导出', 'export', NULL, NULL, 3, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1010, 957, '查看', 'read', NULL, NULL, 3, 3, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1011, 957, '编辑', 'update', NULL, NULL, 3, 4, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1012, 958, '生成结转凭证', 'generate', NULL, NULL, 4, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1013, 958, '结转损益', 'profitAndLoss', NULL, NULL, 4, 2, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1014, 958, '结账', 'checkOut', NULL, NULL, 4, 3, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1015, 958, '反结账', 'cancelClosing', NULL, NULL, 4, 4, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1016, 3, '财务管理', 'finance', NULL, NULL, 1, 12, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1017, 1016, '账套管理', 'accountSet', NULL, NULL, 3, 1, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1018, 949, '审核', 'examine', NULL, NULL, 3, 10, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1019, 949, '反审核', 'noExamine', NULL, NULL, 3, 11, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1154, 1151, '项目协同配置', 'editCoordination', NULL, NULL, 2, 0, 1, NULL, '1');
INSERT INTO `wk_admin_menu` VALUES (1180, 1119, '自定义角色设置', 'setRole', NULL, NULL, 3, 0, 1, NULL, NULL);
INSERT INTO `wk_admin_menu` VALUES (1181, 1119, '状态设置', 'setStatus', NULL, NULL, 3, 0, 1, NULL, NULL);

-- ----------------------------
-- Table structure for wk_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `wk_admin_role`;
CREATE TABLE `wk_admin_role`  (
  `role_id` bigint NOT NULL,
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `role_type` int NULL DEFAULT NULL COMMENT '0、自定义角色1、管理角色 2、客户管理角色 3、人事角色 4、财务角色 5、项目角色 8、项目自定义角色',
  `sorting` int NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `status` int NULL DEFAULT 1 COMMENT '1 启用 0 禁用',
  `data_type` int NOT NULL DEFAULT 5 COMMENT '数据权限 1、本人，2、本人及下属，3、本部门，4、本部门及下属部门，5、全部 ',
  `is_hidden` int NOT NULL DEFAULT 1 COMMENT '0 隐藏 1 不隐藏',
  `label` int NULL DEFAULT NULL COMMENT '1 系统项目管理员角色 2 项目管理角色 3 项目编辑角色 4 项目只读角色',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人ID',
  PRIMARY KEY (`role_id`) USING BTREE,
  INDEX `role_type`(`role_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_admin_role
-- ----------------------------
INSERT INTO `wk_admin_role` VALUES (1559019593879896074, '超级管理员', 1, 0, 'admin', 1, 5, 1, 5, '2022-08-15 11:30:10', 1559019593470803969, '2022-08-15 11:30:10', 1559019593470803969);
INSERT INTO `wk_admin_role` VALUES (1559019593896673294, '财务管理员', 1, 0, NULL, 1, 5, 1, 14, '2022-08-15 11:30:10', 1559019593470803969, '2022-08-15 11:30:10', 1559019593470803969);
INSERT INTO `wk_admin_role` VALUES (1559019597340197142, '主管', 4, 0, '1', 1, 5, 1, NULL, '2022-08-15 11:30:12', 1559019593470803969, '2022-08-15 11:30:12', 1559019593470803969);
INSERT INTO `wk_admin_role` VALUES (1559019597340197218, '查看者', 4, 0, '1', 1, 5, 1, 4, '2022-08-15 11:30:12', 1559019593470803969, '2022-08-15 11:30:12', 1559019593470803969);
INSERT INTO `wk_admin_role` VALUES (1559019597340197255, '会计', 4, 0, '1', 1, 5, 1, NULL, '2022-08-15 11:30:12', 1559019593470803969, '2022-08-15 11:30:12', 1559019593470803969);
INSERT INTO `wk_admin_role` VALUES (1559019597340197331, '默认角色', 1, 0, 'cp', 1, 2, 1, 5, '2022-08-15 11:30:12', 1559019593470803969, '2022-08-15 11:30:12', 1559019593470803969);

-- ----------------------------
-- Table structure for wk_admin_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `wk_admin_role_menu`;
CREATE TABLE `wk_admin_role_menu`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int NOT NULL COMMENT '角色ID',
  `menu_id` int NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `role_id`(`role_id` ASC) USING BTREE,
  INDEX `menu_id`(`menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2300834 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '角色菜单对应关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_admin_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for wk_admin_user_role
-- ----------------------------
DROP TABLE IF EXISTS `wk_admin_user_role`;
CREATE TABLE `wk_admin_user_role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` int NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19221 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '用户角色对应关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_admin_user_role
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_account_set
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_account_set`;
CREATE TABLE `wk_finance_account_set`  (
  `account_id` bigint NOT NULL COMMENT '账套ID',
  `company_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司编码',
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司名称',
  `company_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公司简介',
  `industry` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所在行业',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所在地',
  `legal_representative` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人代表',
  `id_num` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '身份证号',
  `business_license_num` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `organization_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组织机构代码',
  `remark` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `contacts` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `office_telephone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '办公电话',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `fax_num` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '传真号码',
  `qq_num` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'qq号码',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'email',
  `other` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其他',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详细地址',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `currency_id` bigint NULL DEFAULT NULL COMMENT '本位币id',
  `start_time` datetime NULL DEFAULT NULL COMMENT '启用期间',
  `bookkeeper_id` bigint NULL DEFAULT NULL COMMENT '会计制度',
  `status` int NULL DEFAULT 0 COMMENT '是否有账套（0 没有  1 有）',
  PRIMARY KEY (`account_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '账套表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_account_set
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_account_user
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_account_user`;
CREATE TABLE `wk_finance_account_user`  (
  `id` bigint NOT NULL,
  `account_id` bigint NOT NULL COMMENT '账套ID',
  `user_id` bigint NOT NULL COMMENT '员工ID',
  `is_default` int NOT NULL DEFAULT 0 COMMENT '是否默认（0 不是  1 是）',
  `is_founder` int NOT NULL DEFAULT 0 COMMENT '是否是创始人（0 不是  1 是）',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色id',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '账套员工对应关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_account_user
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_adjuvant
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_adjuvant`;
CREATE TABLE `wk_finance_adjuvant`  (
  `adjuvant_id` bigint NOT NULL,
  `adjuvant_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '辅助核算名称',
  `adjuvant_type` int NULL DEFAULT NULL COMMENT '是否是固定核算 1.是 0.否',
  `create_user_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `account_id` bigint NOT NULL COMMENT '账套表',
  `label` int NOT NULL COMMENT '标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`adjuvant_id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE,
  INDEX `account_id`(`account_id` ASC) USING BTREE,
  INDEX `idx_companyid_accountid`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '辅助核算表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_adjuvant
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_adjuvant_carte
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_adjuvant_carte`;
CREATE TABLE `wk_finance_adjuvant_carte`  (
  `carte_id` bigint NOT NULL,
  `carte_number` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '编码',
  `carte_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `adjuvant_id` bigint NULL DEFAULT NULL COMMENT '核算表id',
  `status` int NULL DEFAULT NULL COMMENT '状态 1.正常启用 2.正常禁用 3.删除',
  `create_user_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `remark` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `specification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格（存货）',
  `unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位（存货）',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`carte_id`) USING BTREE,
  INDEX `adjuvant_id`(`adjuvant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '辅助核算关联类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_adjuvant_carte
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_assist
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_assist`;
CREATE TABLE `wk_finance_assist`  (
  `assist_id` bigint NOT NULL,
  `subject_id` bigint NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`assist_id`) USING BTREE,
  INDEX `wk_finance_assist_subject_id_assist_id_index`(`subject_id` ASC, `assist_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '辅助核算辅助表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_assist
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_assist_adjuvant
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_assist_adjuvant`;
CREATE TABLE `wk_finance_assist_adjuvant`  (
  `assist_adjuvant_id` bigint NOT NULL,
  `assist_id` bigint NULL DEFAULT NULL,
  `label` int NULL DEFAULT NULL COMMENT '标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义',
  `account_id` bigint NULL DEFAULT NULL,
  `relation_id` bigint NULL DEFAULT NULL COMMENT '关联模块id',
  `label_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `adjuvant_id` bigint NULL DEFAULT NULL COMMENT '核算表id',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`assist_adjuvant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '辅助核算关联模块表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_assist_adjuvant
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_balance_sheet_config
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_balance_sheet_config`;
CREATE TABLE `wk_finance_balance_sheet_config`  (
  `id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `account_id` bigint NULL DEFAULT NULL,
  `grade` int NULL DEFAULT NULL COMMENT '1 等级 第几级',
  `row_Id` int NULL DEFAULT NULL COMMENT '行ID',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `wk_finance_balance_sheet_config_company_id_account_id_index`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '资产负债表配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_balance_sheet_config
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_balance_sheet_report
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_balance_sheet_report`;
CREATE TABLE `wk_finance_balance_sheet_report`  (
  `id` bigint NOT NULL,
  `from_period` int NOT NULL COMMENT '开始账期：格式 yyyyMM',
  `to_period` int NULL DEFAULT NULL COMMENT '结束账期：格式 yyyyMM',
  `type` int NULL DEFAULT 1 COMMENT '类型 1 月度 2 季度',
  `grade` int NULL DEFAULT NULL COMMENT '1 等级 第几级',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `initial_period` decimal(10, 2) NULL DEFAULT NULL COMMENT '年初数',
  `end_period` decimal(10, 2) NULL DEFAULT NULL COMMENT '期末数',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `account_id` bigint NULL DEFAULT NULL,
  `settled` tinyint(1) NULL DEFAULT 0 COMMENT '是否结账',
  `row_Id` int NULL DEFAULT NULL COMMENT '行ID',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `wk_finance_balance_sheet_report_company_id_account_id_index`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '资产负债表报表数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_balance_sheet_report
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_cash_flow_statement_config
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_cash_flow_statement_config`;
CREATE TABLE `wk_finance_cash_flow_statement_config`  (
  `id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `category` int NULL DEFAULT NULL COMMENT '现金流量表分类',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `account_id` bigint NULL DEFAULT NULL COMMENT '账套表',
  `grade` int NULL DEFAULT NULL COMMENT '1 等级 第几级',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `cash_flow_statement_config_company_id_account_id_index`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '现金流量表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_cash_flow_statement_config
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_cash_flow_statement_extend_config
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_cash_flow_statement_extend_config`;
CREATE TABLE `wk_finance_cash_flow_statement_extend_config`  (
  `id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `category` int NULL DEFAULT NULL COMMENT '表格标识',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `type` int NULL DEFAULT NULL COMMENT '类型：0 支持用户手动配置 1 固定公式',
  `month_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '月值',
  `year_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '年值',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `account_id` bigint NULL DEFAULT NULL,
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `grade` int NULL DEFAULT NULL COMMENT '等级',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `cash_flow_statement_extend_config_company_id_account_id_index`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '现金流量扩展表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_cash_flow_statement_extend_config
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_cash_flow_statement_extend_data
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_cash_flow_statement_extend_data`;
CREATE TABLE `wk_finance_cash_flow_statement_extend_data`  (
  `id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `category` int NULL DEFAULT NULL COMMENT '现金流量表分类',
  `month_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '月值',
  `year_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '年值',
  `from_period` int NULL DEFAULT NULL COMMENT '开始账期：格式 yyyyMM',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `account_id` bigint NULL DEFAULT NULL,
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `to_period` int NULL DEFAULT NULL COMMENT '结束账期：格式 yyyyMM',
  `type` int NULL DEFAULT 1 COMMENT '类型 1 月度 2 季度',
  `grade` int NULL DEFAULT NULL COMMENT '等级',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `from_period_to_period_company_id_account_id_index`(`from_period` ASC, `to_period` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '现金流量扩展表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_cash_flow_statement_extend_data
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_cash_flow_statement_report
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_cash_flow_statement_report`;
CREATE TABLE `wk_finance_cash_flow_statement_report`  (
  `id` bigint NOT NULL,
  `from_period` int NOT NULL COMMENT '开始账期：格式 yyyyMM',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `month_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '月值',
  `year_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '年值',
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `category` int NULL DEFAULT NULL COMMENT '现金流量表分类',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `account_id` bigint NULL DEFAULT NULL COMMENT '账套表',
  `to_period` int NULL DEFAULT NULL COMMENT '结束账期：格式 yyyyMM',
  `type` int NULL DEFAULT 1 COMMENT '类型 1 月度 2 季度',
  `grade` int NULL DEFAULT NULL COMMENT '1 等级 第几级',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `from_period_to_period_company_id_account_id_index`(`from_period` ASC, `to_period` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '现金流量表数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_cash_flow_statement_report
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_certificate
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_certificate`;
CREATE TABLE `wk_finance_certificate`  (
  `certificate_id` bigint NOT NULL,
  `voucher_id` bigint NULL DEFAULT NULL COMMENT '凭证字',
  `certificate_num` int NULL DEFAULT NULL COMMENT '凭证号',
  `certificate_time` datetime NULL DEFAULT NULL COMMENT '凭证日期',
  `batch_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '批次 比如附件批次',
  `file_num` int NULL DEFAULT 0 COMMENT '附单据张数',
  `debtor_balance` decimal(18, 2) NULL DEFAULT NULL COMMENT '合计借方金额',
  `credit_balance` decimal(18, 2) NULL DEFAULT NULL COMMENT '合计贷方金额',
  `total` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '合计',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '制单人',
  `create_time` datetime NULL DEFAULT NULL,
  `check_status` int NULL DEFAULT 0 COMMENT '0待审核、1通过、2拒绝、3审核中 4:撤回 5 未提交 6 创建 7 已删除 8 作废',
  `examine_user_id` bigint NULL DEFAULT NULL COMMENT '审核人id',
  `account_id` bigint NULL DEFAULT NULL,
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`certificate_id`) USING BTREE,
  INDEX `voucher_id`(`voucher_id` ASC, `certificate_num` ASC, `check_status` ASC, `account_id` ASC) USING BTREE,
  INDEX `account_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '凭证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_certificate
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_certificate_association
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_certificate_association`;
CREATE TABLE `wk_finance_certificate_association`  (
  `association_id` bigint NOT NULL,
  `detail_id` bigint NULL DEFAULT NULL COMMENT '凭证详情id',
  `create_user_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `relation_id` bigint NULL DEFAULT NULL COMMENT '关联模块id',
  `label` int NOT NULL COMMENT '标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义',
  `adjuvant_id` bigint NULL DEFAULT NULL COMMENT '核算表id',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`association_id`) USING BTREE,
  INDEX `relation_id`(`relation_id` ASC) USING BTREE,
  INDEX `account_id`(`account_id` ASC, `relation_id` ASC, `adjuvant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '凭证详情关联标签类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_certificate_association
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_certificate_detail
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_certificate_detail`;
CREATE TABLE `wk_finance_certificate_detail`  (
  `detail_id` bigint NOT NULL,
  `digest_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '摘要内容',
  `subject_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会计科目内容',
  `quantity` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数量',
  `debtor_balance` decimal(18, 2) NULL DEFAULT NULL COMMENT '借方金额',
  `credit_balance` decimal(18, 2) NULL DEFAULT NULL COMMENT '贷方金额',
  `certificate_id` bigint NULL DEFAULT NULL COMMENT '凭证id',
  `subject_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科目编码',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '科目id',
  `account_id` bigint NULL DEFAULT NULL,
  `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '单价',
  `assist_id` bigint NULL DEFAULT NULL COMMENT '财务初始余额id（辅助核算需要）',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`detail_id`) USING BTREE,
  INDEX `certificate_id`(`certificate_id` ASC, `subject_id` ASC, `account_id` ASC) USING BTREE,
  INDEX `account_id`(`account_id` ASC, `subject_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '凭证详情' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_certificate_detail
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_currency
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_currency`;
CREATE TABLE `wk_finance_currency`  (
  `currency_id` bigint NOT NULL COMMENT '币种id',
  `currency_coding` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '编码',
  `currency_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '币种名称',
  `exchange_rate` decimal(18, 2) NULL DEFAULT NULL COMMENT '汇率',
  `home_currency` int NULL DEFAULT NULL COMMENT '是否是本币位',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `status` int NULL DEFAULT 1 COMMENT '1.正常 3.删除',
  `account_id` bigint NULL DEFAULT NULL,
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`currency_id`) USING BTREE,
  INDEX `currency_name`(`currency_name` ASC, `status` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '币种' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_currency
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_dashboard_config
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_dashboard_config`;
CREATE TABLE `wk_finance_dashboard_config`  (
  `id` bigint NOT NULL,
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `account_id` bigint NULL DEFAULT NULL,
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '仪表盘设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_dashboard_config
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_digest
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_digest`;
CREATE TABLE `wk_finance_digest`  (
  `digest_id` bigint NOT NULL,
  `digest_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '摘要内容',
  `create_user_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`digest_id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '凭证摘要' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_digest
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_flows
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_flows`;
CREATE TABLE `wk_finance_flows`  (
  `flows_id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目名称',
  `place` int NULL DEFAULT NULL COMMENT '行次',
  `cumulative` decimal(18, 2) NULL DEFAULT NULL COMMENT '本年累计',
  `data_source` int NULL DEFAULT NULL COMMENT '数据是否可以编辑',
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`flows_id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '现金流量初始余额' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_flows
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_income_statement_config
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_income_statement_config`;
CREATE TABLE `wk_finance_income_statement_config`  (
  `id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` json NULL COMMENT '公式',
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `account_id` bigint NULL DEFAULT NULL,
  `grade` int NULL DEFAULT NULL COMMENT '1 等级 第几级',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `wk_finance_income_statement_config_company_id_account_id_index`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '利润表配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_income_statement_config
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_income_statement_report
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_income_statement_report`;
CREATE TABLE `wk_finance_income_statement_report`  (
  `id` bigint NOT NULL,
  `type` int NULL DEFAULT 1 COMMENT '类型 1 月度 2 季度',
  `from_period` int NOT NULL COMMENT '开始账期：格式 yyyyMM',
  `to_period` int NULL DEFAULT NULL COMMENT '结束账期：格式 yyyyMM',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` json NULL COMMENT '公式',
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `month_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '月值',
  `year_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '年值',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `account_id` bigint NULL DEFAULT NULL,
  `grade` int NULL DEFAULT NULL COMMENT '1 等级 第几级',
  `settled` tinyint(1) NULL DEFAULT 0 COMMENT '是否结账',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `wk_finance_income_statement_report_company_id_account_id_index`(`account_id` ASC) USING BTREE,
  INDEX `account_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '利润表报表数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_income_statement_report
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_indicator
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_indicator`;
CREATE TABLE `wk_finance_indicator`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指标名称',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `sort` int NULL DEFAULT NULL COMMENT '顺序',
  `type` int NULL DEFAULT NULL COMMENT '类型 1 资产负债表 2 利润表 3 现金流量表',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '财务指标' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_indicator
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_initial
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_initial`;
CREATE TABLE `wk_finance_initial`  (
  `initial_id` bigint NOT NULL,
  `subject_id` bigint NULL DEFAULT NULL COMMENT '科目id',
  `is_assist` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是辅助核算',
  `initial_balance` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '期初金额',
  `initial_num` int NULL DEFAULT NULL COMMENT '期初数量',
  `add_up_debtor_balance` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '本年累计借方金额',
  `add_up_debtor_num` int NULL DEFAULT NULL COMMENT '本年累计借方数量',
  `add_up_credit_balance` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '本年累计贷方金额',
  `add_up_credit_num` int NULL DEFAULT NULL COMMENT '本年累计贷方数量',
  `beginning_balance` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '年初金额',
  `beginning_num` int NULL DEFAULT NULL COMMENT '年初数量',
  `profit_balance` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '实际损益发生金额',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `account_id` bigint NOT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`initial_id`) USING BTREE,
  INDEX `account_id`(`account_id` ASC) USING BTREE,
  INDEX `subject_id`(`subject_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '财务初始余额' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_initial
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_initial_assist
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_initial_assist`;
CREATE TABLE `wk_finance_initial_assist`  (
  `assist_id` bigint NOT NULL,
  `initial_id` bigint NULL DEFAULT NULL,
  `subject_id` bigint NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `finance_assist_id` bigint NULL DEFAULT NULL COMMENT '辅助核算辅助表id wk_finance_assist',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`assist_id`) USING BTREE,
  INDEX `account_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '初始余额辅助核算' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_initial_assist
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_initial_assist_adjuvant
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_initial_assist_adjuvant`;
CREATE TABLE `wk_finance_initial_assist_adjuvant`  (
  `assist_adjuvant_id` bigint NOT NULL,
  `assist_id` bigint NULL DEFAULT NULL,
  `label` int NULL DEFAULT NULL COMMENT '标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义',
  `account_id` bigint NULL DEFAULT NULL,
  `relation_id` bigint NULL DEFAULT NULL COMMENT '关联模块id',
  `label_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `adjuvant_id` bigint NULL DEFAULT NULL COMMENT '核算表id',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`assist_adjuvant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '财务初始余额管理辅助核算表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_initial_assist_adjuvant
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_parameter
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_parameter`;
CREATE TABLE `wk_finance_parameter`  (
  `parameter_id` bigint NOT NULL,
  `start_time` datetime NULL DEFAULT NULL COMMENT '启用时间',
  `bookkeeper_id` bigint NULL DEFAULT NULL COMMENT '会计制度id',
  `level` int NULL DEFAULT 4 COMMENT '科目级次',
  `rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科目编码长度 例：4-2-2',
  `account_book_direction` int NULL DEFAULT 1 COMMENT '账簿余额方向与科目方向相同 1.相同 2.不同',
  `deficit_examine` int NULL DEFAULT 2 COMMENT '现金、银行存款科目赤字检查 1.是 2.否',
  `voucher_examine` int NULL DEFAULT 2 COMMENT '凭证审核后才允许结账 1.是 2.否',
  `property_unable` int NULL DEFAULT 2 COMMENT '生成折旧凭证后不能新增和修改以前期间的卡片 1.是 2.否',
  `taxpayer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '纳税人名称',
  `taxpayer_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '纳税人识别号',
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司名称',
  `currency_id` bigint NOT NULL COMMENT '币种id',
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`parameter_id`) USING BTREE,
  INDEX `company_id`(`currency_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统参数设置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_parameter
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_report_template
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_report_template`;
CREATE TABLE `wk_finance_report_template`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sort` int NULL DEFAULT NULL COMMENT '行次',
  `formula` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公式',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `editable` tinyint(1) NULL DEFAULT 1 COMMENT '可编辑',
  `sort_index` int NULL DEFAULT NULL COMMENT '索引',
  `row_Id` int NULL DEFAULT NULL COMMENT '行id',
  `type` int NULL DEFAULT NULL COMMENT '类型 1 资产负债表 2 利润表 3 现金流量表 4 扩展表 ',
  `category` int NULL DEFAULT NULL COMMENT '现金流量表分类',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `grade` int NULL DEFAULT NULL COMMENT '1 等级 第几级',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 136 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '财务系统报表配置模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_report_template
-- ----------------------------
INSERT INTO `wk_finance_report_template` VALUES (1, '流动资产：', 0, '[]', NULL, 0, 0, 0, 1, NULL, '2021-09-18 16:39:40', 1);
INSERT INTO `wk_finance_report_template` VALUES (2, '流动负债：', 0, '[]', NULL, 0, 32, 0, 1, NULL, '2021-09-18 16:39:40', 1);
INSERT INTO `wk_finance_report_template` VALUES (3, '货币资金', 1, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1331,\"subjectName\":\"库存现金\",\"subjectNumber\":\"1001\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1332,\"subjectName\":\"银行存款\",\"subjectNumber\":\"1002\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1333,\"subjectName\":\"其他货币资金\",\"subjectNumber\":\"1012\"}]', NULL, 1, 1, 1, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (4, '短期借款', 31, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1375,\"subjectName\":\"短期借款\",\"subjectNumber\":\"2001\"}]', NULL, 1, 33, 1, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (5, '短期投资', 2, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1334,\"subjectName\":\"短期投资\",\"subjectNumber\":\"1101\"}]', NULL, 1, 2, 2, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (6, '应付票据', 32, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1376,\"subjectName\":\"应付票据\",\"subjectNumber\":\"2201\"}]', NULL, 1, 34, 2, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (7, '应收票据', 3, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1339,\"subjectName\":\"应收票据\",\"subjectNumber\":\"1121\"}]', NULL, 1, 3, 3, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (8, '应付账款', 33, '[{\"operator\":\"+\",\"rules\":4,\"subjectId\":1341,\"subjectName\":\"预付账款\",\"subjectNumber\":\"1123\"},{\"operator\":\"+\",\"rules\":4,\"subjectId\":1377,\"subjectName\":\"应付账款\",\"subjectNumber\":\"2202\"}]', NULL, 1, 35, 3, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (9, '应收账款', 4, '[{\"operator\":\"+\",\"rules\":3,\"subjectId\":1340,\"subjectName\":\"应收账款\",\"subjectNumber\":\"1122\"},{\"operator\":\"+\",\"rules\":3,\"subjectId\":1378,\"subjectName\":\"预收账款\",\"subjectNumber\":\"2203\"}]', NULL, 1, 4, 4, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (10, '预收账款', 34, '[{\"operator\":\"+\",\"rules\":4,\"subjectId\":1340,\"subjectName\":\"应收账款\",\"subjectNumber\":\"1122\"},{\"operator\":\"+\",\"rules\":4,\"subjectId\":1378,\"subjectName\":\"预收账款\",\"subjectNumber\":\"2203\"}]', NULL, 1, 36, 4, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (11, '预付账款', 5, '[{\"operator\":\"+\",\"rules\":3,\"subjectId\":1341,\"subjectName\":\"预付账款\",\"subjectNumber\":\"1123\"},{\"operator\":\"+\",\"rules\":3,\"subjectId\":1377,\"subjectName\":\"应付账款\",\"subjectNumber\":\"2202\"}]', NULL, 1, 5, 5, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (12, '应付职工薪酬', 35, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1379,\"subjectName\":\"应付职工薪酬\",\"subjectNumber\":\"2211\"}]', NULL, 1, 37, 5, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (13, '应收股利', 6, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1342,\"subjectName\":\"应收股利\",\"subjectNumber\":\"1131\"}]', NULL, 1, 6, 6, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (14, '应交税费', 36, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1393,\"subjectName\":\" 应交税费\",\"subjectNumber\":\"2221\"}]', NULL, 1, 38, 6, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (15, '应收利息', 7, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1343,\"subjectName\":\"应收利息\",\"subjectNumber\":\"1132\"}]', NULL, 1, 7, 7, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (16, '应付利息', 37, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1412,\"subjectName\":\"应付利息\",\"subjectNumber\":\"2231\"}]', NULL, 1, 39, 7, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (17, '其他应收款', 8, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1344,\"subjectName\":\"其他应收款\",\"subjectNumber\":\"1221\"}]', NULL, 1, 8, 8, 1, NULL, '2021-09-18 16:39:40', 2);
INSERT INTO `wk_finance_report_template` VALUES (18, '应付利润', 38, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1413,\"subjectName\":\"应付利润\",\"subjectNumber\":\"2232\"}]', NULL, 1, 40, 8, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (19, '存货', 9, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1345,\"subjectName\":\"材料采购\",\"subjectNumber\":\"1401\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1346,\"subjectName\":\"在途物资\",\"subjectNumber\":\"1402\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1347,\"subjectName\":\"原材料\",\"subjectNumber\":\"1403\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1348,\"subjectName\":\"材料成本差异\",\"subjectNumber\":\"1404\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1349,\"subjectName\":\"库存商品\",\"subjectNumber\":\"1405\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1350,\"subjectName\":\"商品进销差价\",\"subjectNumber\":\"1407\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1351,\"subjectName\":\"委托加工物资\",\"subjectNumber\":\"1408\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1352,\"subjectName\":\"周转材料\",\"subjectNumber\":\"1411\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1353,\"subjectName\":\"消耗性生物资产\",\"subjectNumber\":\"1421\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1437,\"subjectName\":\"生产成本\",\"subjectNumber\":\"4001\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1440,\"subjectName\":\"制造费用\",\"subjectNumber\":\"4101\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1442,\"subjectName\":\"工程施工\",\"subjectNumber\":\"4401\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1443,\"subjectName\":\"机械作业\",\"subjectNumber\":\"4403\"}]', NULL, 1, 9, 9, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (20, '其他应付款', 39, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1414,\"subjectName\":\"其他应付款\",\"subjectNumber\":\"2241\"}]', NULL, 1, 41, 9, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (21, '其中：原材料', 10, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1347,\"subjectName\":\"原材料\",\"subjectNumber\":\"1403\"}]', NULL, 1, 10, 10, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (22, '其他流动负债', 40, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1396,\"subjectName\":\"待转销项税额\",\"subjectNumber\":\"222106\"}]', NULL, 1, 42, 10, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (23, '在产品', 11, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1437,\"subjectName\":\"生产成本\",\"subjectNumber\":\"4001\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1440,\"subjectName\":\"制造费用\",\"subjectNumber\":\"4101\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1442,\"subjectName\":\"工程施工\",\"subjectNumber\":\"4401\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1443,\"subjectName\":\"机械作业\",\"subjectNumber\":\"4403\"}]', NULL, 1, 11, 11, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (24, '流动负债合计', 41, '[\"L31+L32+L33+L34+L35+L36+L37+L38+L39+L40\"]', NULL, 0, 43, 11, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (25, '库存商品', 12, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1349,\"subjectName\":\"库存商品\",\"subjectNumber\":\"1405\"}]', NULL, 1, 12, 12, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (26, '非流动负债：', 0, '[]', NULL, 0, 44, 12, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (27, '周转材料', 13, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1352,\"subjectName\":\"周转材料\",\"subjectNumber\":\"1411\"}]', NULL, 1, 13, 13, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (28, '长期借款', 42, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1416,\"subjectName\":\"长期借款\",\"subjectNumber\":\"2501\"}]', NULL, 1, 45, 13, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (29, '其他流动资产', 14, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1394,\"subjectName\":\"待抵扣进项税额\",\"subjectNumber\":\"222104\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1395,\"subjectName\":\"待认证进项税额\",\"subjectNumber\":\"222105\"},{\"operator\":\"+\",\"rules\":1,\"subjectId\":1392,\"subjectName\":\"未交增值税\",\"subjectNumber\":\"222102\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1397,\"subjectName\":\"增值税留抵税额\",\"subjectNumber\":\"222107\"}]', NULL, 1, 14, 14, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (30, '长期应付款', 43, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1417,\"subjectName\":\"长期应付款\",\"subjectNumber\":\"2701\"}]', NULL, 1, 46, 14, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (31, '流动资产合计', 15, '[\"L1+L2+L3+L4+L5+L6+L7+L8+L9+L14\"]', NULL, 0, 15, 15, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (32, '递延收益', 44, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1415,\"subjectName\":\"递延收益\",\"subjectNumber\":\"2401\"}]', NULL, 1, 47, 15, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (33, '非流动资产：', 0, '[]', NULL, 0, 16, 16, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (34, '其他非流动负债', 45, '[]', NULL, 0, 48, 16, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (35, '长期债券投资', 16, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1354,\"subjectName\":\"长期债券投资\",\"subjectNumber\":\"1501\"}]', NULL, 1, 17, 17, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (36, '非流动负债合计', 46, '[\"L42+L43+L44+L45\"]', NULL, 0, 49, 17, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (37, '长期股权投资', 17, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1357,\"subjectName\":\"长期股权投资\",\"subjectNumber\":\"1511\"}]', NULL, 1, 18, 18, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (38, '负债合计', 47, '[\"L41+L46\"]', NULL, 0, 50, 18, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (39, '固定资产原价', 18, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1360,\"subjectName\":\"固定资产\",\"subjectNumber\":\"1601\"}]', NULL, 1, 19, 19, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (40, '减：累计折旧', 19, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1361,\"subjectName\":\"累计折旧\",\"subjectNumber\":\"1602\"}]', NULL, 1, 20, 20, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (41, '固定资产账面价值', 20, '[\"L18-L19\"]', NULL, 0, 21, 21, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (42, '在建工程', 21, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1362,\"subjectName\":\"在建工程\",\"subjectNumber\":\"1604\"}]', NULL, 1, 22, 22, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (43, '工程物资', 22, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1367,\"subjectName\":\"工程物资\",\"subjectNumber\":\"1605\"}]', NULL, 1, 23, 23, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (44, '固定资产清理', 23, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1368,\"subjectName\":\"固定资产清理\",\"subjectNumber\":\"1606\"}]', NULL, 1, 24, 24, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (45, '生产性生物资产', 24, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1369,\"subjectName\":\"生产性生物资产\",\"subjectNumber\":\"1621\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1370,\"subjectName\":\"生产性生物资产累计折旧\",\"subjectNumber\":\"1622\"}]', NULL, 1, 25, 25, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (46, '所有者权益（或股东权益）：', 0, '[]', NULL, 0, 51, 25, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (47, '无形资产', 25, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1371,\"subjectName\":\"无形资产\",\"subjectNumber\":\"1701\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1372,\"subjectName\":\"累计摊销\",\"subjectNumber\":\"1702\"}]', NULL, 1, 26, 26, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (48, '实收资本（或股本）', 48, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1418,\"subjectName\":\"实收资本\",\"subjectNumber\":\"3001\"}]', NULL, 1, 52, 26, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (49, '开发支出', 26, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1441,\"subjectName\":\"研发支出\",\"subjectNumber\":\"4301\"}]', NULL, 1, 27, 27, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (50, '资本公积', 49, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1419,\"subjectName\":\"资本公积\",\"subjectNumber\":\"3002\"}]', NULL, 1, 53, 27, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (51, '长期待摊费用', 27, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1373,\"subjectName\":\"长期待摊费用\",\"subjectNumber\":\"1801\"}]', NULL, 1, 28, 28, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (52, '盈余公积', 50, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1424,\"subjectName\":\"盈余公积\",\"subjectNumber\":\"3101\"}]', NULL, 1, 54, 28, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (53, '其他非流动资产', 28, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1374,\"subjectName\":\"待处理财产损溢\",\"subjectNumber\":\"1901\"}]', NULL, 1, 29, 29, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (54, '未分配利润', 51, '[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1428,\"subjectName\":\"本年利润\",\"subjectNumber\":\"3103\"},{\"operator\":\"+\",\"rules\":0,\"subjectId\":1429,\"subjectName\":\"利润分配\",\"subjectNumber\":\"3104\"}]', NULL, 1, 55, 29, 1, NULL, '2021-09-18 16:39:41', 2);
INSERT INTO `wk_finance_report_template` VALUES (55, '非流动资产合计', 29, '[\"L16+L17+L20+L21+L22+L23+L24+L25+L26+L27+L28\"]', NULL, 0, 30, 30, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (56, '所有者权益（或股东权益）合计', 52, '[\"L48+L49+L50+L51\"]', NULL, 0, 56, 30, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (57, '资产总计', 30, '[\"L15+L29\"]', NULL, 0, 31, 31, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (58, '负债和所有者权益（或股东权益）总计', 53, '[\"L47+L52\"]', NULL, 0, 57, 31, 1, NULL, '2021-09-18 16:39:41', 1);
INSERT INTO `wk_finance_report_template` VALUES (59, '一、营业收入', 1, '[{\"operator\":\"+\",\"rules\":6,\"subjectId\":1444,\"subjectName\":\"主营业务收入\",\"subjectNumber\":\"5001\"},{\"operator\":\"+\",\"rules\":6,\"subjectId\":1445,\"subjectName\":\"其他业务收入\",\"subjectNumber\":\"5051\"}]', NULL, 1, 0, NULL, 2, NULL, '2021-09-18 16:39:56', 1);
INSERT INTO `wk_finance_report_template` VALUES (60, '减：营业成本', 2, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1453,\"subjectName\":\"主营业务成本\",\"subjectNumber\":\"5401\"},{\"operator\":\"+\",\"rules\":5,\"subjectId\":1454,\"subjectName\":\"其他业务成本\",\"subjectNumber\":\"5402\"}]', NULL, 1, 1, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (61, '税金及附加', 3, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1455,\"subjectName\":\"税金及附加\",\"subjectNumber\":\"5403\"}]', NULL, 1, 2, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (62, '其中：消费税', 4, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1456,\"subjectName\":\"消费税\",\"subjectNumber\":\"540301\"}]', NULL, 1, 3, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (63, '城市维护建设税', 6, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1457,\"subjectName\":\"城市维护建设税\",\"subjectNumber\":\"540303\"}]', NULL, 1, 4, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (64, '资源税', 7, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1458,\"subjectName\":\"资源税\",\"subjectNumber\":\"540304\"}]', NULL, 1, 5, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (65, '土地增值税', 8, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1459,\"subjectName\":\"土地增值税\",\"subjectNumber\":\"540305\"}]', NULL, 1, 6, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (66, '城镇土地使用税、房产税、车船税、印花税', 9, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1460,\"subjectName\":\"城镇土地使用税\",\"subjectNumber\":\"540306\"},{\"operator\":\"+\",\"rules\":5,\"subjectId\":1462,\"subjectName\":\"车船税\",\"subjectNumber\":\"540308\"},{\"operator\":\"+\",\"rules\":5,\"subjectId\":1461,\"subjectName\":\"房产税\",\"subjectNumber\":\"540307\"},{\"operator\":\"+\",\"rules\":5,\"subjectId\":1463,\"subjectName\":\"印花税\",\"subjectNumber\":\"540309\"}]', NULL, 1, 7, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (67, '教育费附加、矿产资源补偿费、排污费', 10, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1466,\"subjectName\":\"排污费\",\"subjectNumber\":\"540312\"},{\"operator\":\"+\",\"rules\":5,\"subjectId\":1465,\"subjectName\":\"矿产资源补偿费\",\"subjectNumber\":\"540311\"},{\"operator\":\"+\",\"rules\":5,\"subjectId\":1464,\"subjectName\":\"教育费附加\",\"subjectNumber\":\"540310\"}]', NULL, 1, 8, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (68, '销售费用', 11, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1468,\"subjectName\":\"销售费用\",\"subjectNumber\":\"5601\"}]', NULL, 1, 9, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (69, '其中：商品维修费', 12, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1483,\"subjectName\":\"商品维修费\",\"subjectNumber\":\"560115\"}]', NULL, 1, 10, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (70, '广告费和业务宣传费', 13, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1484,\"subjectName\":\"广告和业务宣传费\",\"subjectNumber\":\"560116\"}]', NULL, 1, 11, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (71, '管理费用', 14, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1486,\"subjectName\":\"管理费用\",\"subjectNumber\":\"5602\"}]', NULL, 1, 12, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (72, '其中：开办费', 15, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1499,\"subjectName\":\"开办费\",\"subjectNumber\":\"560213\"}]', NULL, 1, 13, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (73, '业务招待费', 16, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1491,\"subjectName\":\"交际应酬费\",\"subjectNumber\":\"560205\"}]', NULL, 1, 14, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (74, '研究费用', 17, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1501,\"subjectName\":\"研究费用\",\"subjectNumber\":\"560215\"}]', NULL, 1, 15, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (75, '财务费用', 18, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1503,\"subjectName\":\"财务费用\",\"subjectNumber\":\"5603\"}]', NULL, 1, 16, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (76, '其中：利息费用（收入以“-”号填列）', 19, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1505,\"subjectName\":\"利息\",\"subjectNumber\":\"560302\"}]', NULL, 1, 17, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (77, '加：投资收益（损失以“-”号填列）', 20, '[{\"operator\":\"+\",\"rules\":6,\"subjectId\":1446,\"subjectName\":\"投资收益\",\"subjectNumber\":\"5111\"}]', NULL, 1, 18, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (78, '二、营业利润（亏损以“-”号填列）', 21, '[\"L1-L2-L3-L11-L14-L18\"]', NULL, 0, 19, NULL, 2, NULL, '2021-09-18 16:39:56', 1);
INSERT INTO `wk_finance_report_template` VALUES (79, '加：营业外收入', 22, '[{\"operator\":\"+\",\"rules\":6,\"subjectId\":1447,\"subjectName\":\"营业外收入\",\"subjectNumber\":\"5301\"}]', NULL, 1, 20, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (80, '其中：政府补助', 23, '[{\"operator\":\"+\",\"rules\":6,\"subjectId\":1449,\"subjectName\":\"政府补助\",\"subjectNumber\":\"530102\"}]', NULL, 1, 21, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (81, '减：营业外支出', 24, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1508,\"subjectName\":\"营业外支出\",\"subjectNumber\":\"5711\"}]', NULL, 1, 22, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (82, '其中：坏账损失', 25, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1511,\"subjectName\":\"坏账损失\",\"subjectNumber\":\"571103\"}]', NULL, 1, 23, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (83, '无法收回的长期债券投资损失', 26, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1512,\"subjectName\":\"无法收回的长期债券投资损失\",\"subjectNumber\":\"571104\"}]', NULL, 1, 24, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (84, '无法收回的长期股权投资损失', 27, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1513,\"subjectName\":\"无法收回的长期股权投资损失\",\"subjectNumber\":\"571105\"}]', NULL, 1, 25, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (85, '自然灾害等不可抗力因素造成的损失', 28, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1514,\"subjectName\":\"自然灾害等不可抗力造成的损失\",\"subjectNumber\":\"571106\"}]', NULL, 1, 26, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (86, '税收滞纳金', 29, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1515,\"subjectName\":\"税收滞纳金\",\"subjectNumber\":\"571107\"}]', NULL, 1, 27, NULL, 2, NULL, '2021-09-18 16:39:56', 3);
INSERT INTO `wk_finance_report_template` VALUES (87, '三、利润总额（亏损总额以“-”号填列）', 30, '[\"L21+L22-L24\"]', NULL, 0, 28, NULL, 2, NULL, '2021-09-18 16:39:56', 1);
INSERT INTO `wk_finance_report_template` VALUES (88, '减：所得税费用', 31, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1519,\"subjectName\":\"所得税费用\",\"subjectNumber\":\"5801\"}]', NULL, 1, 29, NULL, 2, NULL, '2021-09-18 16:39:56', 2);
INSERT INTO `wk_finance_report_template` VALUES (89, '四、净利润（净亏损以“-”号填列）', 32, '[\"L30-L31\"]', NULL, 0, 30, NULL, 2, NULL, '2021-09-18 16:39:56', 1);
INSERT INTO `wk_finance_report_template` VALUES (90, '一、经营活动产生的现金流量：', 0, '[]', '', 0, 0, 0, 3, 1, '2021-09-18 16:39:59', 1);
INSERT INTO `wk_finance_report_template` VALUES (91, '销售产成品、商品、提供劳务收到的现金', 1, '[\"IN1-((BA[3,2]-BA[3,1])+(BA[4,2]-BA[4,1])-(BA[34,2]-BA[34,1]))+EX5+EX11\"]', '利表[营业收入]-资表[(应收票据期末余额-应收票据期初余额)+(应收账款期末余额-应收账款期初余额)-(预收账款期末余额-预收账款期初余额)]+表外[本年销项税+在其他业务支出中列支的税金]', 0, 1, 1, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (92, '收到其他与经营活动有关的现金', 2, '[\"((IN22-(BA[8,2]-BA[8,1])) < 0 ? 0 : (IN22-(BA[8,2]-BA[8,1])))\"]', '利表[营业外收入]-资表[其他应收款期末数-其他应收款期初数]', 0, 2, 2, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (93, '购买原材料、商品、接受劳务支付的现金', 3, '[\"IN2+(BA[9,2] -BA[9,1])-(BA[32,2]-BA[32,1])-(BA[33,2]-BA[33,1])+(BA[5,2]-BA[5,1])+EX6\"]', '利表[营业成本]＋资表[（存货期末余额－存货期初余额）-（应付票据期末余额-应付票据期初余额）-（应付账款期末余额-应付账款期初余额）＋（预付账款期末余额－预付账款期初余额）]+表外[本年进项税]', 0, 3, 3, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (94, '支付的职工薪酬', 4, '[\"EX4\"]', '表外[实际支付给职工及为职工支付的现金]', 0, 4, 4, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (95, '支付的税费', 5, '[\"EX12-(BA[36,2]-BA[36,1])\"]', '表外[应缴纳各项税金合计]-资表[（应交税费期末数-应交税费期初数]', 0, 5, 5, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (96, '支付其他与经营活动有关的现金', 6, '[\"(((IN24+IN14+IN11+IN18+IN3+IN19)-(EX15+EX16+EX4+EX18+EX14+EX17+EX13+EX10+EX9+EX8-EX19)+(BA[14,2]-BA[14,1]) - (BA[39,2]-BA[39,1] - (BA[35,2]-BA[35,1]) - (BA[40,2]-BA[40,1]))) < 0 ? 0 : ((IN24+IN14+IN11+IN18+IN3+IN19)-(EX15+EX16+EX4+EX18+EX14+EX17+EX13+EX10+EX9+EX8-EX19)+(BA[14,2]-BA[14,1]) - (BA[39,2]-BA[39,1] - (BA[35,2]-BA[35,1]) - (BA[40,2]-BA[40,1]))))\"]', '利表[营业外支出＋管理费用＋销售费用+财务费用+营业税金及附加+利息费用] -表外[无形资产摊销+长期待摊费用摊销+实际支付给职工及为职工支付的现金+处置固定资产、无形资产和其他长期资产的损失(减：收益)＋利息费用+计提的减值准备+计提固定资产折旧+管理费用中列支的税金+管理费用中列支的税金+所得税+主营业务税金及附加-汇兑收益（减：汇兑损失））+资表[（其他流动资产期末数-其他流动资产期初数-（其他应付款期末-其他应付款期初）-（应付职工薪酬期末-应付职工薪酬期初）-（其他流动负债期末数-期初）]', 0, 6, 6, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (97, '经营活动产生的现金流量净额', 7, '[\"L1+L2-L3-L4-L5-L6\"]', '行1+2-3-4-5-6', 0, 7, 7, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (98, '二、投资活动产生的现金流量：', 0, '[]', '', 0, 8, 8, 3, 1, '2021-09-18 16:39:59', 1);
INSERT INTO `wk_finance_report_template` VALUES (99, '收回短期投资、长期债券投资和长期股权投资收到的现金', 8, '[\"((BA[2,2]-BA[2,1]) + (BA[16,2]-BA[16,1])+(BA[17,2]-BA[17,1]))*(-1) < 0 ? 0 : ((BA[2,2]-BA[2,1]) + (BA[16,2]-BA[16,1])+(BA[17,2]-BA[17,1]))*(-1)\"]', '资表[（短期投资期末-短期投资期初）+（长期债权投资合计期末数-长期债权投资合计期初数）+（长期股权投资期末数-长期股权投资期初数）+（长期应收款期末-长期应收款期初）]*-1 +利表[公允价值变动收益]', 0, 9, 9, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (100, '取得投资收益收到的现金', 9, '[\"IN20-((BA[7,2]-BA[7,1])+(BA[6,2]-BA[6,1]))\"]', '利表[投资收益本年累计]-资表[（应收利息期末数-应收利息期初数）+（应收股利期末数-应收股利期初数）]', 0, 10, 10, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (101, '处置固定资产、无形资产和其他非流动资产收回的现金净额', 10, '[]', '', 0, 11, 11, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (102, '短期投资、长期债券投资和长期股权投资支付的现金', 11, '[\"(((BA[2,2]-BA[2,1]) + (BA[16,2]-BA[16,1])+(BA[17,2]-BA[17,1]))*(-1)) < 0 ? (((BA[2,2]-BA[2,1]) + (BA[16,2]-BA[16,1])+(BA[17,2]-BA[17,1]))*(-1))*(-1):0\"]', '', 0, 12, 12, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (103, '购建固定资产、无形资产和其他非流动资产支付的现金', 12, '[\"((BA[21,2]-BA[21,1])+(BA[20,2]-BA[20,1])+(BA[25,2]-BA[25,1])+(BA[28,2]-BA[28,1])+(BA[23,2]-BA[23,1])+(BA[22,2]-BA[22,1])+(BA[26,2]-BA[26,1])+(BA[27,2]-BA[27,1])+(BA[24,2]-BA[24,1]))+(EX15+EX16+EX18)+(EX13+EX17)\"]', '资表[（在建工程期末数－在建工程期初数）＋（固定资产净值期末数－固定资产净值期初数）＋（无形资产期末数－无形资产期初数）＋（其他非流动资产期末数－其他非流动资产期初数）+（固定资产清理期末数-固定资产清理期初数）+（工程物资期末数－工程物资期初数）+（开发支出期末数－开发支出期初数）+（长期待摊费用期末数－长期待摊费用期初数）+（生产性生物资产期末数－生产性生物资产期初数）]  +表外[无形资产摊销+长期待摊费用摊销+处置固定资产、无形资产和其他长期资产的损失(减：收益)] +表外[计提折旧费用+计提减值准备（仅新准则下加计提减值准备）]', 0, 13, 13, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (104, '投资活动产生的现金流量净额', 13, '[\"L8+L9+L10-L11-L12\"]', '行8+9+10-11-12', 0, 14, 14, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (105, '三、筹资活动产生的现金流量：', 0, '[]', '', 0, 15, 15, 3, 1, '2021-09-18 16:39:59', 1);
INSERT INTO `wk_finance_report_template` VALUES (106, '取得借款收到的现金', 14, '[\"((BA[31,2]-BA[31,1])+(BA[42,2]-BA[42,1])+(BA[43,2]-BA[43,1])+(BA[44,2]-BA[44,1])+(BA[45,2]-BA[45,1])) < 0 ? 0 : ((BA[31,2]-BA[31,1])+(BA[42,2]-BA[42,1])+(BA[43,2]-BA[43,1])+(BA[44,2]-BA[44,1])+(BA[45,2]-BA[45,1]))\"]', '资表[（短期借款期末数－短期借款期初数）＋（长期借款期末数－长期借款期初数）+（长期应付款期末数－长期应付款期初数）+（递延收益期末数－递延收益期初数）+（其他非流动负债期末数－期初数）]', 0, 16, 16, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (107, '吸收投资者投资收到的现金', 15, '[\"(BA[48,2]-BA[48,1])+(BA[49,2]-BA[49,1])+(BA[50,2]-BA[50,1])\"]', '资表【（实收资本或股本期末数－实收资本或股本期初数）+（资本公积期末数-资本公积期初数）+（盈余公积期末数-盈余公积期初数）】', 0, 17, 17, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (108, '偿还借款本金支付的现金', 16, '[\"((BA[31,2]-BA[31,1])+(BA[42,2]-BA[42,1])+(BA[43,2]-BA[43,1])+(BA[44,2]-BA[44,1])+(BA[45,2]-BA[45,1])) < 0 ? ((BA[31,2]-BA[31,1])+(BA[42,2]-BA[42,1])+(BA[43,2]-BA[43,1])+(BA[44,2]-BA[44,1])+(BA[45,2]-BA[45,1]))*(-1):0\"]', '', 0, 18, 18, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (109, '偿还借款利息支付的现金', 17, '[\"IN19-(BA[37,2]-BA[37,1])\"]', '利润表【利息费用】-资表【应付利息期末数-应付利息期初数】', 0, 19, 19, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (110, '分配利润支付的现金', 18, '[\"IN32-((BA[38,2]-BA[38,1])+(BA[51,2]-BA[51,1]))\"]', '利润表【净利润】-资表【（应付利润期末数-应付利润期初数）+（未分配利润期末数-未分配利润期初数）】', 0, 20, 20, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (111, '筹资活动产生的现金流量净额', 19, '[\"L14+L15-L17-L18\"]', '行14+15-17-18', 0, 21, 21, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (112, '四、现金净增加额', 20, '[\"L7+L13+L19\"]', '行7+13+19', 0, 22, 22, 3, 1, '2021-09-18 16:39:59', 1);
INSERT INTO `wk_finance_report_template` VALUES (113, '加：期初现金余额', 21, '[\"BA[1,1]\"]', '资表【货币资金期初数】', 0, 23, 23, 3, 1, '2021-09-18 16:39:59', 2);
INSERT INTO `wk_finance_report_template` VALUES (114, '五、期末现金余额', 22, '[\"L20+L21\"]', '行20+21', 0, 24, 24, 3, 1, '2021-09-18 16:39:59', 1);
INSERT INTO `wk_finance_report_template` VALUES (115, '支付给职工的工资', 1, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1379,\"subjectName\":\"应付职工薪酬\",\"subjectNumber\":\"2211\"}]', '', 1, 0, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (116, '支付给职工的四金', 2, '[]', '', 1, 1, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (117, '支付给职工的其他福利费', 3, '[]', '', 1, 2, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (118, '支付给职工以及为职工支付的现金', 4, '[\"L1+L2+L3\"]', '行数1+2+3之和，参与现金流量表行 4 计算', 0, 3, NULL, 4, 1, '2021-09-18 16:40:02', 2);
INSERT INTO `wk_finance_report_template` VALUES (119, '销项税额（小规模纳税人填本年累计应交增值税）', 5, '[{\"operator\":\"+\",\"rules\":6,\"subjectId\":1388,\"subjectName\":\"销项税额\",\"subjectNumber\":\"22210107\"}]', '参与现金流量表行 1 计算', 1, 4, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (120, '进项税额（小规模纳税人不填此项）', 6, '[{\"operator\":\"+\",\"rules\":5,\"subjectId\":1382,\"subjectName\":\"进项税额\",\"subjectNumber\":\"22210101\"}]', '参与现金流量表行 3 计算', 1, 5, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (121, '应交增值税', 7, '[\"L5-L6\"]', '等于行数5、6之差', 0, 6, NULL, 4, 1, '2021-09-18 16:40:02', 2);
INSERT INTO `wk_finance_report_template` VALUES (122, '主营业务税金及附加', 8, '[{\"operator\":\"+\",\"rules\":7,\"subjectId\":1455,\"subjectName\":\"税金及附加\",\"subjectNumber\":\"5403\"}]', '参与现金流量表行 6 计算', 1, 7, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (123, '所得税', 9, '[{\"operator\":\"+\",\"rules\":7,\"subjectId\":1519,\"subjectName\":\"所得税费用\",\"subjectNumber\":\"5801\"}]', '参与现金流量表行 6 计算', 1, 8, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (124, '管理费用中列支的税金', 10, '[]', '参与现金流量表行 6 计算', 1, 9, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (125, '在其他业务支出中列支的税金', 11, '[]', '参与现金流量表行 1 计算', 1, 10, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (126, '应缴纳各项税金合计', 12, '[\"L7+L8+L9+L10+L11\"]', '行数7、8、9、10、11之和；参与现金流量表行 5 计算', 0, 11, NULL, 4, 1, '2021-09-18 16:40:02', 2);
INSERT INTO `wk_finance_report_template` VALUES (127, '计提固定资产折旧', 13, '[{\"operator\":\"+\",\"rules\":6,\"subjectId\":1370,\"subjectName\":\"生产性生物资产累计折旧\",\"subjectNumber\":\"1622\"},{\"operator\":\"+\",\"rules\":6,\"subjectId\":1361,\"subjectName\":\"累计折旧\",\"subjectNumber\":\"1602\"}]', '参与现金流量表行 6 计算', 1, 12, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (128, '利息支出', 14, '[{\"operator\":\"+\",\"rules\":7,\"subjectId\":1505,\"subjectName\":\"利息\",\"subjectNumber\":\"560302\"}]', '参与现金流量表行 6 计算', 1, 13, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (129, '无形资产摊销', 15, '[]', '参与现金流量表行6、 12 计算', 1, 14, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (130, '长期待摊费用摊销', 16, '[]', '参与现金流量表行6、 12 计算', 1, 15, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (131, '计提的各项减值准备（仅反映本年与上年计提的差额）', 17, '[]', '参与现金流量表行 12 计算（仅新准则下加计提减值准备', 1, 16, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (132, '处置固定资产、无形资产和其他长期资产的损失(减：收益)', 18, '[]', '参与现金流量表行 12 计算', 1, 17, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (133, '汇兑收益（减：汇兑损失）', 19, '[]', '参与现金流量表行 6 计算', 1, 18, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (134, '不涉及现金收支的投资活动', 20, '[]', '参与现金流量表行 8 计算', 1, 19, NULL, 4, 1, '2021-09-18 16:40:02', 1);
INSERT INTO `wk_finance_report_template` VALUES (135, '不涉及现金收支的筹资活动', 21, '[]', '参与现金流量表行 15 计算（在现金流量表体现的金额与在辅助表填写的为相反数）', 1, 20, NULL, 4, 1, '2021-09-18 16:40:02', 1);

-- ----------------------------
-- Table structure for wk_finance_statement
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_statement`;
CREATE TABLE `wk_finance_statement`  (
  `statement_id` bigint NOT NULL,
  `statement_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结账名称',
  `is_end_over` int NULL DEFAULT 0 COMMENT '是否期末转结 1.是 0.否',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '转结科目id',
  `gain_rule` int NULL DEFAULT 1 COMMENT '取数规则 1.余额2.借方余额3.贷方余额 4.借方发生额5.贷方发生额6.损益发生额',
  `time_type` int NULL DEFAULT 1 COMMENT '时间类型 1.期末 2.期初 3.年初',
  `voucher_id` bigint NULL DEFAULT NULL COMMENT '凭证字',
  `digest_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '凭证摘要',
  `voucher_type` int NULL DEFAULT NULL COMMENT '凭证分类 1收益和损失分开结转 （分别生成收益凭证和损失凭证） 2.收益和损失同时结转',
  `adjust_subject_id` bigint NULL DEFAULT NULL COMMENT '“以前年度损益调整”科目',
  `end_subject_id` bigint NULL DEFAULT NULL COMMENT '“以前年度损益调整”科目的结转科目',
  `rest_subject_id` bigint NULL DEFAULT NULL COMMENT '其他损益科目的结转科目',
  `end_way` int NULL DEFAULT 1 COMMENT '结转方式：按余额反向结转 1.是 0.否',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `statement_type` int NULL DEFAULT 1 COMMENT '结账类型 1.普通结账 2.结账损益 3.转出未交增值税\r\n4.计提地税 5.计提所得税',
  `account_id` bigint NULL DEFAULT NULL COMMENT '账套id',
  `end_time_days` int NULL DEFAULT NULL COMMENT '结转损益日期，最大31，超过31默认最后一天',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`statement_id`) USING BTREE,
  INDEX `subject_id`(`subject_id` ASC, `voucher_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '结账表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_statement
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_statement_certificate
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_statement_certificate`;
CREATE TABLE `wk_finance_statement_certificate`  (
  `statement_certificate_id` bigint NOT NULL,
  `statement_id` bigint NULL DEFAULT NULL COMMENT '结账id',
  `certificate_id` bigint NULL DEFAULT NULL COMMENT '凭证id',
  `certificate_time` datetime NULL DEFAULT NULL COMMENT '生成凭证时间',
  `status` int NULL DEFAULT 1 COMMENT '结账状态 1.未结账 2.已结账',
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`statement_certificate_id`) USING BTREE,
  INDEX `statement_id`(`statement_id` ASC, `certificate_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '结账和凭证关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_statement_certificate
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_statement_settle
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_statement_settle`;
CREATE TABLE `wk_finance_statement_settle`  (
  `settle_id` bigint NOT NULL,
  `settle_time` datetime NULL DEFAULT NULL COMMENT '结账时间（已经结账的）',
  `create_user_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `account_id` bigint NULL DEFAULT NULL,
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`settle_id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '结账清单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_statement_settle
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_statement_subject
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_statement_subject`;
CREATE TABLE `wk_finance_statement_subject`  (
  `statement_subject_id` bigint NOT NULL,
  `statement_id` bigint NULL DEFAULT NULL COMMENT '结账id',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '科目id',
  `digest_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '摘要',
  `is_lend` int NULL DEFAULT NULL COMMENT '借/贷 1.借 2.贷',
  `money_ratio` int NULL DEFAULT NULL COMMENT '金额比例 %(最大不超过100）',
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`statement_subject_id`) USING BTREE,
  INDEX `company_id`(`subject_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '结账和科目关联表（除结转损益类型）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_statement_subject
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_statement_template
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_statement_template`;
CREATE TABLE `wk_finance_statement_template`  (
  `template_id` bigint NOT NULL,
  `digest_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '凭证摘要',
  `template_type` int NULL DEFAULT NULL COMMENT '1.日常开支 2.采购销售 3.往来款（含个人借款）4.转账业务 5.高级结转',
  `account_id` bigint NULL DEFAULT NULL,
  `is_end_over` int NULL DEFAULT 0 COMMENT '是否期末转结 1.是 0.否',
  `gain_rule` int NULL DEFAULT NULL COMMENT '取数规则 1.余额2.借方余额3.贷方余额 4.借方发生额5.贷方发生额6.损益发生额',
  `time_type` int NULL DEFAULT 1 COMMENT '时间类型 1.期末 2.期初 3.年初',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '转结科目id',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`template_id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '结账模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_statement_template
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_statement_template_subject
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_statement_template_subject`;
CREATE TABLE `wk_finance_statement_template_subject`  (
  `template_subject_id` bigint NOT NULL,
  `subject_id` bigint NULL DEFAULT NULL,
  `template_id` bigint NULL DEFAULT NULL,
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科目编号',
  `is_lend` int NULL DEFAULT NULL COMMENT '借/贷 1.借 2.贷',
  `money_ratio` int NULL DEFAULT NULL COMMENT '金额比例 %(最大不超过100）',
  `digest_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`template_subject_id`) USING BTREE,
  INDEX `subject_id`(`subject_id` ASC, `template_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '结账模板关联科目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_statement_template_subject
-- ----------------------------
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771841, 1716792716654366724, 1716792717828771840, '560107', 1, 100, '报销差旅费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771842, 1716792716465623040, 1716792717828771840, '1001', 2, 100, '报销差旅费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771844, 1716792716650172421, 1716792717828771843, '560101', 1, 100, '报销电话费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771845, 1716792716465623040, 1716792717828771843, '1001', 2, 100, '报销电话费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771847, 1716792716671143941, 1716792717828771846, '560303', 1, 100, '支付银行手续费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771848, 1716792716474011648, 1716792717828771846, '1002', 2, 100, '支付银行手续费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771850, 1716792716570480640, 1716792717828771849, '2211', 1, 100, '支付工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771851, 1716792716465623040, 1716792717828771849, '1001', 2, 100, '支付工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771853, 1716792716650172422, 1716792717828771852, '560102', 1, 100, '支付房租水电费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771854, 1716792716465623040, 1716792717828771852, '1001', 2, 100, '支付房租水电费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771856, 1716792716587257861, 1716792717828771855, '222117', 1, 100, '缴纳地税款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771857, 1716792716474011648, 1716792717828771855, '1002', 2, 100, '缴纳地税款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771859, 1716792716583063557, 1716792717828771858, '222111', 1, 100, '缴纳所得税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771860, 1716792716474011648, 1716792717828771858, '1002', 2, 100, '缴纳所得税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771862, 1716792716578869249, 1716792717828771861, '222102', 1, 100, '缴纳增值税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771863, 1716792716474011648, 1716792717828771861, '1002', 2, 100, '缴纳增值税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771865, 1716792716474011648, 1716792717828771864, '1002', 1, 100, '利息收入', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771866, 1716792716671143940, 1716792717828771864, '560302', 2, 100, '利息收入', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771868, 1716792716474011648, 1716792717828771867, '1002', 1, 100, '提现金', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771869, 1716792716671143940, 1716792717828771867, '560302', 2, 100, '提现金', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771871, 1716792716465623040, 1716792717828771870, '1001', 1, 100, '报销业务招待费用', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717828771872, 1716792716474011648, 1716792717828771870, '1002', 2, 100, '报销业务招待费用', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966145, 1716792716524343297, 1716792717832966144, '1405', 1, 86, '采购商品（银行付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966146, 1716792716570480643, 1716792717832966144, '22210101', 1, 14, '采购商品（银行付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966147, 1716792716474011648, 1716792717832966144, '1002', 2, 100, '采购商品（银行付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966149, 1716792716524343297, 1716792717832966148, '1405', 1, 86, '采购商品（应付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966150, 1716792716570480643, 1716792717832966148, '22210101', 1, 14, '采购商品（应付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966151, 1716792716566286336, 1716792717832966148, '2202', 2, 100, '采购商品（应付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966153, 1716792716474011648, 1716792717832966152, '1002', 1, 100, '销售商品（银行收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966154, 1716792716620812288, 1716792717832966152, '5001', 2, 86, '销售商品（银行收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966155, 1716792716574674946, 1716792717832966152, '22210107', 2, 14, '销售商品（银行收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966157, 1716792716465623040, 1716792717832966156, '1001', 1, 100, '销售商品（现金收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966158, 1716792716620812288, 1716792717832966156, '5001', 2, 86, '销售商品（现金收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717832966159, 1716792716574674946, 1716792717832966156, '22210107', 2, 14, '销售商品（现金收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160449, 1716792716503371776, 1716792717837160448, '1122', 1, 100, '销售商品（应收）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160450, 1716792716620812288, 1716792717837160448, '5001', 2, 86, '销售商品（应收）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160451, 1716792716574674946, 1716792717837160448, '22210107', 2, 14, '销售商品（应收）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160453, 1716792716507566081, 1716792717837160452, '1221', 1, 100, '个人借款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160454, 1716792716465623040, 1716792717837160452, '1001', 2, 100, '个人借款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160456, 1716792716465623040, 1716792717837160455, '1001', 1, 100, '个人还款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160457, 1716792716507566081, 1716792717837160455, '1221', 2, 100, '个人还款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160459, 1716792716474011648, 1716792717837160458, '1002', 1, 100, '收货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160460, 1716792716503371776, 1716792717837160458, '1122', 2, 100, '收货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160462, 1716792716566286336, 1716792717837160461, '2202', 1, 100, '支付货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160463, 1716792716474011648, 1716792717837160461, '1002', 2, 100, '支付货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160465, 1716792716654366727, 1716792717837160464, '560110', 1, 15, '计提工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160466, 1716792716666949633, 1716792717837160464, '560209', 1, 85, '计提工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717837160467, 1716792716570480640, 1716792717837160464, '2211', 2, 100, '计提工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717954600961, 1716792716654366724, 1716792717954600960, '560107', 1, 100, '报销差旅费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717954600962, 1716792716465623040, 1716792717954600960, '1001', 2, 100, '报销差旅费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717954600964, 1716792716650172421, 1716792717954600963, '560101', 1, 100, '报销电话费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717954600965, 1716792716465623040, 1716792717954600963, '1001', 2, 100, '报销电话费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717954600967, 1716792716671143941, 1716792717954600966, '560303', 1, 100, '支付银行手续费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717954600968, 1716792716474011648, 1716792717954600966, '1002', 2, 100, '支付银行手续费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795265, 1716792716570480640, 1716792717958795264, '2211', 1, 100, '支付工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795266, 1716792716465623040, 1716792717958795264, '1001', 2, 100, '支付工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795268, 1716792716650172422, 1716792717958795267, '560102', 1, 100, '支付房租水电费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795269, 1716792716465623040, 1716792717958795267, '1001', 2, 100, '支付房租水电费', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795271, 1716792716587257861, 1716792717958795270, '222117', 1, 100, '缴纳地税款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795272, 1716792716474011648, 1716792717958795270, '1002', 2, 100, '缴纳地税款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795274, 1716792716583063557, 1716792717958795273, '222111', 1, 100, '缴纳所得税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795275, 1716792716474011648, 1716792717958795273, '1002', 2, 100, '缴纳所得税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795277, 1716792716578869249, 1716792717958795276, '222102', 1, 100, '缴纳增值税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795278, 1716792716474011648, 1716792717958795276, '1002', 2, 100, '缴纳增值税', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795280, 1716792716474011648, 1716792717958795279, '1002', 1, 100, '利息收入', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795281, 1716792716671143940, 1716792717958795279, '560302', 2, 100, '利息收入', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795283, 1716792716474011648, 1716792717958795282, '1002', 1, 100, '提现金', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795284, 1716792716671143940, 1716792717958795282, '560302', 2, 100, '提现金', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795286, 1716792716465623040, 1716792717958795285, '1001', 1, 100, '报销业务招待费用', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795287, 1716792716474011648, 1716792717958795285, '1002', 2, 100, '报销业务招待费用', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795289, 1716792716524343297, 1716792717958795288, '1405', 1, 86, '采购商品（银行付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795290, 1716792716570480643, 1716792717958795288, '22210101', 1, 14, '采购商品（银行付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795291, 1716792716474011648, 1716792717958795288, '1002', 2, 100, '采购商品（银行付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795293, 1716792716524343297, 1716792717958795292, '1405', 1, 86, '采购商品（应付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795294, 1716792716570480643, 1716792717958795292, '22210101', 1, 14, '采购商品（应付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717958795295, 1716792716566286336, 1716792717958795292, '2202', 2, 100, '采购商品（应付款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989569, 1716792716474011648, 1716792717962989568, '1002', 1, 100, '销售商品（银行收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989570, 1716792716620812288, 1716792717962989568, '5001', 2, 86, '销售商品（银行收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989571, 1716792716574674946, 1716792717962989568, '22210107', 2, 14, '销售商品（银行收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989573, 1716792716465623040, 1716792717962989572, '1001', 1, 100, '销售商品（现金收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989574, 1716792716620812288, 1716792717962989572, '5001', 2, 86, '销售商品（现金收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989575, 1716792716574674946, 1716792717962989572, '22210107', 2, 14, '销售商品（现金收款）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989577, 1716792716503371776, 1716792717962989576, '1122', 1, 100, '销售商品（应收）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989578, 1716792716620812288, 1716792717962989576, '5001', 2, 86, '销售商品（应收）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989579, 1716792716574674946, 1716792717962989576, '22210107', 2, 14, '销售商品（应收）', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989581, 1716792716507566081, 1716792717962989580, '1221', 1, 100, '个人借款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989582, 1716792716465623040, 1716792717962989580, '1001', 2, 100, '个人借款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989584, 1716792716465623040, 1716792717962989583, '1001', 1, 100, '个人还款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989585, 1716792716507566081, 1716792717962989583, '1221', 2, 100, '个人还款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989587, 1716792716474011648, 1716792717962989586, '1002', 1, 100, '收货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989588, 1716792716503371776, 1716792717962989586, '1122', 2, 100, '收货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989590, 1716792716566286336, 1716792717962989589, '2202', 1, 100, '支付货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989591, 1716792716474011648, 1716792717962989589, '1002', 2, 100, '支付货款', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989593, 1716792716654366727, 1716792717962989592, '560110', 1, 15, '计提工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989594, 1716792716666949633, 1716792717962989592, '560209', 1, 85, '计提工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');
INSERT INTO `wk_finance_statement_template_subject` VALUES (1716792717962989595, 1716792716570480640, 1716792717962989592, '2211', 2, 100, '计提工资', 1716792716092329984, 1614516599265243136, '2023-10-24 20:24:15', NULL, '2023-10-24 20:24:15');

-- ----------------------------
-- Table structure for wk_finance_subject
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_subject`;
CREATE TABLE `wk_finance_subject`  (
  `subject_id` bigint NOT NULL,
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科目编号',
  `subject_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科目名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '上级科目',
  `type` int NULL DEFAULT NULL COMMENT '科目类型 1.资产 2.负债 3.权益 4.成本 5.损益6.共同',
  `category` int NULL DEFAULT NULL COMMENT '科目类别 根据类型改变 type为1时 1.流动资产 2.非流动资产 type为2时 1.流动负债 2.非流动负债 type为3 1.所有者权益 type为4 成本 type为5 1.营业收入 2.其他收益 3.期间费用 4.其他损失 5.营业成本及税金 6.以前年度损益调整 7.所得税 type为6 时  1.共同',
  `balance_direction` int NULL DEFAULT NULL COMMENT '余额方向 1.借 2.贷4',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `amount_unit` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数量核算 计量单位',
  `is_cash` int NULL DEFAULT 0 COMMENT '是否现金科目 1.是 0.否',
  `status` int NULL DEFAULT 1 COMMENT '1.正常启用 2.正常禁用 3.删除',
  `grade` int NULL DEFAULT 1 COMMENT '等级，第几级',
  `is_amount` int NULL DEFAULT 0 COMMENT '是否开启核算 1.开启 0.不开启',
  `account_id` bigint NULL DEFAULT NULL,
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`subject_id`) USING BTREE,
  INDEX `number`(`number` ASC, `subject_name` ASC, `parent_id` ASC, `type` ASC, `account_id` ASC) USING BTREE,
  INDEX `wk_finance_subject_number_company_id_account_id_index`(`number` ASC, `account_id` ASC) USING BTREE,
  INDEX `wk_finance_subject_company_id_account_id_index`(`account_id` ASC) USING BTREE,
  INDEX `wk_finance_subject_subject_id_company_id_account_id_index`(`subject_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科目' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_subject
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_subject_adjuvant
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_subject_adjuvant`;
CREATE TABLE `wk_finance_subject_adjuvant`  (
  `subject_adjuvant_id` bigint NOT NULL,
  `subject_id` bigint NULL DEFAULT NULL,
  `adjuvant_id` bigint NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `label` int NOT NULL COMMENT '标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义',
  `label_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签名称',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`subject_adjuvant_id`) USING BTREE,
  INDEX `subject_id`(`subject_id` ASC, `adjuvant_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科目和辅助核算关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_subject_adjuvant
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_subject_currency
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_subject_currency`;
CREATE TABLE `wk_finance_subject_currency`  (
  `subject_currency_id` bigint NOT NULL,
  `subject_id` bigint NULL DEFAULT NULL,
  `currency_id` bigint NULL DEFAULT NULL,
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`subject_currency_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科目和币种关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_subject_currency
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_subject_template
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_subject_template`;
CREATE TABLE `wk_finance_subject_template`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科目编号',
  `subject_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科目名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '上级科目',
  `type` int NULL DEFAULT NULL COMMENT '科目类型 1.资产 2.负债 3.权益 4.成本 5.损益6.共同',
  `category` int NULL DEFAULT NULL COMMENT '科目类别 根据类型改变 type为1时 1.流动资产 2.非流动资产 type为2时 1.流动负债 2.非流动负债 type为3 1.所有者权益 type为4 成本 type为5 1.营业收入 2.其他收益 3.期间费用 4.其他损失 5.营业成本及税金 6.以前年度损益调整 7.所得税 type为6 时  1.共同',
  `balance_direction` int NULL DEFAULT NULL COMMENT '余额方向 1.借 2.贷4',
  `amount_unit` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数量核算 计量单位',
  `is_cash` int NULL DEFAULT 0 COMMENT '是否现金科目 1.是 0.否',
  `status` int NULL DEFAULT 1 COMMENT '1.正常启用 2.正常禁用 3.删除',
  `grade` int NULL DEFAULT 1 COMMENT '等级，第几级',
  `is_amount` int NULL DEFAULT 0 COMMENT '是否开启核算 1.开启 0.不开启',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1521 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科目模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_subject_template
-- ----------------------------
INSERT INTO `wk_finance_subject_template` VALUES (1331, '1001', '库存现金', 0, 1, 1, 1, NULL, 1, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1332, '1002', '银行存款', 0, 1, 1, 1, NULL, 1, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1333, '1012', '其他货币资金', 0, 1, 1, 1, NULL, 1, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1334, '1101', '短期投资', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1335, '110101', '股票', 1334, 1, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1336, '110102', '债券', 1334, 1, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1337, '110103', '基金', 1334, 1, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1338, '110110', '其他', 1334, 1, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1339, '1121', '应收票据', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1340, '1122', '应收账款', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1341, '1123', '预付账款', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1342, '1131', '应收股利', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1343, '1132', '应收利息', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1344, '1221', '其他应收款', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1345, '1401', '材料采购', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1346, '1402', '在途物资', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1347, '1403', '原材料', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1348, '1404', '材料成本差异', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1349, '1405', '库存商品', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1350, '1407', '商品进销差价', 0, 1, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1351, '1408', '委托加工物资', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1352, '1411', '周转材料', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1353, '1421', '消耗性生物资产', 0, 1, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1354, '1501', '长期债券投资', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1355, '150101', '债券投资', 1354, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1356, '150102', '其他债权投资', 1354, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1357, '1511', '长期股权投资', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1358, '151101', '股票投资', 1357, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1359, '151102', '其他股权投资', 1357, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1360, '1601', '固定资产', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1361, '1602', '累计折旧', 0, 1, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1362, '1604', '在建工程', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1363, '160401', '建筑工程', 1362, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1364, '160402', '安装工程', 1362, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1365, '160403', '技术改造工程', 1362, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1366, '160404', '其他支出', 1362, 1, 2, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1367, '1605', '工程物资', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1368, '1606', '固定资产清理', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1369, '1621', '生产性生物资产', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1370, '1622', '生产性生物资产累计折旧', 0, 1, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1371, '1701', '无形资产', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1372, '1702', '累计摊销', 0, 1, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1373, '1801', '长期待摊费用', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1374, '1901', '待处理财产损溢', 0, 1, 2, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1375, '2001', '短期借款', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1376, '2201', '应付票据', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1377, '2202', '应付账款', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1378, '2203', '预收账款', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1379, '2211', '应付职工薪酬', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1380, '2221', '应交税费', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1381, '222101', '应交增值税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1382, '22210101', '进项税额', 1381, 2, 1, 1, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1383, '22210102', '销项税额的抵减', 1381, 2, 1, 1, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1384, '22210103', '已交税金', 1381, 2, 1, 1, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1385, '22210104', '转出未交增值税', 1381, 2, 1, 1, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1386, '22210105', '减免税款', 1381, 2, 1, 1, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1387, '22210106', '出口抵减内销产品应纳税额', 1381, 2, 1, 1, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1388, '22210107', '销项税额', 1381, 2, 1, 2, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1389, '22210108', '出口退税', 1381, 2, 1, 2, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1390, '22210109', '进项税额转出', 1381, 2, 1, 2, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1391, '22210110', '转出多交增值税', 1381, 2, 1, 2, NULL, 0, 1, 3, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1392, '222102', '未交增值税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1393, '222103', '预交增值税', 1380, 2, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1394, '222104', '待抵扣进项税额', 1380, 2, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1395, '222105', '待认证进项税额', 1380, 2, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1396, '222106', '待转销项税额', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1397, '222107', '增值税留抵税额', 1380, 2, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1398, '222108', '简易计税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1399, '222109', '转让金融商品应交增值税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1400, '222110', '代扣代交增值税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1401, '222111', '应交所得税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1402, '222112', '应交个人所得税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1403, '222113', '教育费附加', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1404, '222114', '地方教育费附加', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1405, '222115', '应交资源税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1406, '222116', '应交土地增值税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1407, '222117', '应交城市维护建设税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1408, '222118', '应交房产税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1409, '222119', '应交土地使用税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1410, '222120', '应交车船使用税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1411, '222121', '应交消费税', 1380, 2, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1412, '2231', '应付利息', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1413, '2232', '应付利润', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1414, '2241', '其他应付款', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1415, '2401', '递延收益', 0, 2, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1416, '2501', '长期借款', 0, 2, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1417, '2701', '长期应付款', 0, 2, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1418, '3001', '实收资本', 0, 3, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1419, '3002', '资本公积', 0, 3, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1420, '300201', '资本溢价', 1419, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1421, '300202', '接受捐赠非现金资产准备', 1419, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1422, '300206', '外币资本折算差额', 1419, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1423, '300207', '其他资本公积', 1419, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1424, '3101', '盈余公积', 0, 3, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1425, '310101', '法定盈余公积', 1424, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1426, '310102', '任意盈余公积', 1424, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1427, '310103', '法定公益金', 1424, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1428, '3103', '本年利润', 0, 3, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1429, '3104', '利润分配', 0, 3, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1430, '310401', '其他转入', 1429, 3, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1431, '310402', '提取法定盈余公积', 1429, 3, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1432, '310403', '提取法定公益金', 1429, 3, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1433, '310409', '提取任意盈余公积', 1429, 3, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1434, '310410', '应付利润', 1429, 3, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1435, '310411', '转作资本的利润', 1429, 3, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1436, '310415', '未分配利润', 1429, 3, 1, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1437, '4001', '生产成本', 0, 4, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1438, '400101', '基本生产成本', 1437, 4, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1439, '400102', '辅助生产成本', 1437, 4, 1, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1440, '4101', '制造费用', 0, 4, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1441, '4301', '研发支出', 0, 4, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1442, '4401', '工程施工', 0, 4, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1443, '4403', '机械作业', 0, 4, 1, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1444, '5001', '主营业务收入', 0, 5, 1, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1445, '5051', '其他业务收入', 0, 5, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1446, '5111', '投资收益', 0, 5, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1447, '5301', '营业外收入', 0, 5, 2, 2, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1448, '530101', '非流动资产处置净收益', 1447, 5, 2, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1449, '530102', '政府补助', 1447, 5, 2, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1450, '530103', '捐赠收益', 1447, 5, 2, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1451, '530104', '盘盈收益', 1447, 5, 2, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1452, '530105', '其他', 1447, 5, 2, 2, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1453, '5401', '主营业务成本', 0, 5, 5, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1454, '5402', '其他业务成本', 0, 5, 4, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1455, '5403', '税金及附加', 0, 5, 5, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1456, '540301', '消费税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1457, '540303', '城市维护建设税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1458, '540304', '资源税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1459, '540305', '土地增值税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1460, '540306', '城镇土地使用税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1461, '540307', '房产税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1462, '540308', '车船税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1463, '540309', '印花税', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1464, '540310', '教育费附加', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1465, '540311', '矿产资源补偿费', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1466, '540312', '排污费', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1467, '540313', '地方教育费附加', 1455, 5, 5, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1468, '5601', '销售费用', 0, 5, 3, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1469, '560101', '办公用品', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1470, '560102', '房租', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1471, '560103', '物业管理费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1472, '560104', '水电费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1473, '560105', '交际应酬费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1474, '560106', '市内交通费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1475, '560107', '差旅费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1476, '560108', '补助', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1477, '560109', '通讯费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1478, '560110', '工资', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1479, '560111', '佣金', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1480, '560112', '保险金', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1481, '560113', '福利费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1482, '560114', '累计折旧', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1483, '560115', '商品维修费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1484, '560116', '广告和业务宣传费', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1485, '560199', '其他', 1468, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1486, '5602', '管理费用', 0, 5, 3, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1487, '560201', '办公用品', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1488, '560202', '房租', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1489, '560203', '物业管理费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1490, '560204', '水电费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1491, '560205', '交际应酬费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1492, '560206', '市内交通费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1493, '560207', '差旅费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1494, '560208', '通讯费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1495, '560209', '工资', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1496, '560210', '保险金', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1497, '560211', '福利费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1498, '560212', '累计折旧', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1499, '560213', '开办费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1500, '560214', '职工教育经费', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1501, '560215', '研究费用', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1502, '560299', '其他', 1486, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1503, '5603', '财务费用', 0, 5, 3, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1504, '560301', '汇兑损益', 1503, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1505, '560302', '利息', 1503, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1506, '560303', '手续费', 1503, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1507, '560399', '其他', 1503, 5, 3, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1508, '5711', '营业外支出', 0, 5, 4, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1509, '571101', '存货盘亏毁损', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1510, '571102', '非流动资产处置净损失', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1511, '571103', '坏账损失', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1512, '571104', '无法收回的长期债券投资损失', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1513, '571105', '无法收回的长期股权投资损失', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1514, '571106', '自然灾害等不可抗力造成的损失', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1515, '571107', '税收滞纳金', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1516, '571108', '罚金、罚款', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1517, '571109', '捐赠支出', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1518, '571110', '其他', 1508, 5, 4, 1, NULL, 0, 1, 2, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1519, '5801', '所得税费用', 0, 5, 7, 1, NULL, 0, 1, 1, 0);
INSERT INTO `wk_finance_subject_template` VALUES (1520, '6000', '以前年度损益调整', 0, 5, 6, 1, NULL, 0, 1, 1, 0);

-- ----------------------------
-- Table structure for wk_finance_template
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_template`;
CREATE TABLE `wk_finance_template`  (
  `template_id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '凭证模板名称',
  `type_id` bigint NULL DEFAULT NULL COMMENT '凭证类别',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '凭证模板内容',
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`template_id`) USING BTREE,
  INDEX `type_id`(`type_id` ASC, `account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '凭证模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_template
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_template_type
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_template_type`;
CREATE TABLE `wk_finance_template_type`  (
  `type_id` bigint NOT NULL,
  `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '凭证名称',
  `account_id` bigint NULL DEFAULT NULL,
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`type_id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '凭证模板类型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_template_type
-- ----------------------------

-- ----------------------------
-- Table structure for wk_finance_voucher
-- ----------------------------
DROP TABLE IF EXISTS `wk_finance_voucher`;
CREATE TABLE `wk_finance_voucher`  (
  `voucher_id` bigint NOT NULL,
  `voucher_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '凭证字',
  `print_titles` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '打印标题',
  `is_default` int NULL DEFAULT NULL COMMENT '是否默认',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `account_id` bigint NULL DEFAULT NULL,
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`voucher_id`) USING BTREE,
  INDEX `company_id`(`account_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '凭证字' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wk_finance_voucher
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
