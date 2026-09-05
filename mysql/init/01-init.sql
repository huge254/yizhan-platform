-- 易栈数据库初始化脚本
-- 注意：管理员账号不再写死在本文件（原文档中的 BCrypt 哈希是无效的），
-- 改由后端启动时 DataInitializer 自动创建：admin / root123456

CREATE DATABASE IF NOT EXISTS yizhan DEFAULT CHARSET utf8mb4;
USE yizhan;

CREATE TABLE IF NOT EXISTS user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(50),
  role VARCHAR(20) DEFAULT 'USER',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS goods (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(500),
  status TINYINT DEFAULT 0 COMMENT '0-待审核 1-上架 2-下架',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_user (user_id)
);

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  goods_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  status TINYINT DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已完成',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_buyer (buyer_id)
);

CREATE TABLE IF NOT EXISTS comment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  goods_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_goods (goods_id)
);
