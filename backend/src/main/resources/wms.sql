-- ============================================================
-- 仓库管理系统 (WMS) 数据库脚本
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS wms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wms_db;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`    VARCHAR(50) NOT NULL                COMMENT '用户名',
  `password`    VARCHAR(100) NOT NULL               COMMENT '密码',
  `real_name`   VARCHAR(50) DEFAULT NULL            COMMENT '真实姓名',
  `phone`       VARCHAR(20) DEFAULT NULL            COMMENT '手机号',
  `email`       VARCHAR(100) DEFAULT NULL           COMMENT '邮箱',
  `avatar`      VARCHAR(255) DEFAULT NULL           COMMENT '头像',
  `role`        VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin/manager/user',
  `status`      TINYINT     NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 供应商表
-- ----------------------------
DROP TABLE IF EXISTS `sys_supplier`;
CREATE TABLE `sys_supplier` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`         VARCHAR(100) NOT NULL               COMMENT '供应商名称',
  `contact`      VARCHAR(50)  DEFAULT NULL           COMMENT '联系人',
  `phone`        VARCHAR(20)  DEFAULT NULL           COMMENT '联系电话',
  `email`        VARCHAR(100) DEFAULT NULL           COMMENT '邮箱',
  `address`      VARCHAR(255) DEFAULT NULL           COMMENT '地址',
  `remark`       VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  `status`       TINYINT      NOT NULL DEFAULT 1     COMMENT '状态：1启用 0禁用',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ----------------------------
-- 仓库信息表
-- ----------------------------
DROP TABLE IF EXISTS `sys_warehouse`;
CREATE TABLE `sys_warehouse` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`         VARCHAR(100) NOT NULL               COMMENT '仓库名称',
  `location`     VARCHAR(255) DEFAULT NULL           COMMENT '仓库位置',
  `capacity`     DECIMAL(10,2) DEFAULT NULL          COMMENT '仓库容量',
  `manager_id`   BIGINT       DEFAULT NULL           COMMENT '管理员ID',
  `manager_name` VARCHAR(50)  DEFAULT NULL           COMMENT '管理员姓名',
  `remark`       VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  `status`       TINYINT      NOT NULL DEFAULT 1     COMMENT '状态：1启用 0禁用',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息表';

-- ----------------------------
-- 物资类型表
-- ----------------------------
DROP TABLE IF EXISTS `sys_material_type`;
CREATE TABLE `sys_material_type` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(100) NOT NULL               COMMENT '类型名称',
  `code`        VARCHAR(50)  DEFAULT NULL           COMMENT '类型编码',
  `remark`      VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  `status`      TINYINT      NOT NULL DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资类型表';

-- ----------------------------
-- 物资信息表
-- ----------------------------
DROP TABLE IF EXISTS `sys_material`;
CREATE TABLE `sys_material` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`         VARCHAR(100)  NOT NULL               COMMENT '物资名称',
  `code`         VARCHAR(50)   DEFAULT NULL           COMMENT '物资编码',
  `type_id`      BIGINT        DEFAULT NULL           COMMENT '物资类型ID',
  `type_name`    VARCHAR(100)  DEFAULT NULL           COMMENT '物资类型名称',
  `warehouse_id` BIGINT        DEFAULT NULL           COMMENT '所属仓库ID',
  `warehouse_name` VARCHAR(100) DEFAULT NULL          COMMENT '所属仓库名称',
  `unit`         VARCHAR(20)   DEFAULT NULL           COMMENT '单位',
  `quantity`     DECIMAL(10,2) DEFAULT 0              COMMENT '库存数量',
  `price`        DECIMAL(10,2) DEFAULT NULL           COMMENT '单价',
  `min_stock`    DECIMAL(10,2) NOT NULL DEFAULT 5.00  COMMENT '最低库存预警值',
  `image`        VARCHAR(255)  DEFAULT NULL           COMMENT '图片',
  `remark`       VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
  `status`       TINYINT       NOT NULL DEFAULT 1,
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资信息表';

-- ----------------------------
-- 入库信息表
-- ----------------------------
DROP TABLE IF EXISTS `sys_inbound`;
CREATE TABLE `sys_inbound` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inbound_no`    VARCHAR(50)   NOT NULL               COMMENT '入库单号',
  `material_id`   BIGINT        NOT NULL               COMMENT '物资ID',
  `material_name` VARCHAR(100)  DEFAULT NULL           COMMENT '物资名称',
  `warehouse_id`  BIGINT        DEFAULT NULL           COMMENT '仓库ID',
  `warehouse_name` VARCHAR(100) DEFAULT NULL           COMMENT '仓库名称',
  `supplier_id`   BIGINT        DEFAULT NULL           COMMENT '供应商ID',
  `supplier_name` VARCHAR(100)  DEFAULT NULL           COMMENT '供应商名称',
  `quantity`      DECIMAL(10,2) NOT NULL               COMMENT '入库数量',
  `price`         DECIMAL(10,2) DEFAULT NULL           COMMENT '单价',
  `total_amount`  DECIMAL(10,2) DEFAULT NULL           COMMENT '总金额',
  `operator_id`   BIGINT        DEFAULT NULL           COMMENT '操作员ID',
  `operator_name` VARCHAR(50)   DEFAULT NULL           COMMENT '操作员姓名',
  `inbound_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `remark`        VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库信息表';

