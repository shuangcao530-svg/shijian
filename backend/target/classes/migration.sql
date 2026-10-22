-- ============================================
-- 新增功能数据库迁移脚本 (MySQL 兼容版)
-- 执行方式：mysql -u root -p shijian < migration.sql
-- ============================================

-- 1. 通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
    `from_user_id` BIGINT COMMENT '触发通知的用户ID',
    `type` VARCHAR(32) NOT NULL COMMENT '通知类型',
    `target_type` VARCHAR(32) COMMENT '目标类型',
    `target_id` BIGINT COMMENT '目标ID',
    `content` VARCHAR(500) COMMENT '通知内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_read` (`user_id`, `is_read`),
    INDEX `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 2. 话题表
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '话题名称',
    `description` VARCHAR(500) COMMENT '话题描述',
    `icon` VARCHAR(255) COMMENT '话题图标',
    `post_count` INT DEFAULT 0 COMMENT '关联帖子数',
    `follow_count` INT DEFAULT 0 COMMENT '关注数',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门',
    `status` TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `idx_name` (`name`),
    INDEX `idx_hot` (`is_hot`, `post_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

-- 3. 帖子-话题关联表
DROP TABLE IF EXISTS `post_topic`;
CREATE TABLE `post_topic` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id` BIGINT NOT NULL,
    `topic_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_post` (`post_id`),
    INDEX `idx_topic` (`topic_id`),
    UNIQUE KEY `uk_post_topic` (`post_id`, `topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子话题关联表';

-- 4. 话题关注表
DROP TABLE IF EXISTS `topic_follow`;
CREATE TABLE `topic_follow` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `topic_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_topic` (`user_id`, `topic_id`),
    INDEX `idx_topic` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题关注表';

-- 5. 签到表
DROP TABLE IF EXISTS `checkin`;
CREATE TABLE `checkin` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `checkin_date` DATE NOT NULL COMMENT '签到日期',
    `consecutive_days` INT DEFAULT 1 COMMENT '连续签到天数',
    `points_awarded` INT DEFAULT 0 COMMENT '本次获得积分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到表';

-- 6. 积分记录表
DROP TABLE IF EXISTS `point_record`;
CREATE TABLE `point_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `points` INT NOT NULL COMMENT '积分变化',
    `reason` VARCHAR(100) NOT NULL COMMENT '原因',
    `target_type` VARCHAR(32) COMMENT '关联类型',
    `target_id` BIGINT COMMENT '关联ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 7. 用户表增加字段（使用存储过程安全添加）
DELIMITER //
DROP PROCEDURE IF EXISTS add_user_columns //
CREATE PROCEDURE add_user_columns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='user' AND column_name='points') THEN
        ALTER TABLE `user` ADD COLUMN `points` INT DEFAULT 0 COMMENT '总积分';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='user' AND column_name='level') THEN
        ALTER TABLE `user` ADD COLUMN `level` INT DEFAULT 1 COMMENT '用户等级';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='user' AND column_name='consecutive_checkin_days') THEN
        ALTER TABLE `user` ADD COLUMN `consecutive_checkin_days` INT DEFAULT 0 COMMENT '连续签到天数';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='user' AND column_name='last_checkin_date') THEN
        ALTER TABLE `user` ADD COLUMN `last_checkin_date` DATE COMMENT '最后签到日期';
    END IF;
END //
DELIMITER ;
CALL add_user_columns();
DROP PROCEDURE IF EXISTS add_user_columns;

-- 8. 敏感词表
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(32) DEFAULT 'GENERAL' COMMENT '分类',
    `severity` INT DEFAULT 1 COMMENT '严重程度',
    `status` TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `idx_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

INSERT IGNORE INTO `sensitive_word` (`word`, `category`, `severity`) VALUES
('广告推广', 'AD', 2),
('加微信', 'AD', 1),
('代考', 'GENERAL', 3),
('办证', 'AD', 2),
('赌博', 'GENERAL', 3),
('六合彩', 'GENERAL', 3),
('嫖', 'PORNOGRAPHIC', 3),
('约炮', 'PORNOGRAPHIC', 3),
('枪', 'GENERAL', 3),
('毒品', 'GENERAL', 3);

-- 9. 搜索历史表
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `keyword` VARCHAR(200) NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_time` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';

-- 10. 帖子表增加AI审核字段
DELIMITER //
DROP PROCEDURE IF EXISTS add_post_columns //
CREATE PROCEDURE add_post_columns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='post' AND column_name='ai_score') THEN
        ALTER TABLE `post` ADD COLUMN `ai_score` DECIMAL(3,2) COMMENT 'AI内容质量评分 0-5';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='post' AND column_name='ai_tags') THEN
        ALTER TABLE `post` ADD COLUMN `ai_tags` VARCHAR(500) COMMENT 'AI分析标签';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='post' AND column_name='audit_status') THEN
        ALTER TABLE `post` ADD COLUMN `audit_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'AI审核状态';
    END IF;
END //
DELIMITER ;
CALL add_post_columns();
DROP PROCEDURE IF EXISTS add_post_columns;

-- 11. 评论表增加AI审核字段
DELIMITER //
DROP PROCEDURE IF EXISTS add_comment_columns //
CREATE PROCEDURE add_comment_columns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='comment' AND column_name='ai_score') THEN
        ALTER TABLE `comment` ADD COLUMN `ai_score` DECIMAL(3,2) COMMENT 'AI内容质量评分';
    END IF;
END //
DELIMITER ;
CALL add_comment_columns();
DROP PROCEDURE IF EXISTS add_comment_columns;

-- 12. 举报表增加字段
DELIMITER //
DROP PROCEDURE IF EXISTS add_report_columns //
CREATE PROCEDURE add_report_columns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='report' AND column_name='feedback') THEN
        ALTER TABLE `report` ADD COLUMN `feedback` VARCHAR(500) COMMENT '处理反馈';
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema='shijian' AND table_name='report' AND column_name='resolved_time') THEN
        ALTER TABLE `report` ADD COLUMN `resolved_time` DATETIME COMMENT '处理完成时间';
    END IF;
END //
DELIMITER ;
CALL add_report_columns();
DROP PROCEDURE IF EXISTS add_report_columns;

-- 13. 初始化话题数据
INSERT IGNORE INTO `topic` (`name`, `description`, `is_hot`, `post_count`) VALUES
('#校园生活', '分享校园日常点滴', 1, 0),
('#学习经验', '学习方法与心得交流', 1, 0),
('#美食分享', '校园周边美食推荐', 1, 0),
('#社团活动', '社团招新与活动资讯', 0, 0),
('#新生攻略', '新生入学指南', 0, 0),
('#期末复习', '期末考试复习资料与经验', 0, 0),
('#校园风景', '校园美景随手拍', 0, 0),
('#兼职实习', '兼职与实习信息分享', 0, 0);
