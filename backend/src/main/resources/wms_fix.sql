USE wms_db;

-- 修复字段长度
ALTER TABLE sys_user MODIFY real_name VARCHAR(100) DEFAULT NULL COMMENT '真实姓名';

-- 清空用户表重新插入（密码会由 DataInitializer 自动重新生成正确的 BCrypt 哈希）
DELETE FROM sys_user;

-- 插入初始用户（密码字段临时存 plain，启动后 DataInitializer 会用 BCrypt 重新设置）
INSERT INTO sys_user (username, password, real_name, phone, role, status, deleted) VALUES
('admin',   'TEMP_WILL_BE_RESET', '系统管理员', '13800000001', 'admin',   1, 0),
('manager', 'TEMP_WILL_BE_RESET', '仓库管理员', '13800000002', 'manager', 1, 0),
('user1',   'TEMP_WILL_BE_RESET', '张三',       '13800000003', 'user',    1, 0);

-- 插入供应商
DELETE FROM sys_supplier;
INSERT INTO sys_supplier (name, contact, phone, address, status, deleted) VALUES
('北京物资科技有限公司', '李经理', '010-12345678', '北京市朝阳区', 1, 0),
('上海供应链管理有限公司', '王总', '021-87654321', '上海市浦东新区', 1, 0),
('广州贸易集团', '陈主任', '020-11223344', '广州市天河区', 1, 0);

-- 插入仓库
DELETE FROM sys_warehouse;
INSERT INTO sys_warehouse (name, location, capacity, manager_name, status, deleted) VALUES
('A区主仓库', '厂区东侧A区', 5000.00, '仓库管理员', 1, 0),
('B区备用仓库', '厂区西侧B区', 3000.00, '仓库管理员', 1, 0),
('C区冷藏仓库', '厂区北侧C区', 1000.00, '仓库管理员', 1, 0);

-- 插入物资类型
DELETE FROM sys_material_type;
INSERT INTO sys_material_type (name, code, remark, status, deleted) VALUES
('办公用品', 'OFC', '日常办公所需物资', 1, 0),
('电子设备', 'ELE', '电子类设备及配件', 1, 0),
('劳保用品', 'SAF', '安全防护类物资', 1, 0),
('清洁用品', 'CLN', '清洁卫生类物资', 1, 0),
('维修工具', 'MNT', '维修维护类工具', 1, 0);

-- 插入物资
DELETE FROM sys_material;
INSERT INTO sys_material (name, code, type_id, type_name, warehouse_id, warehouse_name, unit, quantity, price, min_stock, status, deleted) VALUES
('A4打印纸', 'MAT001', 1, '办公用品', 1, 'A区主仓库', '箱', 100.00, 45.00, 20.00, 1, 0),
('签字笔', 'MAT002', 1, '办公用品', 1, 'A区主仓库', '盒', 50.00, 15.00, 60.00, 1, 0),
('笔记本电脑', 'MAT003', 2, '电子设备', 1, 'A区主仓库', '台', 20.00, 5999.00, 5.00, 1, 0),
('安全帽', 'MAT004', 3, '劳保用品', 2, 'B区备用仓库', '个', 80.00, 35.00, 100.00, 1, 0),
('工作手套', 'MAT005', 3, '劳保用品', 2, 'B区备用仓库', '双', 200.00, 8.00, 50.00, 1, 0),
('拖把', 'MAT006', 4, '清洁用品', 1, 'A区主仓库', '把', 30.00, 25.00, 10.00, 1, 0),
('扳手套装', 'MAT007', 5, '维修工具', 2, 'B区备用仓库', '套', 15.00, 120.00, 5.00, 1, 0),
('移动硬盘', 'MAT008', 2, '电子设备', 1, 'A区主仓库', '个', 25.00, 350.00, 5.00, 1, 0);

SELECT '=== 初始化完成 ===' AS result;
SELECT id, username, role, status FROM sys_user;
SELECT COUNT(*) AS material_count FROM sys_material;
