-- Music System Database Schema
DROP DATABASE IF EXISTS `musictest`;
CREATE DATABASE `musictest` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `musictest`;

-- 1. user table (backtick-escaped because 'user' is MySQL reserved word)
CREATE TABLE IF NOT EXISTS `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    sex VARCHAR(10) CHARACTER SET utf8mb4 DEFAULT NULL,
    phone_num VARCHAR(20) DEFAULT NULL,
    email VARCHAR(128) DEFAULT NULL,
    birth DATE DEFAULT NULL,
    introduction VARCHAR(512) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. song table
CREATE TABLE IF NOT EXISTS `song` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT 'uploader user id',
    name VARCHAR(128) DEFAULT NULL,
    introduction VARCHAR(512) DEFAULT NULL,
    status INT DEFAULT 0 COMMENT '0=pending,1=approved,2=rejected',
    audit_reason VARCHAR(255) DEFAULT NULL,
    play_count INT DEFAULT 0,
    collect_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pic VARCHAR(255) DEFAULT NULL COMMENT 'cover image path',
    lyric TEXT DEFAULT NULL,
    url VARCHAR(255) DEFAULT NULL COMMENT 'audio file path',
    duration INT DEFAULT NULL COMMENT 'song duration in seconds'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. post table (community posts)
CREATE TABLE IF NOT EXISTS `post` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) DEFAULT NULL,
    content TEXT DEFAULT NULL,
    cover VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status INT DEFAULT 0,
    audit_reason VARCHAR(255) DEFAULT NULL,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. collect table (song favorites/collections)
CREATE TABLE IF NOT EXISTS `collect` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    song_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. post_comment table
CREATE TABLE IF NOT EXISTS `post_comment` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    parent_id INT DEFAULT NULL COMMENT 'parent comment id for nested replies',
    like_count INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. post_support table (post likes/upvotes)
CREATE TABLE IF NOT EXISTS `post_support` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. song_comment table
CREATE TABLE IF NOT EXISTS `song_comment` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    song_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    parent_id INT DEFAULT NULL COMMENT 'parent comment id for nested replies',
    like_count INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. comment_like table (comment likes)
CREATE TABLE IF NOT EXISTS `comment_like` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    comment_id INT NOT NULL,
    comment_type VARCHAR(10) NOT NULL COMMENT 'song or post',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_comment_type (user_id, comment_id, comment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert test admin user (password: 123456, encoded with legacy MD5(SALT+password) = MD5('zyt123456'))
-- The system will auto-upgrade to BCrypt on first successful login
INSERT INTO `user` (username, password, sex, email, phone_num, introduction) VALUES
('admin', MD5(CONCAT('zyt', '123456')), '男', 'admin@music.com', '13800000000', '系统管理员');