-- ----------------------------
-- 出库信息表
-- ----------------------------
DROP TABLE IF EXISTS `sys_outbound`;
CREATE TABLE `sys_outbound` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `outbound_no`   VARCHAR(50)   NOT NULL               COMMENT '出库单号',
  `material_id`   BIGINT        NOT NULL               COMMENT '物资ID',
  `material_name` VARCHAR(100)  DEFAULT NULL           COMMENT '物资名称',
  `warehouse_id`  BIGINT        DEFAULT NULL           COMMENT '仓库ID',
  `warehouse_name` VARCHAR(100) DEFAULT NULL           COMMENT '仓库名称',
  `quantity`      DECIMAL(10,2) NOT NULL               COMMENT '出库数量',
  `receiver`      VARCHAR(50)   DEFAULT NULL           COMMENT '领用人',
  `operator_id`   BIGINT        DEFAULT NULL           COMMENT '操作员ID',
  `operator_name` VARCHAR(50)   DEFAULT NULL           COMMENT '操作员姓名',
  `outbound_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '出库时间',
  `remark`        VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库信息表';

-- ----------------------------
-- 物资申请表
-- ----------------------------
DROP TABLE IF EXISTS `sys_apply`;
CREATE TABLE `sys_apply` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no`      VARCHAR(50)   NOT NULL               COMMENT '申请单号',
  `material_id`   BIGINT        NOT NULL               COMMENT '物资ID',
  `material_name` VARCHAR(100)  DEFAULT NULL           COMMENT '物资名称',
  `quantity`      DECIMAL(10,2) NOT NULL               COMMENT '申请数量',
  `apply_reason`  VARCHAR(500)  DEFAULT NULL           COMMENT '申请原因',
  `user_id`       BIGINT        NOT NULL               COMMENT '申请人ID',
  `user_name`     VARCHAR(50)   DEFAULT NULL           COMMENT '申请人姓名',
  `status`        VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
  `approve_remark` VARCHAR(500) DEFAULT NULL           COMMENT '审批备注',
  `approve_time`  DATETIME      DEFAULT NULL           COMMENT '审批时间',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资申请表';

-- ----------------------------
-- 物资归还表
-- ----------------------------
DROP TABLE IF EXISTS `sys_return`;
CREATE TABLE `sys_return` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `return_no`     VARCHAR(50)   NOT NULL               COMMENT '归还单号',
  `material_id`   BIGINT        NOT NULL               COMMENT '物资ID',
  `material_name` VARCHAR(100)  DEFAULT NULL           COMMENT '物资名称',
  `quantity`      DECIMAL(10,2) NOT NULL               COMMENT '归还数量',
  `return_reason` VARCHAR(500)  DEFAULT NULL           COMMENT '归还原因',
  `user_id`       BIGINT        NOT NULL               COMMENT '归还人ID',
  `user_name`     VARCHAR(50)   DEFAULT NULL           COMMENT '归还人姓名',
  `status`        VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
  `approve_remark` VARCHAR(500) DEFAULT NULL           COMMENT '审批备注',
  `approve_time`  DATETIME      DEFAULT NULL           COMMENT '审批时间',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资归还表';

-- ----------------------------
-- 初始数据
-- ----------------------------
-- 初始用户（密码均为 123456，BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
('admin',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5.6', '系统管理员', '13800000001', 'admin',   1),
('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5.6', '仓库管理员', '13800000002', 'manager', 1),
('user1',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5.6', '张三',     '13800000003', 'user',    1);

-- 注意：上面密码哈希值仅为示例占位，实际应用启动时会用 DataInitializer 重新生成正确哈希

-- 初始供应商
INSERT INTO `sys_supplier` (`name`, `contact`, `phone`, `address`, `status`) VALUES
('北京物资科技有限公司', '李经理', '010-12345678', '北京市朝阳区科技园1号', 1),
('上海供应链管理有限公司', '王总', '021-87654321', '上海市浦东新区张江高科技园', 1),
('广州贸易集团', '陈主任', '020-11223344', '广州市天河区CBD中心', 1);

-- 初始仓库
INSERT INTO `sys_warehouse` (`name`, `location`, `capacity`, `manager_name`, `status`) VALUES
('A区主仓库', '厂区东侧A区', 5000.00, '仓库管理员', 1),
('B区备用仓库', '厂区西侧B区', 3000.00, '仓库管理员', 1),
('C区冷藏仓库', '厂区北侧C区', 1000.00, '仓库管理员', 1);

-- 初始物资类型
INSERT INTO `sys_material_type` (`name`, `code`, `remark`, `status`) VALUES
('办公用品', 'OFC', '日常办公所需物资', 1),
('电子设备', 'ELE', '电子类设备及配件', 1),
('劳保用品', 'SAF', '安全防护类物资', 1),
('清洁用品', 'CLN', '清洁卫生类物资', 1),
('维修工具', 'MNT', '维修维护类工具', 1);

-- 初始物资
INSERT INTO `sys_material` (`name`, `code`, `type_id`, `type_name`, `warehouse_id`, `warehouse_name`, `unit`, `quantity`, `price`, `status`) VALUES
('A4打印纸', 'MAT001', 1, '办公用品', 1, 'A区主仓库', '箱', 100.00, 45.00, 1),
('签字笔', 'MAT002', 1, '办公用品', 1, 'A区主仓库', '盒', 50.00, 15.00, 1),
('笔记本电脑', 'MAT003', 2, '电子设备', 1, 'A区主仓库', '台', 20.00, 5999.00, 1),
('安全帽', 'MAT004', 3, '劳保用品', 2, 'B区备用仓库', '个', 80.00, 35.00, 1),
('工作手套', 'MAT005', 3, '劳保用品', 2, 'B区备用仓库', '双', 200.00, 8.00, 1),
('拖把', 'MAT006', 4, '清洁用品', 1, 'A区主仓库', '把', 30.00, 25.00, 1),
('扳手套装', 'MAT007', 5, '维修工具', 2, 'B区备用仓库', '套', 15.00, 120.00, 1),
('移动硬盘', 'MAT008', 2, '电子设备', 1, 'A区主仓库', '个', 25.00, 350.00, 1);
