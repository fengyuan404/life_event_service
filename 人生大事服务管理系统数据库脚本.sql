-- 人生大事服务管理系统数据库脚本
-- MySQL 8.0

DROP DATABASE IF EXISTS life_event_service;
CREATE DATABASE life_event_service
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE life_event_service;

-- 1. 员工表
CREATE TABLE staff (
  staff_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工编号',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  password_hash VARCHAR(128) NOT NULL COMMENT '密码哈希值',
  staff_name VARCHAR(50) NOT NULL COMMENT '员工姓名',
  gender ENUM('男', '女') NOT NULL COMMENT '性别',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  role ENUM('admin', 'receptionist', 'finance') NOT NULL COMMENT '角色',
  status ENUM('active', 'disabled') NOT NULL DEFAULT 'active' COMMENT '账号状态',
  hire_date DATE NOT NULL COMMENT '入职日期',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='员工账号表';

-- 2. 家属表
CREATE TABLE family (
  family_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '家属编号',
  family_name VARCHAR(50) NOT NULL COMMENT '家属姓名',
  gender ENUM('男', '女') NOT NULL COMMENT '性别',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  id_card VARCHAR(30) NOT NULL UNIQUE COMMENT '证件号码',
  address VARCHAR(200) NOT NULL COMMENT '联系地址',
  remark VARCHAR(255) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间'
) ENGINE=InnoDB COMMENT='家属信息表';

-- 3. 逝者表
CREATE TABLE deceased (
  deceased_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '逝者编号',
  family_id INT NOT NULL COMMENT '家属编号',
  deceased_name VARCHAR(50) NOT NULL COMMENT '逝者姓名',
  gender ENUM('男', '女') NOT NULL COMMENT '性别',
  id_card VARCHAR(30) NOT NULL UNIQUE COMMENT '证件号码',
  birth_date DATE NOT NULL COMMENT '出生日期',
  death_date DATE NOT NULL COMMENT '离世日期',
  relation_to_family VARCHAR(30) NOT NULL COMMENT '与家属关系',
  epitaph VARCHAR(500) NULL COMMENT '碑文内容',
  burial_type ENUM('单穴', '双穴', '家庭穴') NOT NULL DEFAULT '单穴' COMMENT '安葬规格',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建档时间',
  CONSTRAINT fk_deceased_family
    FOREIGN KEY (family_id) REFERENCES family(family_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_deceased_date CHECK (death_date >= birth_date)
) ENGINE=InnoDB COMMENT='逝者档案表';

-- 4. 墓区表
CREATE TABLE grave_area (
  area_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '墓区编号',
  area_code VARCHAR(30) NOT NULL UNIQUE COMMENT '墓区编码',
  area_name VARCHAR(50) NOT NULL COMMENT '墓区名称',
  area_size DECIMAL(10,2) NOT NULL COMMENT '墓区面积',
  base_price DECIMAL(10,2) NOT NULL COMMENT '基础价格',
  environment_desc VARCHAR(255) NULL COMMENT '环境说明',
  status ENUM('active', 'disabled') NOT NULL DEFAULT 'active' COMMENT '启用状态',
  CONSTRAINT chk_area_size CHECK (area_size > 0),
  CONSTRAINT chk_area_price CHECK (base_price >= 0)
) ENGINE=InnoDB COMMENT='墓地区域表';

-- 5. 墓穴表
CREATE TABLE grave (
  grave_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '墓穴编号',
  area_id INT NOT NULL COMMENT '墓区编号',
  grave_code VARCHAR(30) NOT NULL UNIQUE COMMENT '墓穴编码',
  location_desc VARCHAR(100) NOT NULL COMMENT '位置描述',
  grave_size DECIMAL(8,2) NOT NULL COMMENT '墓穴面积',
  status ENUM('empty', 'rented', 'expired', 'maintenance') NOT NULL DEFAULT 'empty' COMMENT '墓穴状态',
  rent_price DECIMAL(10,2) NOT NULL COMMENT '租赁单价',
  max_years INT NOT NULL DEFAULT 20 COMMENT '最长使用年限',
  remark VARCHAR(255) NULL COMMENT '备注',
  CONSTRAINT fk_grave_area
    FOREIGN KEY (area_id) REFERENCES grave_area(area_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_grave_size CHECK (grave_size > 0),
  CONSTRAINT chk_grave_price CHECK (rent_price >= 0),
  CONSTRAINT chk_grave_years CHECK (max_years > 0)
) ENGINE=InnoDB COMMENT='墓穴资源表';

-- 6. 租赁记录表
CREATE TABLE rent_record (
  rent_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '租赁编号',
  family_id INT NOT NULL COMMENT '家属编号',
  deceased_id INT NOT NULL UNIQUE COMMENT '逝者编号',
  grave_id INT NOT NULL COMMENT '墓穴编号',
  staff_id INT NOT NULL COMMENT '经办员工编号',
  start_date DATE NOT NULL COMMENT '租赁开始日期',
  expire_date DATE NOT NULL COMMENT '租赁到期日期',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  rent_status ENUM('active', 'expired', 'cancelled') NOT NULL DEFAULT 'active' COMMENT '租赁状态',
  active_grave_id INT GENERATED ALWAYS AS (CASE WHEN rent_status = 'active' THEN grave_id ELSE NULL END) STORED COMMENT '有效租赁墓穴唯一校验字段',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_rent_active_grave (active_grave_id),
  CONSTRAINT fk_rent_family
    FOREIGN KEY (family_id) REFERENCES family(family_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_rent_deceased
    FOREIGN KEY (deceased_id) REFERENCES deceased(deceased_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_rent_grave
    FOREIGN KEY (grave_id) REFERENCES grave(grave_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_rent_staff
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_rent_date CHECK (expire_date > start_date),
  CONSTRAINT chk_rent_amount CHECK (total_amount >= 0)
) ENGINE=InnoDB COMMENT='安葬租赁记录表';

-- 7. 缴费记录表
CREATE TABLE payment (
  payment_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '缴费编号',
  rent_id INT NOT NULL COMMENT '租赁编号',
  staff_id INT NOT NULL COMMENT '收款员工编号',
  pay_type ENUM('租赁费', '管理费', '续费', '其他') NOT NULL COMMENT '缴费类型',
  pay_method ENUM('现金', '银行卡', '微信', '支付宝', '转账') NOT NULL COMMENT '缴费方式',
  pay_amount DECIMAL(10,2) NOT NULL COMMENT '缴费金额',
  pay_time DATETIME NOT NULL COMMENT '缴费时间',
  invoice_no VARCHAR(50) NOT NULL UNIQUE COMMENT '票据编号',
  remark VARCHAR(255) NULL COMMENT '备注',
  CONSTRAINT fk_payment_rent
    FOREIGN KEY (rent_id) REFERENCES rent_record(rent_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_payment_staff
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_pay_amount CHECK (pay_amount > 0)
) ENGINE=InnoDB COMMENT='缴费记录表';

-- 8. 祭扫预约表
CREATE TABLE sacrifice_book (
  book_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '预约编号',
  family_id INT NOT NULL COMMENT '家属编号',
  grave_id INT NOT NULL COMMENT '墓穴编号',
  staff_id INT NOT NULL COMMENT '登记员工编号',
  book_time DATETIME NOT NULL COMMENT '预约祭扫时间',
  visitor_count INT NOT NULL COMMENT '到场人数',
  special_need VARCHAR(255) NULL COMMENT '特殊需求',
  checkin_status ENUM('booked', 'checked_in', 'cancelled') NOT NULL DEFAULT 'booked' COMMENT '核销状态',
  checkin_time DATETIME NULL COMMENT '核销时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT fk_book_family
    FOREIGN KEY (family_id) REFERENCES family(family_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_book_grave
    FOREIGN KEY (grave_id) REFERENCES grave(grave_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_book_staff
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_book_visitors CHECK (visitor_count > 0)
) ENGINE=InnoDB COMMENT='祭扫预约表';

-- 业务索引
CREATE INDEX idx_grave_status ON grave(status);
CREATE INDEX idx_rent_expire ON rent_record(expire_date, rent_status);
CREATE INDEX idx_pay_time ON payment(pay_time);
CREATE INDEX idx_book_time ON sacrifice_book(book_time);

-- 测试数据：员工表 10 条
INSERT INTO staff (username, password_hash, staff_name, gender, phone, role, status, hire_date) VALUES
('admin01', 'hash_admin01', '张明', '男', '13800010001', 'admin', 'active', '2020-03-01'),
('admin02', 'hash_admin02', '李慧', '女', '13800010002', 'admin', 'active', '2021-05-12'),
('recv01', 'hash_recv01', '王强', '男', '13800010003', 'receptionist', 'active', '2021-07-20'),
('recv02', 'hash_recv02', '赵敏', '女', '13800010004', 'receptionist', 'active', '2022-01-10'),
('recv03', 'hash_recv03', '刘洋', '男', '13800010005', 'receptionist', 'active', '2022-04-18'),
('recv04', 'hash_recv04', '陈静', '女', '13800010006', 'receptionist', 'active', '2023-02-01'),
('fin01', 'hash_fin01', '孙婷', '女', '13800010007', 'finance', 'active', '2020-09-15'),
('fin02', 'hash_fin02', '周磊', '男', '13800010008', 'finance', 'active', '2021-11-03'),
('recv05', 'hash_recv05', '吴迪', '男', '13800010009', 'receptionist', 'disabled', '2019-06-21'),
('fin03', 'hash_fin03', '郑欣', '女', '13800010010', 'finance', 'active', '2023-08-16');

-- 测试数据：家属表 10 条
INSERT INTO family (family_name, gender, phone, id_card, address, remark) VALUES
('顾建国', '男', '13900020001', 'FAM20260001', '江城市东湖区春风路1号', '长子'),
('沈丽华', '女', '13900020002', 'FAM20260002', '江城市南山区桂花巷8号', '配偶'),
('唐伟', '男', '13900020003', 'FAM20260003', '江城市西城区人民路66号', '儿子'),
('何芳', '女', '13900020004', 'FAM20260004', '江城市北城区清河苑5栋', '女儿'),
('曹旭', '男', '13900020005', 'FAM20260005', '江城市滨江新区云澜湾12号', '孙子'),
('邓梅', '女', '13900020006', 'FAM20260006', '江城市开发区银杏路9号', '外甥女'),
('余海', '男', '13900020007', 'FAM20260007', '江城市东湖区文华街19号', '兄弟'),
('卢倩', '女', '13900020008', 'FAM20260008', '江城市南山区兰亭小区3栋', '妹妹'),
('蒋峰', '男', '13900020009', 'FAM20260009', '江城市新城大道88号', '侄子'),
('薛宁', '女', '13900020010', 'FAM20260010', '江城市老城区和平巷22号', '女儿');

-- 测试数据：逝者表 10 条
INSERT INTO deceased (family_id, deceased_name, gender, id_card, birth_date, death_date, relation_to_family, epitaph, burial_type) VALUES
(1, '顾德安', '男', 'DEC20260001', '1942-02-12', '2024-03-11', '父亲', '一生勤勉，厚德传家', '单穴'),
(2, '沈文清', '男', 'DEC20260002', '1938-09-20', '2023-12-05', '丈夫', '清风长存，音容永忆', '单穴'),
(3, '唐淑珍', '女', 'DEC20260003', '1945-05-08', '2025-01-16', '母亲', '慈爱一生，福泽后人', '双穴'),
(4, '何志远', '男', 'DEC20260004', '1940-11-18', '2025-04-09', '父亲', '志存高远，德范长留', '单穴'),
(5, '曹玉兰', '女', 'DEC20260005', '1936-01-23', '2022-10-30', '祖母', '兰心蕙质，福寿绵长', '家庭穴'),
(6, '邓启明', '男', 'DEC20260006', '1950-07-14', '2024-08-22', '舅舅', '明德惟馨，永志不忘', '单穴'),
(7, '余正华', '男', 'DEC20260007', '1948-03-09', '2023-05-19', '兄长', '正直仁厚，风范长存', '单穴'),
(8, '卢秀英', '女', 'DEC20260008', '1941-12-01', '2025-02-28', '母亲', '秀外慧中，恩泽家门', '双穴'),
(9, '蒋安平', '男', 'DEC20260009', '1939-06-06', '2021-09-14', '叔父', '安然长眠，平和一生', '单穴'),
(10, '薛桂香', '女', 'DEC20260010', '1944-10-10', '2024-11-03', '母亲', '桂馥兰香，慈恩永怀', '单穴');

-- 测试数据：墓区表 10 条
INSERT INTO grave_area (area_code, area_name, area_size, base_price, environment_desc, status) VALUES
('A', '松鹤园', 1200.00, 18000.00, '靠近主路，松柏环绕', 'active'),
('B', '长青园', 980.00, 16000.00, '地势平缓，适合祭扫', 'active'),
('C', '静思园', 860.00, 15000.00, '环境安静，绿化较高', 'active'),
('D', '明德园', 1100.00, 17500.00, '近服务中心，交通便利', 'active'),
('E', '怀恩园', 760.00, 14000.00, '小型片区，管理集中', 'active'),
('F', '福泽园', 1300.00, 20000.00, '景观较好，价格较高', 'active'),
('G', '安宁园', 900.00, 15500.00, '入口较近，预约较多', 'active'),
('H', '永念园', 820.00, 14800.00, '适合家庭穴规划', 'active'),
('I', '清晖园', 700.00, 13500.00, '新开放片区', 'active'),
('J', '追思园', 650.00, 13000.00, '维护中，暂缓新增租赁', 'disabled');

-- 测试数据：墓穴表 12 条
INSERT INTO grave (area_id, grave_code, location_desc, grave_size, status, rent_price, max_years, remark) VALUES
(1, 'A-001', '松鹤园1排1号', 1.20, 'rented', 22000.00, 20, '靠近步道'),
(1, 'A-002', '松鹤园1排2号', 1.20, 'rented', 22000.00, 20, '采光较好'),
(2, 'B-001', '长青园2排1号', 1.10, 'rented', 19000.00, 20, '常规单穴'),
(2, 'B-002', '长青园2排2号', 1.10, 'rented', 19000.00, 20, '常规单穴'),
(3, 'C-001', '静思园1排6号', 1.00, 'rented', 17500.00, 20, '安静区域'),
(4, 'D-001', '明德园3排3号', 1.30, 'rented', 23000.00, 25, '近服务中心'),
(5, 'E-001', '怀恩园1排5号', 1.00, 'rented', 16500.00, 20, '常规单穴'),
(6, 'F-001', '福泽园2排8号', 1.50, 'rented', 28000.00, 30, '景观位置'),
(7, 'G-001', '安宁园1排9号', 1.10, 'rented', 18800.00, 20, '入口较近'),
(8, 'H-001', '永念园4排2号', 1.80, 'rented', 32000.00, 30, '家庭穴'),
(9, 'I-001', '清晖园1排1号', 1.10, 'empty', 16800.00, 20, '新开放'),
(10, 'J-001', '追思园1排1号', 1.10, 'maintenance', 15000.00, 20, '维护中');

-- 测试数据：租赁记录表 10 条
INSERT INTO rent_record (family_id, deceased_id, grave_id, staff_id, start_date, expire_date, total_amount, rent_status) VALUES
(1, 1, 1, 3, '2024-03-15', '2044-03-14', 22000.00, 'active'),
(2, 2, 2, 4, '2023-12-10', '2043-12-09', 22000.00, 'active'),
(3, 3, 3, 5, '2025-01-20', '2045-01-19', 19000.00, 'active'),
(4, 4, 4, 6, '2025-04-12', '2045-04-11', 19000.00, 'active'),
(5, 5, 5, 3, '2022-11-02', '2042-11-01', 17500.00, 'active'),
(6, 6, 6, 4, '2024-08-25', '2049-08-24', 23000.00, 'active'),
(7, 7, 7, 5, '2023-05-22', '2043-05-21', 16500.00, 'active'),
(8, 8, 8, 6, '2025-03-03', '2055-03-02', 28000.00, 'active'),
(9, 9, 9, 3, '2021-09-18', '2026-07-10', 18800.00, 'active'),
(10, 10, 10, 4, '2024-11-06', '2026-08-20', 32000.00, 'active');

-- 测试数据：缴费记录表 12 条
INSERT INTO payment (rent_id, staff_id, pay_type, pay_method, pay_amount, pay_time, invoice_no, remark) VALUES
(1, 7, '租赁费', '银行卡', 22000.00, '2024-03-15 10:20:00', 'INV20240315001', '首期租赁费'),
(2, 7, '租赁费', '微信', 22000.00, '2023-12-10 15:30:00', 'INV20231210001', '首期租赁费'),
(3, 8, '租赁费', '转账', 19000.00, '2025-01-20 11:05:00', 'INV20250120001', '首期租赁费'),
(4, 8, '租赁费', '支付宝', 19000.00, '2025-04-12 09:40:00', 'INV20250412001', '首期租赁费'),
(5, 7, '租赁费', '现金', 17500.00, '2022-11-02 14:15:00', 'INV20221102001', '首期租赁费'),
(6, 10, '租赁费', '银行卡', 23000.00, '2024-08-25 16:10:00', 'INV20240825001', '首期租赁费'),
(7, 7, '租赁费', '微信', 16500.00, '2023-05-22 10:50:00', 'INV20230522001', '首期租赁费'),
(8, 8, '租赁费', '转账', 28000.00, '2025-03-03 13:20:00', 'INV20250303001', '首期租赁费'),
(9, 10, '租赁费', '银行卡', 18800.00, '2021-09-18 09:30:00', 'INV20210918001', '首期租赁费'),
(10, 7, '租赁费', '支付宝', 32000.00, '2024-11-06 12:00:00', 'INV20241106001', '首期租赁费'),
(1, 8, '管理费', '微信', 800.00, '2025-03-15 10:00:00', 'INV20250315002', '年度管理费'),
(9, 10, '续费', '转账', 5000.00, '2026-06-01 15:20:00', 'INV20260601001', '续费定金');

-- 测试数据：祭扫预约表 10 条
INSERT INTO sacrifice_book (family_id, grave_id, staff_id, book_time, visitor_count, special_need, checkin_status, checkin_time) VALUES
(1, 1, 3, '2026-04-04 09:00:00', 4, '需要鲜花代购', 'checked_in', '2026-04-04 08:55:00'),
(2, 2, 4, '2026-04-04 10:00:00', 3, '行动不便需轮椅', 'checked_in', '2026-04-04 09:50:00'),
(3, 3, 5, '2026-04-05 09:30:00', 5, '需要引导服务', 'checked_in', '2026-04-05 09:20:00'),
(4, 4, 6, '2026-04-05 11:00:00', 2, NULL, 'booked', NULL),
(5, 5, 3, '2026-06-25 09:00:00', 6, '需要停车位', 'booked', NULL),
(6, 6, 4, '2026-06-26 14:00:00', 4, NULL, 'booked', NULL),
(7, 7, 5, '2026-07-01 10:30:00', 3, '需要鲜花', 'booked', NULL),
(8, 8, 6, '2026-07-02 15:00:00', 8, '家庭集体祭扫', 'booked', NULL),
(9, 9, 3, '2026-07-03 09:30:00', 2, NULL, 'cancelled', NULL),
(10, 10, 4, '2026-07-04 16:00:00', 3, '需要清洁墓碑', 'booked', NULL);

-- 业务视图 1：空置墓穴
CREATE VIEW v_empty_grave AS
SELECT
  g.grave_id,
  g.grave_code,
  ga.area_name,
  g.location_desc,
  g.grave_size,
  g.rent_price,
  g.max_years
FROM grave g
JOIN grave_area ga ON g.area_id = ga.area_id
WHERE g.status = 'empty'
  AND ga.status = 'active';

-- 业务视图 2：租期即将到期或已到期订单
CREATE VIEW v_rent_expire AS
SELECT
  rr.rent_id,
  f.family_name,
  f.phone,
  d.deceased_name,
  g.grave_code,
  rr.start_date,
  rr.expire_date,
  rr.rent_status,
  DATEDIFF(rr.expire_date, CURDATE()) AS days_to_expire
FROM rent_record rr
JOIN family f ON rr.family_id = f.family_id
JOIN deceased d ON rr.deceased_id = d.deceased_id
JOIN grave g ON rr.grave_id = g.grave_id
WHERE rr.rent_status = 'active'
  AND rr.expire_date <= DATE_ADD(CURDATE(), INTERVAL 90 DAY);

-- 业务视图 3：月度收入统计
CREATE VIEW v_month_income AS
SELECT
  DATE_FORMAT(pay_time, '%Y-%m') AS income_month,
  pay_type,
  COUNT(*) AS payment_count,
  SUM(pay_amount) AS total_income
FROM payment
GROUP BY DATE_FORMAT(pay_time, '%Y-%m'), pay_type;

-- 查询语句示例：不少于 15 条

-- 1. 单表查询：查询全部在职业务接待员
SELECT staff_id, staff_name, phone
FROM staff
WHERE role = 'receptionist' AND status = 'active';

-- 2. 单表查询：查询所有空置墓穴
SELECT grave_code, location_desc, rent_price
FROM grave
WHERE status = 'empty';

-- 3. 单表查询：查询缴费金额大于 20000 的记录
SELECT payment_id, rent_id, pay_amount, pay_time
FROM payment
WHERE pay_amount > 20000;

-- 4. 多表连接：查询家属名下逝者及租用墓穴
SELECT f.family_name, f.phone, d.deceased_name, g.grave_code, ga.area_name, rr.expire_date
FROM family f
JOIN deceased d ON f.family_id = d.family_id
JOIN rent_record rr ON d.deceased_id = rr.deceased_id
JOIN grave g ON rr.grave_id = g.grave_id
JOIN grave_area ga ON g.area_id = ga.area_id
WHERE f.family_id = 1;

-- 5. 多表连接：查询租赁订单和经办员工
SELECT rr.rent_id, d.deceased_name, g.grave_code, s.staff_name, rr.total_amount
FROM rent_record rr
JOIN deceased d ON rr.deceased_id = d.deceased_id
JOIN grave g ON rr.grave_id = g.grave_id
JOIN staff s ON rr.staff_id = s.staff_id;

-- 6. 多表连接：查询缴费记录对应家属电话
SELECT p.invoice_no, f.family_name, f.phone, p.pay_type, p.pay_amount, p.pay_time
FROM payment p
JOIN rent_record rr ON p.rent_id = rr.rent_id
JOIN family f ON rr.family_id = f.family_id;

-- 7. 多表连接：查询祭扫预约详情
SELECT sb.book_id, f.family_name, g.grave_code, sb.book_time, sb.visitor_count, sb.checkin_status
FROM sacrifice_book sb
JOIN family f ON sb.family_id = f.family_id
JOIN grave g ON sb.grave_id = g.grave_id
ORDER BY sb.book_time;

-- 8. 嵌套查询：查询存在缴费记录的租赁订单
SELECT rent_id, family_id, deceased_id, total_amount
FROM rent_record
WHERE rent_id IN (SELECT rent_id FROM payment);

-- 9. 嵌套查询：查询租赁价格高于平均价格的墓穴
SELECT grave_code, rent_price
FROM grave
WHERE rent_price > (SELECT AVG(rent_price) FROM grave);

-- 10. 嵌套查询：查询 90 天内到期且仍为有效状态的订单
SELECT rent_id, expire_date, rent_status
FROM rent_record
WHERE rent_status = 'active'
  AND expire_date <= DATE_ADD(CURDATE(), INTERVAL 90 DAY);

-- 11. 聚合统计：按墓区统计墓穴数量和空置数量
SELECT ga.area_name,
       COUNT(g.grave_id) AS grave_count,
       SUM(CASE WHEN g.status = 'empty' THEN 1 ELSE 0 END) AS empty_count
FROM grave_area ga
LEFT JOIN grave g ON ga.area_id = g.area_id
GROUP BY ga.area_id, ga.area_name;

-- 12. 聚合统计：按月份统计收入
SELECT DATE_FORMAT(pay_time, '%Y-%m') AS income_month,
       SUM(pay_amount) AS total_income
FROM payment
GROUP BY DATE_FORMAT(pay_time, '%Y-%m')
ORDER BY income_month;

-- 13. 聚合统计：按缴费方式统计金额
SELECT pay_method, COUNT(*) AS pay_count, SUM(pay_amount) AS total_amount
FROM payment
GROUP BY pay_method;

-- 14. 条件筛选：查询未来 30 天祭扫预约
SELECT book_id, family_id, grave_id, book_time, visitor_count
FROM sacrifice_book
WHERE book_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 30 DAY)
  AND checkin_status = 'booked';

-- 15. 视图查询：查询空置墓穴视图
SELECT * FROM v_empty_grave;

-- 16. 视图查询：查询到期提醒视图
SELECT * FROM v_rent_expire ORDER BY days_to_expire;

-- 17. 视图查询：查询月度营收视图
SELECT * FROM v_month_income ORDER BY income_month;

-- 18. 综合查询：查询每名员工经办租赁数量
SELECT s.staff_name, s.role, COUNT(rr.rent_id) AS handled_rent_count
FROM staff s
LEFT JOIN rent_record rr ON s.staff_id = rr.staff_id
GROUP BY s.staff_id, s.staff_name, s.role
ORDER BY handled_rent_count DESC;

-- 修改语句示例：不少于 2 条

-- 1. 更新某租赁订单到期时间
UPDATE rent_record
SET expire_date = '2046-03-14'
WHERE rent_id = 1;

-- 2. 修改家属联系电话
UPDATE family
SET phone = '13999990001'
WHERE family_id = 1;

-- 3. 将维护完成的墓穴改为空置
UPDATE grave
SET status = 'empty', remark = '维护完成，可重新租赁'
WHERE grave_code = 'J-001';

-- 删除语句示例：使用业务软删除，避免破坏历史数据

-- 1. 取消作废祭扫预约
UPDATE sacrifice_book
SET checkin_status = 'cancelled'
WHERE book_id = 4;

-- 2. 停用离职员工账号
UPDATE staff
SET status = 'disabled'
WHERE staff_id = 9;

-- 如教师要求展示 DELETE，可使用下面两条针对无依赖临时数据的示例：
INSERT INTO sacrifice_book (family_id, grave_id, staff_id, book_time, visitor_count, special_need)
VALUES (1, 1, 3, '2026-12-01 09:00:00', 2, 'DELETE演示数据');
DELETE FROM sacrifice_book
WHERE special_need = 'DELETE演示数据';

INSERT INTO family (family_name, gender, phone, id_card, address, remark)
VALUES ('测试家属', '男', '13999999999', 'FAM_TEST_DELETE', '测试地址', 'DELETE演示数据');
DELETE FROM family
WHERE id_card = 'FAM_TEST_DELETE';

-- 多用户权限管理
-- 注意：实际执行前可根据本机 MySQL 密码规范调整密码。
CREATE USER IF NOT EXISTS 'life_admin'@'localhost' IDENTIFIED BY 'Admin@123456';
CREATE USER IF NOT EXISTS 'life_sales'@'localhost' IDENTIFIED BY 'Sales@123456';
CREATE USER IF NOT EXISTS 'life_finance'@'localhost' IDENTIFIED BY 'Finance@123456';

GRANT ALL PRIVILEGES ON life_event_service.* TO 'life_admin'@'localhost';

GRANT SELECT, INSERT, UPDATE ON life_event_service.family TO 'life_sales'@'localhost';
GRANT SELECT, INSERT, UPDATE ON life_event_service.deceased TO 'life_sales'@'localhost';
GRANT SELECT, INSERT, UPDATE ON life_event_service.rent_record TO 'life_sales'@'localhost';
GRANT SELECT, INSERT, UPDATE ON life_event_service.sacrifice_book TO 'life_sales'@'localhost';
GRANT SELECT ON life_event_service.grave TO 'life_sales'@'localhost';
GRANT SELECT ON life_event_service.grave_area TO 'life_sales'@'localhost';
GRANT SELECT ON life_event_service.v_empty_grave TO 'life_sales'@'localhost';
GRANT SELECT ON life_event_service.v_rent_expire TO 'life_sales'@'localhost';

GRANT SELECT ON life_event_service.payment TO 'life_finance'@'localhost';
GRANT SELECT ON life_event_service.rent_record TO 'life_finance'@'localhost';
GRANT SELECT ON life_event_service.family TO 'life_finance'@'localhost';
GRANT SELECT ON life_event_service.v_month_income TO 'life_finance'@'localhost';

FLUSH PRIVILEGES;

-- 数据库备份与恢复命令示例
-- 备份：
-- mysqldump -u root -p --databases life_event_service > life_event_service_backup.sql
-- 恢复：
-- mysql -u root -p < life_event_service_backup.sql
