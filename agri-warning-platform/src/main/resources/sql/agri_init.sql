-- - =========================================
-- 农业病虫害综合平台数据库初始化脚本
-- 适用数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_general_ci
-- =========================================
drop DATABASE IF  EXISTS agri_pest_platform;
CREATE DATABASE IF NOT EXISTS agri_pest_platform
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE agri_pest_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- 4.1 user（用户表）
-- =========================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色：ADMIN / USER',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    CONSTRAINT `chk_user_role` CHECK (`role` IN ('ADMIN', 'USER')),
    CONSTRAINT `chk_user_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- =========================================
-- 4.2 crop（作物表）
-- =========================================
DROP TABLE IF EXISTS `crop`;
CREATE TABLE `crop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '作物唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '作物名称',
    `category` VARCHAR(50) NOT NULL COMMENT '作物分类',
    `intro` VARCHAR(255) DEFAULT NULL COMMENT '作物简要介绍',
    `description` TEXT DEFAULT NULL COMMENT '作物详细描述',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '作物图片地址',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_crop_name` (`name`),
    KEY `idx_crop_category` (`category`),
    CONSTRAINT `chk_crop_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作物表';

-- =========================================
-- 4.3 pest（病虫害表）
-- =========================================
DROP TABLE IF EXISTS `pest`;
CREATE TABLE `pest` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '病虫害唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '病虫害名称',
    `type` VARCHAR(20) NOT NULL COMMENT '病虫害类型：病害 / 虫害',
    `description` TEXT DEFAULT NULL COMMENT '病虫害详细描述',
    `symptoms` TEXT DEFAULT NULL COMMENT '病虫害症状',
    `cause` TEXT DEFAULT NULL COMMENT '病虫害成因',
    `prevention` TEXT DEFAULT NULL COMMENT '防治措施',
    `risk_level` VARCHAR(20) DEFAULT '中' COMMENT '风险等级：低 / 中 / 高',
    `season` VARCHAR(100) DEFAULT NULL COMMENT '高发季节：春 / 夏 / 秋 / 冬 / 全年',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pest_name` (`name`),
    KEY `idx_pest_type` (`type`),
    KEY `idx_pest_risk_level` (`risk_level`),
    KEY `idx_pest_season` (`season`),
    CONSTRAINT `chk_pest_type` CHECK (`type` IN ('病害', '虫害')),
    CONSTRAINT `chk_pest_risk_level` CHECK (`risk_level` IN ('低', '中', '高')),
    CONSTRAINT `chk_pest_season` CHECK (`season` IS NULL OR `season` IN ('春', '夏', '秋', '冬', '全年')),
    CONSTRAINT `chk_pest_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='病虫害表';

-- =========================================
-- 4.4 crop_pest_rel（作物与病虫害关联表）
-- =========================================
DROP TABLE IF EXISTS `crop_pest_rel`;
CREATE TABLE `crop_pest_rel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `crop_id` BIGINT NOT NULL COMMENT '作物ID，关联 crop 表',
    `pest_id` BIGINT NOT NULL COMMENT '病虫害ID，关联 pest 表',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_crop_pest_rel` (`crop_id`, `pest_id`),
    KEY `idx_crop_pest_rel_crop_id` (`crop_id`),
    KEY `idx_crop_pest_rel_pest_id` (`pest_id`),
    CONSTRAINT `fk_crop_pest_rel_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `fk_crop_pest_rel_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `chk_crop_pest_rel_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作物与病虫害关联表';


-- =========================================
-- 4.5 pest_environment_conditions（适宜病虫害发生的环境条件表）
-- =========================================
DROP TABLE IF EXISTS `pest_environment_conditions`;
CREATE TABLE `pest_environment_conditions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pest_id` BIGINT NOT NULL COMMENT '关联病虫害ID',
    `temperature_range` VARCHAR(100) DEFAULT NULL COMMENT '适宜温度范围',
    `humidity_range` VARCHAR(100) DEFAULT NULL COMMENT '适宜湿度范围',
    `environment_description` TEXT DEFAULT NULL COMMENT '环境条件描述',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_environment_conditions_pest_id` (`pest_id`),
    CONSTRAINT `fk_environment_conditions_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `chk_environment_conditions_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='适宜病虫害发生的环境条件表';

-- =========================================
-- 4.6 prewarning_rules（预警规则表）
-- =========================================
DROP TABLE IF EXISTS `prewarning_rules`;
CREATE TABLE `prewarning_rules` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `crop_id` BIGINT NOT NULL COMMENT '作物ID，关联 crop 表',
    `pest_id` BIGINT NOT NULL COMMENT '病虫害ID，关联 pest 表',
    `min_temp` DECIMAL(5,2) DEFAULT NULL COMMENT '最低温度',
    `max_temp` DECIMAL(5,2) DEFAULT NULL COMMENT '最高温度',
    `min_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '最低湿度',
    `max_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '最高湿度',
    `min_precipitation` DECIMAL(8,2) DEFAULT NULL COMMENT '最低降雨量',
    `max_precipitation` DECIMAL(8,2) DEFAULT NULL COMMENT '最高降雨量',
    `min_wind_speed` DECIMAL(5,2) DEFAULT NULL COMMENT '最低风速',
    `max_wind_speed` DECIMAL(5,2) DEFAULT NULL COMMENT '最高风速',
    `risk_level` VARCHAR(20) NOT NULL COMMENT '风险等级：低 / 中 / 高',
    `suggestion` TEXT DEFAULT NULL COMMENT '防治建议',
    `rule_status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '规则状态：ENABLED / DISABLED',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_prewarning_rules_crop_id` (`crop_id`),
    KEY `idx_prewarning_rules_pest_id` (`pest_id`),
    KEY `idx_prewarning_rules_risk_level` (`risk_level`),
    KEY `idx_prewarning_rules_rule_status` (`rule_status`),

    CONSTRAINT `fk_prewarning_rules_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `fk_prewarning_rules_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),

    CONSTRAINT `chk_prewarning_rules_risk_level` CHECK (`risk_level` IN ('低', '中', '高')),
    CONSTRAINT `chk_prewarning_rules_rule_status` CHECK (`rule_status` IN ('ENABLED', 'DISABLED')),
    CONSTRAINT `chk_prewarning_rules_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警规则表';

-- =========================================
-- 4.7 warning（预警表）
-- =========================================
DROP TABLE IF EXISTS `warning`;
CREATE TABLE `warning` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警ID',
    `title` VARCHAR(200) NOT NULL COMMENT '预警标题',
    `crop_id` BIGINT NOT NULL COMMENT '作物ID，关联 crop 表',
    `pest_id` BIGINT NOT NULL COMMENT '病虫害ID，关联 pest 表',
    `risk_level` VARCHAR(20) NOT NULL COMMENT '风险等级：低 / 中 / 高',
    `warning_type` VARCHAR(20) NOT NULL COMMENT '预警类型：TODAY / FORECAST',
    `warning_date` DATE NOT NULL COMMENT '预警对应日期',
    `rule_id` BIGINT NOT NULL COMMENT '命中的预警规则ID',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_warning_crop_id` (`crop_id`),
    KEY `idx_warning_pest_id` (`pest_id`),
    KEY `idx_warning_rule_id` (`rule_id`),
    KEY `idx_warning_risk_level` (`risk_level`),
    KEY `idx_warning_warning_type` (`warning_type`),
    KEY `idx_warning_warning_date` (`warning_date`),

    CONSTRAINT `fk_warning_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `fk_warning_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `fk_warning_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `prewarning_rules` (`id`),

    CONSTRAINT `chk_warning_risk_level` CHECK (`risk_level` IN ('低', '中', '高')),
    CONSTRAINT `chk_warning_warning_type` CHECK (`warning_type` IN ('TODAY', 'FORECAST')),
    CONSTRAINT `chk_warning_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警表';

-- =========================================
-- 4.8 lightweight_knowledge_base_enhanced_qa（轻量版知识库增强问答表）
-- =========================================
DROP TABLE IF EXISTS `lightweight_knowledge_base_enhanced_qa`;
CREATE TABLE `lightweight_knowledge_base_enhanced_qa` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识问答唯一标识',
    `question` TEXT NOT NULL COMMENT '知识库问题',
    `answer` LONGTEXT NOT NULL COMMENT '知识库答案',
    `pest_id` BIGINT DEFAULT NULL COMMENT '关联病虫害ID',
    `crop_id` BIGINT DEFAULT NULL COMMENT '关联作物ID',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_lightweight_kb_pest_id` (`pest_id`),
    KEY `idx_lightweight_kb_crop_id` (`crop_id`),
    CONSTRAINT `fk_lightweight_kb_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `fk_lightweight_kb_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `chk_lightweight_kb_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='轻量版知识库增强问答表';

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 4.9 ai_chat_message（AI历史消息表）
-- =========================================
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `chat_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '消息角色：user / assistant',
    `content` LONGTEXT NOT NULL COMMENT '消息内容',
    `message_status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT '消息状态：STREAMING / COMPLETED / STOPPED / FAILED',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_ai_chat_message_chat_id` (`chat_id`),
    KEY `idx_ai_chat_message_user_id` (`user_id`),
    KEY `idx_ai_chat_message_chat_id_gmt_create` (`chat_id`, `gmt_create`),
    KEY `idx_ai_chat_message_delete_flag` (`delete_flag`),
    KEY `idx_ai_chat_message_status` (`message_status`),

    CONSTRAINT `fk_ai_chat_message_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_ai_chat_message_role` CHECK (`role` IN ('user', 'assistant')),
    CONSTRAINT `chk_ai_chat_message_status` CHECK (`message_status` IN ('STREAMING', 'COMPLETED', 'STOPPED', 'FAILED')),
    CONSTRAINT `chk_ai_chat_message_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI聊天消息表（悬浮助手与独立AI共用）';


-- =========================================
-- 4.10 ai_chat_session（AI会话记录表）
-- =========================================
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session` (
    `chat_id` VARCHAR(64) NOT NULL COMMENT '会话ID（前端生成）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型：CHAT',
    `context_type` VARCHAR(20) DEFAULT NULL COMMENT '上下文类型：CROP / PEST / WARNING / NONE',
    `context_id` BIGINT DEFAULT NULL COMMENT '上下文业务ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    KEY `idx_ai_chat_session_user_id` (`user_id`),
    KEY `idx_ai_chat_session_context_type_context_id` (`context_type`, `context_id`),
    KEY `idx_ai_chat_session_delete_flag` (`delete_flag`),

    CONSTRAINT `fk_ai_chat_session_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_ai_chat_session_session_type` CHECK (`session_type` IN ('CHAT')),
    CONSTRAINT `chk_ai_chat_session_context_type` CHECK (`context_type` IS NULL OR `context_type` IN ('CROP', 'PEST', 'WARNING', 'NONE')),
    CONSTRAINT `chk_ai_chat_session_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI会话表（仅独立AI问答使用）';



ALTER TABLE warning
ADD UNIQUE KEY uk_warning_unique (
    crop_id,
    pest_id,
    rule_id,
    warning_type,
    warning_date,
    delete_flag
);


ALTER TABLE ai_chat_session
ADD UNIQUE KEY uk_ai_chat_session_user_chat (user_id, chat_id, delete_flag);

ALTER TABLE ai_chat_message
ADD COLUMN message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT普通文本 / IMAGE_TEXT图文消息' AFTER content,
ADD COLUMN image_urls TEXT DEFAULT NULL COMMENT '图片URL列表，JSON数组字符串' AFTER message_type,
ADD COLUMN image_analysis TEXT DEFAULT NULL COMMENT '图片识别结果' AFTER image_urls;

ALTER TABLE `warning`
ADD COLUMN `risk_score` INT DEFAULT NULL COMMENT '风险指数：0-100' AFTER `risk_level`,
ADD COLUMN `risk_score_detail` TEXT DEFAULT NULL COMMENT '风险评分明细JSON' AFTER `risk_score`,
ADD COLUMN `match_detail` TEXT DEFAULT NULL COMMENT '命中依据详情JSON' AFTER `risk_score_detail`;

ALTER TABLE `warning`
ADD KEY `idx_warning_date_risk_level` (`warning_date`, `risk_level`, `delete_flag`),
ADD KEY `idx_warning_date_risk_score` (`warning_date`, `risk_score`, `delete_flag`);

DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    `user_id` BIGINT NOT NULL COMMENT '反馈用户ID',

    `target_type` VARCHAR(30) NOT NULL COMMENT '反馈目标类型：WARNING / AI_IMAGE / AI_CHAT',
    `target_id` BIGINT NOT NULL COMMENT '反馈目标ID：WARNING为warning.id，AI_IMAGE/AI_CHAT为ai_chat_message.id',

    `feedback_result` VARCHAR(30) NOT NULL COMMENT '反馈结果：YES / NO / UNCERTAIN',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '反馈补充说明，可为空',

    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),

    KEY `idx_feedback_user_id` (`user_id`),
    KEY `idx_feedback_target` (`target_type`, `target_id`),
    KEY `idx_feedback_result` (`feedback_result`),
    KEY `idx_feedback_delete_flag` (`delete_flag`),

    CONSTRAINT `fk_feedback_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_feedback_target_type` CHECK (`target_type` IN ('WARNING', 'AI_IMAGE', 'AI_CHAT')),
    CONSTRAINT `chk_feedback_result` CHECK (`feedback_result` IN ('YES', 'NO', 'UNCERTAIN')),
    CONSTRAINT `chk_feedback_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户反馈表';-- - =========================================
-- 农业病虫害综合平台数据库初始化脚本
-- 适用数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_general_ci
-- =========================================
drop DATABASE IF  EXISTS agri_pest_platform;
CREATE DATABASE IF NOT EXISTS agri_pest_platform
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE agri_pest_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- 4.1 user（用户表）
-- =========================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色：ADMIN / USER',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    CONSTRAINT `chk_user_role` CHECK (`role` IN ('ADMIN', 'USER')),
    CONSTRAINT `chk_user_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- =========================================
-- 4.2 crop（作物表）
-- =========================================
DROP TABLE IF EXISTS `crop`;
CREATE TABLE `crop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '作物唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '作物名称',
    `category` VARCHAR(50) NOT NULL COMMENT '作物分类',
    `intro` VARCHAR(255) DEFAULT NULL COMMENT '作物简要介绍',
    `description` TEXT DEFAULT NULL COMMENT '作物详细描述',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '作物图片地址',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_crop_name` (`name`),
    KEY `idx_crop_category` (`category`),
    CONSTRAINT `chk_crop_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作物表';

-- =========================================
-- 4.3 pest（病虫害表）
-- =========================================
DROP TABLE IF EXISTS `pest`;
CREATE TABLE `pest` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '病虫害唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '病虫害名称',
    `type` VARCHAR(20) NOT NULL COMMENT '病虫害类型：病害 / 虫害',
    `description` TEXT DEFAULT NULL COMMENT '病虫害详细描述',
    `symptoms` TEXT DEFAULT NULL COMMENT '病虫害症状',
    `cause` TEXT DEFAULT NULL COMMENT '病虫害成因',
    `prevention` TEXT DEFAULT NULL COMMENT '防治措施',
    `risk_level` VARCHAR(20) DEFAULT '中' COMMENT '风险等级：低 / 中 / 高',
    `season` VARCHAR(100) DEFAULT NULL COMMENT '高发季节：春 / 夏 / 秋 / 冬 / 全年',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pest_name` (`name`),
    KEY `idx_pest_type` (`type`),
    KEY `idx_pest_risk_level` (`risk_level`),
    KEY `idx_pest_season` (`season`),
    CONSTRAINT `chk_pest_type` CHECK (`type` IN ('病害', '虫害')),
    CONSTRAINT `chk_pest_risk_level` CHECK (`risk_level` IN ('低', '中', '高')),
    CONSTRAINT `chk_pest_season` CHECK (`season` IS NULL OR `season` IN ('春', '夏', '秋', '冬', '全年')),
    CONSTRAINT `chk_pest_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='病虫害表';

-- =========================================
-- 4.4 crop_pest_rel（作物与病虫害关联表）
-- =========================================
DROP TABLE IF EXISTS `crop_pest_rel`;
CREATE TABLE `crop_pest_rel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `crop_id` BIGINT NOT NULL COMMENT '作物ID，关联 crop 表',
    `pest_id` BIGINT NOT NULL COMMENT '病虫害ID，关联 pest 表',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_crop_pest_rel` (`crop_id`, `pest_id`),
    KEY `idx_crop_pest_rel_crop_id` (`crop_id`),
    KEY `idx_crop_pest_rel_pest_id` (`pest_id`),
    CONSTRAINT `fk_crop_pest_rel_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `fk_crop_pest_rel_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `chk_crop_pest_rel_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作物与病虫害关联表';


-- =========================================
-- 4.5 pest_environment_conditions（适宜病虫害发生的环境条件表）
-- =========================================
DROP TABLE IF EXISTS `pest_environment_conditions`;
CREATE TABLE `pest_environment_conditions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pest_id` BIGINT NOT NULL COMMENT '关联病虫害ID',
    `temperature_range` VARCHAR(100) DEFAULT NULL COMMENT '适宜温度范围',
    `humidity_range` VARCHAR(100) DEFAULT NULL COMMENT '适宜湿度范围',
    `environment_description` TEXT DEFAULT NULL COMMENT '环境条件描述',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_environment_conditions_pest_id` (`pest_id`),
    CONSTRAINT `fk_environment_conditions_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `chk_environment_conditions_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='适宜病虫害发生的环境条件表';

-- =========================================
-- 4.6 prewarning_rules（预警规则表）
-- =========================================
DROP TABLE IF EXISTS `prewarning_rules`;
CREATE TABLE `prewarning_rules` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `crop_id` BIGINT NOT NULL COMMENT '作物ID，关联 crop 表',
    `pest_id` BIGINT NOT NULL COMMENT '病虫害ID，关联 pest 表',
    `min_temp` DECIMAL(5,2) DEFAULT NULL COMMENT '最低温度',
    `max_temp` DECIMAL(5,2) DEFAULT NULL COMMENT '最高温度',
    `min_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '最低湿度',
    `max_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '最高湿度',
    `min_precipitation` DECIMAL(8,2) DEFAULT NULL COMMENT '最低降雨量',
    `max_precipitation` DECIMAL(8,2) DEFAULT NULL COMMENT '最高降雨量',
    `min_wind_speed` DECIMAL(5,2) DEFAULT NULL COMMENT '最低风速',
    `max_wind_speed` DECIMAL(5,2) DEFAULT NULL COMMENT '最高风速',
    `risk_level` VARCHAR(20) NOT NULL COMMENT '风险等级：低 / 中 / 高',
    `suggestion` TEXT DEFAULT NULL COMMENT '防治建议',
    `rule_status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '规则状态：ENABLED / DISABLED',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_prewarning_rules_crop_id` (`crop_id`),
    KEY `idx_prewarning_rules_pest_id` (`pest_id`),
    KEY `idx_prewarning_rules_risk_level` (`risk_level`),
    KEY `idx_prewarning_rules_rule_status` (`rule_status`),

    CONSTRAINT `fk_prewarning_rules_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `fk_prewarning_rules_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),

    CONSTRAINT `chk_prewarning_rules_risk_level` CHECK (`risk_level` IN ('低', '中', '高')),
    CONSTRAINT `chk_prewarning_rules_rule_status` CHECK (`rule_status` IN ('ENABLED', 'DISABLED')),
    CONSTRAINT `chk_prewarning_rules_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警规则表';

-- =========================================
-- 4.7 warning（预警表）
-- =========================================
DROP TABLE IF EXISTS `warning`;
CREATE TABLE `warning` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警ID',
    `title` VARCHAR(200) NOT NULL COMMENT '预警标题',
    `crop_id` BIGINT NOT NULL COMMENT '作物ID，关联 crop 表',
    `pest_id` BIGINT NOT NULL COMMENT '病虫害ID，关联 pest 表',
    `risk_level` VARCHAR(20) NOT NULL COMMENT '风险等级：低 / 中 / 高',
    `warning_type` VARCHAR(20) NOT NULL COMMENT '预警类型：TODAY / FORECAST',
    `warning_date` DATE NOT NULL COMMENT '预警对应日期',
    `rule_id` BIGINT NOT NULL COMMENT '命中的预警规则ID',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_warning_crop_id` (`crop_id`),
    KEY `idx_warning_pest_id` (`pest_id`),
    KEY `idx_warning_rule_id` (`rule_id`),
    KEY `idx_warning_risk_level` (`risk_level`),
    KEY `idx_warning_warning_type` (`warning_type`),
    KEY `idx_warning_warning_date` (`warning_date`),

    CONSTRAINT `fk_warning_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `fk_warning_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `fk_warning_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `prewarning_rules` (`id`),

    CONSTRAINT `chk_warning_risk_level` CHECK (`risk_level` IN ('低', '中', '高')),
    CONSTRAINT `chk_warning_warning_type` CHECK (`warning_type` IN ('TODAY', 'FORECAST')),
    CONSTRAINT `chk_warning_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警表';

-- =========================================
-- 4.8 lightweight_knowledge_base_enhanced_qa（轻量版知识库增强问答表）
-- =========================================
DROP TABLE IF EXISTS `lightweight_knowledge_base_enhanced_qa`;
CREATE TABLE `lightweight_knowledge_base_enhanced_qa` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识问答唯一标识',
    `question` TEXT NOT NULL COMMENT '知识库问题',
    `answer` LONGTEXT NOT NULL COMMENT '知识库答案',
    `pest_id` BIGINT DEFAULT NULL COMMENT '关联病虫害ID',
    `crop_id` BIGINT DEFAULT NULL COMMENT '关联作物ID',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_lightweight_kb_pest_id` (`pest_id`),
    KEY `idx_lightweight_kb_crop_id` (`crop_id`),
    CONSTRAINT `fk_lightweight_kb_pest_id` FOREIGN KEY (`pest_id`) REFERENCES `pest` (`id`),
    CONSTRAINT `fk_lightweight_kb_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`id`),
    CONSTRAINT `chk_lightweight_kb_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='轻量版知识库增强问答表';

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 4.9 ai_chat_message（AI历史消息表）
-- =========================================
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `chat_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '消息角色：user / assistant',
    `content` LONGTEXT NOT NULL COMMENT '消息内容',
    `message_status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT '消息状态：STREAMING / COMPLETED / STOPPED / FAILED',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_ai_chat_message_chat_id` (`chat_id`),
    KEY `idx_ai_chat_message_user_id` (`user_id`),
    KEY `idx_ai_chat_message_chat_id_gmt_create` (`chat_id`, `gmt_create`),
    KEY `idx_ai_chat_message_delete_flag` (`delete_flag`),
    KEY `idx_ai_chat_message_status` (`message_status`),

    CONSTRAINT `fk_ai_chat_message_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_ai_chat_message_role` CHECK (`role` IN ('user', 'assistant')),
    CONSTRAINT `chk_ai_chat_message_status` CHECK (`message_status` IN ('STREAMING', 'COMPLETED', 'STOPPED', 'FAILED')),
    CONSTRAINT `chk_ai_chat_message_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI聊天消息表（悬浮助手与独立AI共用）';


-- =========================================
-- 4.10 ai_chat_session（AI会话记录表）
-- =========================================
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session` (
    `chat_id` VARCHAR(64) NOT NULL COMMENT '会话ID（前端生成）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型：CHAT',
    `context_type` VARCHAR(20) DEFAULT NULL COMMENT '上下文类型：CROP / PEST / WARNING / NONE',
    `context_id` BIGINT DEFAULT NULL COMMENT '上下文业务ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    KEY `idx_ai_chat_session_user_id` (`user_id`),
    KEY `idx_ai_chat_session_context_type_context_id` (`context_type`, `context_id`),
    KEY `idx_ai_chat_session_delete_flag` (`delete_flag`),

    CONSTRAINT `fk_ai_chat_session_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_ai_chat_session_session_type` CHECK (`session_type` IN ('CHAT')),
    CONSTRAINT `chk_ai_chat_session_context_type` CHECK (`context_type` IS NULL OR `context_type` IN ('CROP', 'PEST', 'WARNING', 'NONE')),
    CONSTRAINT `chk_ai_chat_session_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI会话表（仅独立AI问答使用）';



ALTER TABLE warning
ADD UNIQUE KEY uk_warning_unique (
    crop_id,
    pest_id,
    rule_id,
    warning_type,
    warning_date,
    delete_flag
);


ALTER TABLE ai_chat_session
ADD UNIQUE KEY uk_ai_chat_session_user_chat (user_id, chat_id, delete_flag);

ALTER TABLE ai_chat_message
ADD COLUMN message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT普通文本 / IMAGE_TEXT图文消息' AFTER content,
ADD COLUMN image_urls TEXT DEFAULT NULL COMMENT '图片URL列表，JSON数组字符串' AFTER message_type,
ADD COLUMN image_analysis TEXT DEFAULT NULL COMMENT '图片识别结果' AFTER image_urls;

ALTER TABLE `warning`
ADD COLUMN `risk_score` INT DEFAULT NULL COMMENT '风险指数：0-100' AFTER `risk_level`,
ADD COLUMN `risk_score_detail` TEXT DEFAULT NULL COMMENT '风险评分明细JSON' AFTER `risk_score`,
ADD COLUMN `match_detail` TEXT DEFAULT NULL COMMENT '命中依据详情JSON' AFTER `risk_score_detail`;

ALTER TABLE `warning`
ADD KEY `idx_warning_date_risk_level` (`warning_date`, `risk_level`, `delete_flag`),
ADD KEY `idx_warning_date_risk_score` (`warning_date`, `risk_score`, `delete_flag`);

DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    `user_id` BIGINT NOT NULL COMMENT '反馈用户ID',

    `target_type` VARCHAR(30) NOT NULL COMMENT '反馈目标类型：WARNING / AI_IMAGE / AI_CHAT',
    `target_id` BIGINT NOT NULL COMMENT '反馈目标ID：WARNING为warning.id，AI_IMAGE/AI_CHAT为ai_chat_message.id',

    `feedback_result` VARCHAR(30) NOT NULL COMMENT '反馈结果：YES / NO / UNCERTAIN',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '反馈补充说明，可为空',

    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),

    KEY `idx_feedback_user_id` (`user_id`),
    KEY `idx_feedback_target` (`target_type`, `target_id`),
    KEY `idx_feedback_result` (`feedback_result`),
    KEY `idx_feedback_delete_flag` (`delete_flag`),

    CONSTRAINT `fk_feedback_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_feedback_target_type` CHECK (`target_type` IN ('WARNING', 'AI_IMAGE', 'AI_CHAT')),
    CONSTRAINT `chk_feedback_result` CHECK (`feedback_result` IN ('YES', 'NO', 'UNCERTAIN')),
    CONSTRAINT `chk_feedback_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户反馈表';


--初始化数据

-- =========================================
-- 农业病虫害综合平台：最终展示视频演示数据
-- 适用：MySQL 8.0+，基于你们现有表结构生成
-- 登录账号：
--   管理员：admin / 123456
--   普通用户：normal_user / 123456
-- 说明：
--   1）本脚本只插入演示数据，不创建表。
--   2）执行前请先确认已经执行建表脚本。
--   3）为了视频演示效果，已直接插入约四成 warning，其余可通过“手动生成预警”命中规则生成。
--   4）专业内容参考：全国农技中心水稻重大病虫害防控技术方案、广东/广州农业农村部门荔枝病虫害防治意见、湛江甘蔗等作物病虫害公开报道、香蕉病虫害资料等。
-- =========================================

USE agri_pest_platform;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `ai_chat_message`;
DELETE FROM `ai_chat_session`;
DELETE FROM `warning`;
DELETE FROM `lightweight_knowledge_base_enhanced_qa`;
DELETE FROM `prewarning_rules`;
DELETE FROM `pest_environment_conditions`;
DELETE FROM `crop_pest_rel`;
DELETE FROM `pest`;
DELETE FROM `crop`;
DELETE FROM `user`;

ALTER TABLE `user` AUTO_INCREMENT = 1;
ALTER TABLE `crop` AUTO_INCREMENT = 1;
ALTER TABLE `pest` AUTO_INCREMENT = 1;
ALTER TABLE `crop_pest_rel` AUTO_INCREMENT = 1;
ALTER TABLE `pest_environment_conditions` AUTO_INCREMENT = 1;
ALTER TABLE `prewarning_rules` AUTO_INCREMENT = 1;
ALTER TABLE `warning` AUTO_INCREMENT = 1;
ALTER TABLE `lightweight_knowledge_base_enhanced_qa` AUTO_INCREMENT = 1;
ALTER TABLE `ai_chat_message` AUTO_INCREMENT = 1;

INSERT INTO `user` (`id`,`username`,`role`,`password`,`delete_flag`,`gmt_create`,`gmt_modified`) VALUES
(1,'admin','ADMIN','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,'2026-04-01 08:00:00','2026-04-29 08:00:00'),
(2,'zhangwei','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,'2026-04-01 08:10:00','2026-04-29 08:10:00'),

(3,'liqiang','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(4,'wangjun','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(5,'chenhao','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(6,'huangtao','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(7,'linhai','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(8,'xujie','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(9,'zhoubo','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(10,'yangfan','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(11,'pengfei','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(12,'songtao','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(13,'dengwei','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(14,'luoming','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(15,'zhaolei','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(16,'sunpeng','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(17,'tangming','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(18,'gaoyuan','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(19,'fangjie','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(20,'zhenghao','USER','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(21,'liujun','ADMIN','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(22,'wangbo','ADMIN','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(23,'chenlin','ADMIN','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(24,'luowei','ADMIN','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW()),
(25,'huangbin','ADMIN','$2a$10$ryJxMh3V8pA7IfgKnW6ZZObiJYcLsF/lMXRm3M1ipZFh6vQDWGWgm',0,NOW(),NOW());

INSERT INTO `crop` (`id`, `name`, `category`, `intro`, `description`, `image_url`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
(1001, '水稻', '粮食作物', '湛江早晚造水稻主栽作物，受高温高湿、台风雨和迁飞性害虫影响明显。', '水稻是湛江重要粮食作物之一，生产上需重点关注稻飞虱、稻纵卷叶螟、纹枯病、稻瘟病、白叶枯病等。华南双季稻区温暖湿润，病虫害发生期长，雨后高湿和密植郁蔽环境会加重纹枯病、稻瘟病等流行，迁飞性害虫在季风和台风天气后也容易集中迁入。', 'https://dummyimage.com/640x360/7cb342/ffffff&text=Rice', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1002, '甘蔗', '糖料作物', '湛江传统优势糖料作物，苗期和伸长期需关注螟虫、黑穗病、赤腐病。', '甘蔗在湛江种植面积较大，温暖多雨环境有利于快速生长，也会促进螟虫、绵蚜、黑穗病、赤腐病等发生。生产上应重视健康种苗、合理密植、清除病残体和虫口监测。', 'https://dummyimage.com/640x360/8d6e63/ffffff&text=Sugarcane', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1003, '香蕉', '热带水果', '湛江热带水果代表作物，雨季高湿环境下叶斑病、炭疽病、枯萎病风险较高。', '香蕉喜高温湿润，湛江沿海台风雨季易形成高湿郁闭蕉园小气候，叶斑病、黑星病、炭疽病和枯萎病需长期监测；同时需关注象甲、蚜虫、红蜘蛛等害虫。', 'https://dummyimage.com/640x360/fdd835/000000&text=Banana', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1004, '菠萝', '热带水果', '湛江徐闻等地特色热带水果，需关注心腐病、粉蚧和果腐类病害。', '菠萝喜温暖、光照充足环境，雨季排水不良时心腐病、根腐病、果腐病风险上升；粉蚧等刺吸式害虫还可能诱发煤污和弱树。', 'https://dummyimage.com/640x360/ffb300/000000&text=Pineapple', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1005, '荔枝', '热带水果', '湛江常见岭南水果，果实发育期重点关注蒂蛀虫、霜疫霉病和蝽蟓。', '荔枝在花穗期、幼果期和成熟期病虫害差异明显。连续阴雨和高湿天气容易促进霜疫霉病扩展，果实发育期蒂蛀虫、蝽蟓、尺蠖等会影响产量和商品果率。', 'https://dummyimage.com/640x360/d32f2f/ffffff&text=Litchi', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1006, '龙眼', '热带水果', '湛江常见果树，花果期需关注蝽蟓、炭疽病和霜疫霉病。', '龙眼与荔枝生态位相近，温暖高湿环境下易发生炭疽病、霜疫霉病，花果期还应关注蝽蟓、蒂蛀虫、尺蠖等。', 'https://dummyimage.com/640x360/795548/ffffff&text=Longan', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1007, '花生', '油料作物', '湛江春花生和秋花生均有种植，需关注青枯病、叶斑病、蚜虫和蓟马。', '花生在高温多雨和土壤湿度大时青枯病、叶斑病、锈病风险提高；苗期蓟马、蚜虫为害可造成卷叶、黄化和病毒传播。', 'https://dummyimage.com/640x360/c49a6c/ffffff&text=Peanut', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1008, '番薯', '薯类作物', '湛江常见薯类作物，主要关注甘薯象甲、根腐病和线虫。', '番薯耐瘠薄但怕长期渍水，湛江温暖气候利于甘薯象甲持续繁殖，重茬地块还需关注根腐病、茎线虫等问题。', 'https://dummyimage.com/640x360/ab47bc/ffffff&text=Sweet+Potato', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1009, '辣椒', '蔬菜作物', '湛江露地蔬菜常见品类，高温高湿下疫病、炭疽病、蓟马、蚜虫风险较高。', '辣椒在雨季易受疫病、炭疽病、细菌性斑点病影响，蓟马和蚜虫不仅取食为害，还可能传播病毒病。', 'https://dummyimage.com/640x360/e53935/ffffff&text=Pepper', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1010, '番茄', '蔬菜作物', '湛江设施和露地均可栽培，重点关注晚疫病、青枯病、白粉虱和潜叶蛾。', '番茄对湿度和通风条件敏感，高湿低通风易诱发晚疫病、叶霉病，土壤连作和高温多雨时青枯病风险上升。', 'https://dummyimage.com/640x360/f4511e/ffffff&text=Tomato', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1011, '黄瓜', '蔬菜作物', '湛江常见瓜类蔬菜，高湿环境下霜霉病、白粉病、瓜实蝇较常见。', '黄瓜生长快、采收期长，连续阴雨或棚内结露易造成霜霉病、白粉病扩展，瓜实蝇和蓟马需结合诱捕与田间巡查。', 'https://dummyimage.com/640x360/43a047/ffffff&text=Cucumber', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00'),
(1012, '芒果', '热带水果', '湛江热区果树，花果期需关注炭疽病、细菌性角斑病和蓟马。', '芒果在花期和幼果期对降雨、湿度较敏感，高湿环境下炭疽病易侵染花穗和幼果，蓟马、叶蝉和果实蝇影响坐果和商品性。', 'https://dummyimage.com/640x360/ffa000/000000&text=Mango', 0, '2026-04-05 08:00:00', '2026-04-29 08:00:00');
INSERT INTO `pest` (`id`, `name`, `type`, `description`, `symptoms`, `cause`, `prevention`, `risk_level`, `season`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
(2001, '稻飞虱', '虫害', '华南稻区重要迁飞性害虫，常在水稻中后期集中发生。', '成若虫群集稻丛基部吸汁，稻株黄化、倒伏，严重时形成“冒穿”枯死斑。', '暖湿、郁闭、偏施氮肥和外来虫源迁入均会加重发生。', '保护天敌，浅水勤灌，控制氮肥；达到防治指标时选用对口药剂并轮换作用机理。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2002, '稻纵卷叶螟', '虫害', '水稻迁飞性害虫，幼虫卷叶取食，影响光合作用。', '叶片被吐丝纵卷，叶肉被刮食成白色条斑，严重时田间一片灰白。', '高温高湿、嫩绿稻株和虫源迁入有利于发生。', '灯诱和性诱监测，保护寄生蜂；低龄幼虫盛期精准防治。', '中', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2003, '水稻纹枯病', '病害', '水稻高湿郁闭条件下常发真菌性病害。', '叶鞘出现云纹状病斑，向上扩展后叶片枯黄，严重影响结实。', '高温高湿、密植、偏施氮肥、长期深水灌溉有利于病害扩展。', '合理密植，晒田控苗，减少氮肥；发病初期使用登记药剂均匀喷雾。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2004, '稻瘟病', '病害', '水稻重要真菌性病害，可危害叶片、节、穗颈。', '叶瘟呈梭形病斑，穗颈瘟造成白穗或秕谷。', '温暖高湿、连阴雨、雾露重和感病品种有利于发生。', '选用抗病品种，控制氮肥；破口抽穗期重视预防。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2005, '水稻白叶枯病', '病害', '台风雨后容易扩展的细菌性病害。', '叶缘先出现水渍状黄绿色斑，后沿叶缘扩展成黄白色枯条。', '强风暴雨造成伤口和田间积水有利于细菌传播。', '选抗病品种，避免串灌漫灌；台风雨后及时排水并用对口药剂预防。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2006, '甘蔗螟虫', '虫害', '甘蔗苗期和伸长期主要钻蛀性害虫。', '苗期造成枯心苗，伸长期蛀茎形成虫孔和虫粪，影响糖分积累。', '温暖环境、虫源基数高、残茬处理不彻底易导致发生。', '清除残茬，释放赤眼蜂，性诱监测；卵孵化盛期和低龄幼虫期防治。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2007, '甘蔗黑穗病', '病害', '甘蔗重要系统侵染病害，病株顶端抽出黑色鞭状物。', '病株矮化细弱，顶端长出黑鞭，破裂后散出黑粉状孢子。', '病蔗作种、宿根蔗管理差和感病品种易加重。', '使用健康种苗，温汤或药剂处理蔗种，发现病株连根拔除并带出田外。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2008, '香蕉叶斑病', '病害', '香蕉雨季高湿蕉园常见叶部病害。', '叶片出现褐色或黑褐色条斑、椭圆斑，严重时叶片早衰。', '高温高湿、通风差、病残叶积累有利于流行。', '割除病枯叶，改善通风；雨季按发生程度轮换使用保护性和内吸性杀菌剂。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2009, '香蕉枯萎病', '病害', '土传维管束病害，重茬蕉园风险较高。', '外叶黄化萎蔫，假茎纵切维管束褐变，后期整株枯死。', '病土、病苗、流水和农具传播；高温高湿、排水差加重。', '严格检疫和无病苗，病株隔离清除，改良土壤和轮作，避免病区带土传播。', '高', '全年', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2010, '香蕉炭疽病', '病害', '香蕉果实和叶片常见病害，采后贮运期也可发生。', '果面出现褐色凹陷斑，湿度大时产生粉红色黏孢子堆。', '高湿、多雨、机械伤口和采后管理不当有利于发生。', '减少伤口，清园降湿，适期采收；采后注意分级、消毒和通风。', '中', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2011, '菠萝心腐病', '病害', '菠萝雨季排水不良时常见病害。', '心叶变软腐烂，叶基部水渍状褐变，有时整株死亡。', '高湿积水、排水差、伤口和病原菌侵染有关。', '高畦种植，完善排水，避免伤根伤心；雨后及时排水并处理中心病株。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2012, '菠萝粉蚧', '虫害', '菠萝常见刺吸式害虫，可诱发弱株和煤污。', '群集叶腋、根颈和果实缝隙吸汁，分泌蜜露，植株衰弱。', '蚂蚁搬运、田间杂草和密植有利于扩散。', '清除杂草，控制蚂蚁，保护天敌；发生中心株及时点片防治。', '中', '全年', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2013, '荔枝蒂蛀虫', '虫害', '荔枝果实发育期关键害虫，直接造成落果和虫果。', '幼虫蛀入果蒂、果核附近，造成落果、蛀果和商品性下降。', '花果期虫源基数高、雨后湿度大和管理不及时会加重。', '掌握成虫羽化和卵孵高峰，结合落果调查，适期轮换用药，采前严格遵守安全间隔期。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2014, '荔枝霜疫霉病', '病害', '荔枝果穗和成熟果常见高湿病害。', '果面出现褐色水渍状斑，湿度大时生白色霉层，果实腐烂脱落。', '连续阴雨、果园郁闭、通风差和成熟期高湿有利于发生。', '修剪通风，雨前雨后预防，及时清除病果落果，注意药剂轮换。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2015, '荔枝蝽蟓', '虫害', '荔枝、龙眼常见刺吸式害虫。', '成若虫刺吸嫩梢、花穗和幼果，造成落花落果，分泌物可灼伤组织。', '越冬成虫基数高、春季回暖后活动增强。', '冬春清园，敲捕越冬成虫；若虫盛发期集中防治。', '中', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2016, '花生青枯病', '病害', '花生高温高湿条件下易发生的细菌性病害。', '植株白天萎蔫、夜间恢复，后期整株青枯，根茎部维管束褐变。', '连作、土壤湿度大、排水差和高温有利于发生。', '轮作换茬，高畦排水，使用健康种子；发病株及时拔除并处理病穴。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2017, '花生叶斑病', '病害', '花生中后期常见叶部病害。', '叶片出现圆形褐斑或黑褐斑，严重时大量落叶，影响饱果。', '高湿、连阴雨和田间郁闭有利于扩展。', '合理密植，清除病残体；发病初期用保护性杀菌剂预防扩展。', '中', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2018, '甘薯象甲', '虫害', '番薯重要仓储和田间害虫。', '幼虫蛀食薯块和茎基部，形成虫道，薯块苦臭不能食用。', '高温干旱、裂缝多和连作地块虫源积累易加重。', '使用无虫种苗，高培土减少裂缝，性诱监测，收获后清除残薯。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2019, '辣椒疫病', '病害', '辣椒雨季和排水差地块高发毁灭性病害。', '茎基部水渍状暗褐腐烂，叶片和果实也可腐烂，植株萎蔫死亡。', '高温高湿、积水、连作和雨水飞溅传播有利于发生。', '高畦深沟，雨后排水，轮作；发病中心及时处理并用对口药剂灌根或喷雾。', '高', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2020, '辣椒炭疽病', '病害', '辣椒果实成熟期常见病害。', '果面出现近圆形凹陷斑，湿度大时有橙红色黏孢子层。', '高温高湿、伤口、种子带菌和密植郁闭有利于发生。', '选用健康种子，通风降湿，及时摘除病果；发病初期轮换用药。', '中', '夏', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2021, '番茄晚疫病', '病害', '番茄低温高湿或连阴雨环境下易流行。', '叶片水渍状暗绿色病斑，湿度大时边缘有白霉，果实产生褐色硬斑。', '凉湿、昼夜温差大、结露和通风差促进流行。', '加强通风降湿，避免叶面长时间结露；发病前或初期及时预防。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2022, '白粉虱', '虫害', '番茄、黄瓜、辣椒等蔬菜常见刺吸式害虫。', '成若虫聚集叶背吸汁，叶片黄化，分泌蜜露诱发煤污，并可传播病毒。', '温暖、干湿交替、设施环境和杂草寄主有利于繁殖。', '黄板诱杀，清除杂草，保护天敌；必要时选择低风险药剂轮换防治。', '中', '全年', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2023, '黄瓜霜霉病', '病害', '瓜类蔬菜高湿条件下常发病害。', '叶片出现受叶脉限制的多角形黄斑，叶背潮湿时有灰黑色霉层。', '低温高湿、结露时间长、通风不良容易流行。', '合理密植，通风降湿，避免大水漫灌；发病初期喷施对口药剂。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2024, '黄瓜白粉病', '病害', '黄瓜中后期常见叶部病害。', '叶面出现白色粉状霉斑，逐渐扩大融合，叶片黄化早衰。', '干湿交替、通风差和植株长势弱时易发生。', '选抗病品种，及时摘除老病叶，保持通风透光，药剂交替使用。', '中', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2025, '芒果炭疽病', '病害', '芒果花期和果期重要病害。', '花穗变褐枯死，幼果黑斑脱落，成熟果出现黑褐色凹陷病斑。', '高湿、多雨、花期阴雨和果园郁闭有利于侵染。', '修剪通风，花前、谢花后和幼果期重点预防，采后减少伤口。', '高', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00'),
(2026, '蓟马', '虫害', '花生、辣椒、芒果等作物常见微小害虫。', '锉吸嫩叶、花器和幼果，造成银白斑、畸形、落花落果，也可传播病毒。', '高温偏干、嫩梢多和杂草寄主丰富有利于繁殖。', '蓝板诱杀，清除杂草，保护天敌；花期和嫩梢期加强监测。', '中', '春', 0, '2026-04-06 08:00:00', '2026-04-29 08:00:00');
INSERT INTO `crop_pest_rel` (`id`, `crop_id`, `pest_id`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
(4001, 1001, 2001, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4002, 1001, 2002, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4003, 1001, 2003, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4004, 1001, 2004, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4005, 1001, 2005, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4006, 1002, 2006, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4007, 1002, 2007, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4008, 1003, 2008, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4009, 1003, 2009, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4010, 1003, 2010, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4011, 1004, 2011, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4012, 1004, 2012, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4013, 1005, 2013, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4014, 1005, 2014, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4015, 1005, 2015, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4016, 1006, 2014, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4017, 1006, 2015, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4018, 1007, 2016, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4019, 1007, 2017, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4020, 1007, 2026, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4021, 1008, 2018, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4022, 1009, 2019, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4023, 1009, 2020, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4024, 1009, 2022, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4025, 1009, 2026, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4026, 1010, 2021, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4027, 1010, 2022, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4028, 1011, 2023, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4029, 1011, 2024, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4030, 1011, 2022, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4031, 1012, 2025, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00'),
(4032, 1012, 2026, 0, '2026-04-05 09:00:00', '2026-04-29 09:00:00');
INSERT INTO `pest_environment_conditions` (`id`, `pest_id`, `temperature_range`, `humidity_range`, `environment_description`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
(3001, 2001, '22–30℃', '70%–90%', '稻飞虱适宜温暖湿润、稻丛郁闭环境，迁入后在稻丛基部繁殖，偏施氮肥和田间湿度大时风险升高。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3002, 2002, '24–30℃', '70%–90%', '稻纵卷叶螟在高温高湿、稻株嫩绿条件下取食加重，雨后嫩叶多时低龄幼虫易集中危害。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3003, 2003, '24–32℃', '80%–95%', '纹枯病典型高温高湿病害，密植、长期深水、偏施氮肥和田间郁闭会加快病斑向上扩展。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3004, 2004, '20–28℃', '80%–95%', '稻瘟病在雾露重、阴雨多、叶面湿润时间长时易发生，感病品种和氮肥过多会加重。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3005, 2005, '25–32℃', '75%–95%', '白叶枯病在台风暴雨、伤口多、田间积水和串灌条件下更易传播。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3006, 2006, '22–30℃', '60%–85%', '甘蔗螟虫在温暖环境中发生早，卵孵化和低龄幼虫期是防治关键窗口。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3007, 2007, '20–30℃', '60%–85%', '甘蔗黑穗病以带菌种苗和土壤病残体传播为主，宿根蔗和感病品种风险更高。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3008, 2008, '24–32℃', '80%–95%', '香蕉叶斑病在雨季高湿、蕉园封行、通风差时流行，病枯叶是重要再侵染来源。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3009, 2009, '25–32℃', '70%–90%', '香蕉枯萎病为土传病害，高温高湿、排水差和病区带土作业会增加传播风险。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3010, 2010, '24–30℃', '80%–95%', '香蕉炭疽病在果实伤口、高湿和采后贮运通风差时发病明显。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3011, 2011, '23–30℃', '80%–95%', '菠萝心腐病在雨季、积水、中心叶伤口较多时易发生。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3012, 2012, '22–32℃', '60%–85%', '粉蚧在温暖、田间杂草和蚂蚁活动频繁时扩散快。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3013, 2013, '22–30℃', '70%–90%', '荔枝蒂蛀虫在果实发育期危害重，雨后湿度大、落果管理不及时会增加虫源。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3014, 2014, '20–28℃', '85%–98%', '荔枝霜疫霉病典型喜湿病害，连续阴雨、果园郁闭和果面长时间湿润时流行。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3015, 2015, '20–30℃', '60%–85%', '荔枝蝽蟓春季回暖后活动增强，若虫盛发期危害花穗和幼果。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3016, 2016, '25–35℃', '75%–95%', '花生青枯病在高温高湿、连作和排水不良土壤中容易暴发。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3017, 2017, '22–30℃', '75%–95%', '花生叶斑病在中后期、田间郁闭和多雨高湿条件下扩展快。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3018, 2018, '24–34℃', '50%–80%', '甘薯象甲喜温暖，干旱裂缝和残薯多时产卵、蛀食风险升高。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3019, 2019, '24–32℃', '80%–98%', '辣椒疫病在高温高湿、积水和雨水飞溅条件下传播迅速。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3020, 2020, '24–32℃', '80%–95%', '辣椒炭疽病在成熟期高湿、果面伤口和密植环境下发病重。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3021, 2021, '18–25℃', '80%–95%', '番茄晚疫病偏好凉湿和结露时间长的环境，通风差时发展快。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3022, 2022, '22–32℃', '50%–85%', '白粉虱在温暖环境和杂草寄主丰富时繁殖快，设施和露地蔬菜均需监测。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3023, 2023, '18–26℃', '85%–98%', '黄瓜霜霉病在低温高湿、叶面结露和通风差环境下易流行。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3024, 2024, '20–30℃', '60%–85%', '黄瓜白粉病在干湿交替、通风差和植株长势弱时常发生。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3025, 2025, '22–30℃', '80%–95%', '芒果炭疽病在花期阴雨、高湿和果园郁闭时侵染花穗和幼果。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00'),
(3026, 2026, '24–32℃', '45%–80%', '蓟马在高温偏干、嫩梢和花器较多时繁殖快，蓝板监测效果较好。', 0, '2026-04-01 09:00:00', '2026-04-29 09:00:00');
INSERT INTO `prewarning_rules` (`id`, `rule_name`, `crop_id`, `pest_id`, `min_temp`, `max_temp`, `min_humidity`, `max_humidity`, `min_precipitation`, `max_precipitation`, `min_wind_speed`, `max_wind_speed`, `risk_level`, `suggestion`, `rule_status`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
(5001, '水稻纹枯病今日中风险规则', 1001, 2003, 22, 28, 60, 70, 3, 10, 0, 25, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5002, '稻飞虱今日中风险规则', 1001, 2001, 22, 28, 55, 75, 0, 10, 10, 22, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5003, '香蕉叶斑病未来高湿高风险规则', 1003, 2008, 24, 31, 80, 92, 1, 25, 0, 22, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5004, '荔枝霜疫霉病未来阴雨高风险规则', 1005, 2014, 22, 29, 84, 95, 5, 25, 0, 18, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5005, '辣椒疫病未来降雨高风险规则', 1009, 2019, 23, 31, 80, 95, 5, 25, 0, 18, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5006, '黄瓜霜霉病未来高湿中风险规则', 1011, 2023, 20, 27, 84, 95, 0, 12, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5007, '花生叶斑病未来高湿中风险规则', 1007, 2017, 22, 30, 84, 95, 0, 20, 0, 16, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5008, '芒果炭疽病未来高湿高风险规则', 1012, 2025, 24, 31, 80, 95, 2, 12, 0, 18, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5009, '菠萝心腐病未来积水中风险规则', 1004, 2011, 23, 31, 82, 95, 5, 30, 0, 15, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5010, '香蕉炭疽病未来湿润中风险规则', 1003, 2010, 24, 31, 78, 92, 1, 12, 0, 20, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5011, '荔枝蒂蛀虫果期中风险规则', 1005, 2013, 22, 30, 70, 90, 0, 15, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5012, '甘薯象甲温暖低雨中风险规则', 1008, 2018, 24, 32, 50, 85, 0, 4, 0, 22, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5013, '稻纵卷叶螟温湿适宜规则', 1001, 2002, 24, 31, 75, 88, 0, 8, 0, 22, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5014, '水稻白叶枯病台风雨后关注规则', 1001, 2005, 24, 31, 82, 95, 5, 25, 8, 20, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5015, '甘蔗螟虫暖湿中风险规则', 1002, 2006, 22, 29, 60, 80, 0, 8, 0, 20, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5016, '香蕉枯萎病高温高湿提醒规则', 1003, 2009, 25, 32, 78, 92, 0, 20, 0, 25, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5017, '菠萝粉蚧温暖中风险规则', 1004, 2012, 24, 31, 60, 85, 0, 8, 0, 22, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5018, '荔枝蝽蟓春季中风险规则', 1005, 2015, 22, 30, 60, 88, 0, 10, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5019, '花生青枯病高温高湿规则', 1007, 2016, 25, 32, 78, 95, 2, 25, 0, 18, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5020, '辣椒炭疽病湿热中风险规则', 1009, 2020, 24, 32, 78, 95, 2, 20, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5021, '番茄白粉虱温暖低雨规则', 1010, 2022, 22, 31, 50, 82, 0, 3, 0, 20, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5022, '黄瓜白粉病干湿交替规则', 1011, 2024, 20, 30, 60, 88, 0, 4, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5023, '花生蓟马偏干中风险规则', 1007, 2026, 24, 32, 45, 78, 0, 3, 0, 20, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5024, '芒果蓟马花穗中风险规则', 1012, 2026, 24, 32, 45, 80, 0, 4, 0, 20, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5025, '龙眼霜疫霉病阴雨高风险规则', 1006, 2014, 24, 30, 84, 95, 5, 25, 0, 18, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5026, '龙眼蝽蟓春季中风险规则', 1006, 2015, 22, 30, 60, 88, 0, 10, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5027, '番茄晚疫病凉湿关注规则', 1010, 2021, 20, 27, 80, 95, 0, 10, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5028, '水稻稻瘟病阴湿中风险规则', 1001, 2004, 20, 28, 80, 95, 0, 10, 0, 18, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5029, '香蕉叶斑病强风雨后高风险规则', 1003, 2008, 24, 31, 80, 95, 5, 25, 12, 22, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5030, '辣椒疫病持续湿润高风险规则', 1009, 2019, 24, 31, 85, 95, 3, 25, 0, 16, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5031, '甘蔗黑穗病基础监测规则', 1002, 2007, 20, 30, 55, 85, NULL, NULL, NULL, NULL, '低', '建议保持常规巡查，做好田间卫生、排水和肥水管理，暂不建议大面积用药。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5032, '番茄晚疫病低温高湿高风险规则', 1010, 2021, 16, 23, 85, 98, 5, 30, 0, 15, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5033, '黄瓜霜霉病低温连阴雨高风险规则', 1011, 2023, 16, 24, 88, 98, 5, 30, 0, 15, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5034, '甘薯象甲干旱高风险规则', 1008, 2018, 27, 35, 35, 65, 0, 1, 0, 20, '高', '建议立即安排重点田块巡查，优先检查田间低洼、郁闭和历年重发区域；达到防治指标时及时采取农业防治、生物防治和合规药剂防治，并记录处置结果。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5035, '香蕉炭疽病停用演示规则', 1003, 2010, 24, 32, 80, 98, 0, 30, 0, 25, '中', '建议加强未来3天监测，关注病虫初发中心和天气变化；优先采用清园、通风降湿、诱捕和保护天敌等措施，必要时点片防治。', 'DISABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00'),
(5036, '荔枝蒂蛀虫低风险巡查规则', 1005, 2013, 20, 32, 50, 90, NULL, NULL, NULL, NULL, '低', '建议保持常规巡查，做好田间卫生、排水和肥水管理，暂不建议大面积用药。', 'ENABLED', 0, '2026-04-20 10:00:00', '2026-04-29 15:30:00');

INSERT INTO `lightweight_knowledge_base_enhanced_qa` (`id`, `question`, `answer`, `pest_id`, `crop_id`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
(8001, '稻飞虱主要危害什么作物，田间如何识别？', '稻飞虱属于虫害，在本系统中主要关联作物包括：水稻。识别要点：成若虫群集稻丛基部吸汁，稻株黄化、倒伏，严重时形成“冒穿”枯死斑。。发生原因和诱因：暖湿、郁闭、偏施氮肥和外来虫源迁入均会加重发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2001, 1001, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8002, '稻飞虱在湛江地区应该怎么综合防治？', '综合防治建议：保护天敌，浅水勤灌，控制氮肥；达到防治指标时选用对口药剂并轮换作用机理。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2001, 1001, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8003, '稻纵卷叶螟主要危害什么作物，田间如何识别？', '稻纵卷叶螟属于虫害，在本系统中主要关联作物包括：水稻。识别要点：叶片被吐丝纵卷，叶肉被刮食成白色条斑，严重时田间一片灰白。。发生原因和诱因：高温高湿、嫩绿稻株和虫源迁入有利于发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2002, 1001, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8004, '稻纵卷叶螟在湛江地区应该怎么综合防治？', '综合防治建议：灯诱和性诱监测，保护寄生蜂；低龄幼虫盛期精准防治。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2002, 1001, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8005, '水稻纹枯病主要危害什么作物，田间如何识别？', '水稻纹枯病属于病害，在本系统中主要关联作物包括：水稻。识别要点：叶鞘出现云纹状病斑，向上扩展后叶片枯黄，严重影响结实。。发生原因和诱因：高温高湿、密植、偏施氮肥、长期深水灌溉有利于病害扩展。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2003, 1001, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8006, '水稻纹枯病在湛江地区应该怎么综合防治？', '综合防治建议：合理密植，晒田控苗，减少氮肥；发病初期使用登记药剂均匀喷雾。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2003, 1001, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8007, '稻瘟病主要危害什么作物，田间如何识别？', '稻瘟病属于病害，在本系统中主要关联作物包括：水稻。识别要点：叶瘟呈梭形病斑，穗颈瘟造成白穗或秕谷。。发生原因和诱因：温暖高湿、连阴雨、雾露重和感病品种有利于发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2004, 1001, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8008, '稻瘟病在湛江地区应该怎么综合防治？', '综合防治建议：选用抗病品种，控制氮肥；破口抽穗期重视预防。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2004, 1001, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8009, '水稻白叶枯病主要危害什么作物，田间如何识别？', '水稻白叶枯病属于病害，在本系统中主要关联作物包括：水稻。识别要点：叶缘先出现水渍状黄绿色斑，后沿叶缘扩展成黄白色枯条。。发生原因和诱因：强风暴雨造成伤口和田间积水有利于细菌传播。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2005, 1001, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8010, '水稻白叶枯病在湛江地区应该怎么综合防治？', '综合防治建议：选抗病品种，避免串灌漫灌；台风雨后及时排水并用对口药剂预防。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2005, 1001, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8011, '甘蔗螟虫主要危害什么作物，田间如何识别？', '甘蔗螟虫属于虫害，在本系统中主要关联作物包括：甘蔗。识别要点：苗期造成枯心苗，伸长期蛀茎形成虫孔和虫粪，影响糖分积累。。发生原因和诱因：温暖环境、虫源基数高、残茬处理不彻底易导致发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2006, 1002, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8012, '甘蔗螟虫在湛江地区应该怎么综合防治？', '综合防治建议：清除残茬，释放赤眼蜂，性诱监测；卵孵化盛期和低龄幼虫期防治。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2006, 1002, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8013, '甘蔗黑穗病主要危害什么作物，田间如何识别？', '甘蔗黑穗病属于病害，在本系统中主要关联作物包括：甘蔗。识别要点：病株矮化细弱，顶端长出黑鞭，破裂后散出黑粉状孢子。。发生原因和诱因：病蔗作种、宿根蔗管理差和感病品种易加重。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2007, 1002, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8014, '甘蔗黑穗病在湛江地区应该怎么综合防治？', '综合防治建议：使用健康种苗，温汤或药剂处理蔗种，发现病株连根拔除并带出田外。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2007, 1002, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8015, '香蕉叶斑病主要危害什么作物，田间如何识别？', '香蕉叶斑病属于病害，在本系统中主要关联作物包括：香蕉。识别要点：叶片出现褐色或黑褐色条斑、椭圆斑，严重时叶片早衰。。发生原因和诱因：高温高湿、通风差、病残叶积累有利于流行。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2008, 1003, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8016, '香蕉叶斑病在湛江地区应该怎么综合防治？', '综合防治建议：割除病枯叶，改善通风；雨季按发生程度轮换使用保护性和内吸性杀菌剂。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2008, 1003, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8017, '香蕉枯萎病主要危害什么作物，田间如何识别？', '香蕉枯萎病属于病害，在本系统中主要关联作物包括：香蕉。识别要点：外叶黄化萎蔫，假茎纵切维管束褐变，后期整株枯死。。发生原因和诱因：病土、病苗、流水和农具传播；高温高湿、排水差加重。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2009, 1003, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8018, '香蕉枯萎病在湛江地区应该怎么综合防治？', '综合防治建议：严格检疫和无病苗，病株隔离清除，改良土壤和轮作，避免病区带土传播。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2009, 1003, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8019, '香蕉炭疽病主要危害什么作物，田间如何识别？', '香蕉炭疽病属于病害，在本系统中主要关联作物包括：香蕉。识别要点：果面出现褐色凹陷斑，湿度大时产生粉红色黏孢子堆。。发生原因和诱因：高湿、多雨、机械伤口和采后管理不当有利于发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2010, 1003, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8020, '香蕉炭疽病在湛江地区应该怎么综合防治？', '综合防治建议：减少伤口，清园降湿，适期采收；采后注意分级、消毒和通风。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2010, 1003, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8021, '菠萝心腐病主要危害什么作物，田间如何识别？', '菠萝心腐病属于病害，在本系统中主要关联作物包括：菠萝。识别要点：心叶变软腐烂，叶基部水渍状褐变，有时整株死亡。。发生原因和诱因：高湿积水、排水差、伤口和病原菌侵染有关。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2011, 1004, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8022, '菠萝心腐病在湛江地区应该怎么综合防治？', '综合防治建议：高畦种植，完善排水，避免伤根伤心；雨后及时排水并处理中心病株。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2011, 1004, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8023, '菠萝粉蚧主要危害什么作物，田间如何识别？', '菠萝粉蚧属于虫害，在本系统中主要关联作物包括：菠萝。识别要点：群集叶腋、根颈和果实缝隙吸汁，分泌蜜露，植株衰弱。。发生原因和诱因：蚂蚁搬运、田间杂草和密植有利于扩散。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2012, 1004, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8024, '菠萝粉蚧在湛江地区应该怎么综合防治？', '综合防治建议：清除杂草，控制蚂蚁，保护天敌；发生中心株及时点片防治。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2012, 1004, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8025, '荔枝蒂蛀虫主要危害什么作物，田间如何识别？', '荔枝蒂蛀虫属于虫害，在本系统中主要关联作物包括：荔枝。识别要点：幼虫蛀入果蒂、果核附近，造成落果、蛀果和商品性下降。。发生原因和诱因：花果期虫源基数高、雨后湿度大和管理不及时会加重。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2013, 1005, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8026, '荔枝蒂蛀虫在湛江地区应该怎么综合防治？', '综合防治建议：掌握成虫羽化和卵孵高峰，结合落果调查，适期轮换用药，采前严格遵守安全间隔期。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2013, 1005, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8027, '荔枝霜疫霉病主要危害什么作物，田间如何识别？', '荔枝霜疫霉病属于病害，在本系统中主要关联作物包括：荔枝、龙眼。识别要点：果面出现褐色水渍状斑，湿度大时生白色霉层，果实腐烂脱落。。发生原因和诱因：连续阴雨、果园郁闭、通风差和成熟期高湿有利于发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2014, 1005, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8028, '荔枝霜疫霉病在湛江地区应该怎么综合防治？', '综合防治建议：修剪通风，雨前雨后预防，及时清除病果落果，注意药剂轮换。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2014, 1005, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8029, '荔枝蝽蟓主要危害什么作物，田间如何识别？', '荔枝蝽蟓属于虫害，在本系统中主要关联作物包括：荔枝、龙眼。识别要点：成若虫刺吸嫩梢、花穗和幼果，造成落花落果，分泌物可灼伤组织。。发生原因和诱因：越冬成虫基数高、春季回暖后活动增强。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2015, 1005, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8030, '荔枝蝽蟓在湛江地区应该怎么综合防治？', '综合防治建议：冬春清园，敲捕越冬成虫；若虫盛发期集中防治。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2015, 1005, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8031, '花生青枯病主要危害什么作物，田间如何识别？', '花生青枯病属于病害，在本系统中主要关联作物包括：花生。识别要点：植株白天萎蔫、夜间恢复，后期整株青枯，根茎部维管束褐变。。发生原因和诱因：连作、土壤湿度大、排水差和高温有利于发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2016, 1007, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8032, '花生青枯病在湛江地区应该怎么综合防治？', '综合防治建议：轮作换茬，高畦排水，使用健康种子；发病株及时拔除并处理病穴。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2016, 1007, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8033, '花生叶斑病主要危害什么作物，田间如何识别？', '花生叶斑病属于病害，在本系统中主要关联作物包括：花生。识别要点：叶片出现圆形褐斑或黑褐斑，严重时大量落叶，影响饱果。。发生原因和诱因：高湿、连阴雨和田间郁闭有利于扩展。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2017, 1007, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8034, '花生叶斑病在湛江地区应该怎么综合防治？', '综合防治建议：合理密植，清除病残体；发病初期用保护性杀菌剂预防扩展。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2017, 1007, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8035, '甘薯象甲主要危害什么作物，田间如何识别？', '甘薯象甲属于虫害，在本系统中主要关联作物包括：番薯。识别要点：幼虫蛀食薯块和茎基部，形成虫道，薯块苦臭不能食用。。发生原因和诱因：高温干旱、裂缝多和连作地块虫源积累易加重。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2018, 1008, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8036, '甘薯象甲在湛江地区应该怎么综合防治？', '综合防治建议：使用无虫种苗，高培土减少裂缝，性诱监测，收获后清除残薯。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2018, 1008, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8037, '辣椒疫病主要危害什么作物，田间如何识别？', '辣椒疫病属于病害，在本系统中主要关联作物包括：辣椒。识别要点：茎基部水渍状暗褐腐烂，叶片和果实也可腐烂，植株萎蔫死亡。。发生原因和诱因：高温高湿、积水、连作和雨水飞溅传播有利于发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2019, 1009, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8038, '辣椒疫病在湛江地区应该怎么综合防治？', '综合防治建议：高畦深沟，雨后排水，轮作；发病中心及时处理并用对口药剂灌根或喷雾。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2019, 1009, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8039, '辣椒炭疽病主要危害什么作物，田间如何识别？', '辣椒炭疽病属于病害，在本系统中主要关联作物包括：辣椒。识别要点：果面出现近圆形凹陷斑，湿度大时有橙红色黏孢子层。。发生原因和诱因：高温高湿、伤口、种子带菌和密植郁闭有利于发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2020, 1009, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8040, '辣椒炭疽病在湛江地区应该怎么综合防治？', '综合防治建议：选用健康种子，通风降湿，及时摘除病果；发病初期轮换用药。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2020, 1009, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8041, '番茄晚疫病主要危害什么作物，田间如何识别？', '番茄晚疫病属于病害，在本系统中主要关联作物包括：番茄。识别要点：叶片水渍状暗绿色病斑，湿度大时边缘有白霉，果实产生褐色硬斑。。发生原因和诱因：凉湿、昼夜温差大、结露和通风差促进流行。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2021, 1010, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8042, '番茄晚疫病在湛江地区应该怎么综合防治？', '综合防治建议：加强通风降湿，避免叶面长时间结露；发病前或初期及时预防。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2021, 1010, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8043, '白粉虱主要危害什么作物，田间如何识别？', '白粉虱属于虫害，在本系统中主要关联作物包括：辣椒、番茄、黄瓜。识别要点：成若虫聚集叶背吸汁，叶片黄化，分泌蜜露诱发煤污，并可传播病毒。。发生原因和诱因：温暖、干湿交替、设施环境和杂草寄主有利于繁殖。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2022, 1009, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8044, '白粉虱在湛江地区应该怎么综合防治？', '综合防治建议：黄板诱杀，清除杂草，保护天敌；必要时选择低风险药剂轮换防治。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2022, 1009, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8045, '黄瓜霜霉病主要危害什么作物，田间如何识别？', '黄瓜霜霉病属于病害，在本系统中主要关联作物包括：黄瓜。识别要点：叶片出现受叶脉限制的多角形黄斑，叶背潮湿时有灰黑色霉层。。发生原因和诱因：低温高湿、结露时间长、通风不良容易流行。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2023, 1011, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8046, '黄瓜霜霉病在湛江地区应该怎么综合防治？', '综合防治建议：合理密植，通风降湿，避免大水漫灌；发病初期喷施对口药剂。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2023, 1011, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8047, '黄瓜白粉病主要危害什么作物，田间如何识别？', '黄瓜白粉病属于病害，在本系统中主要关联作物包括：黄瓜。识别要点：叶面出现白色粉状霉斑，逐渐扩大融合，叶片黄化早衰。。发生原因和诱因：干湿交替、通风差和植株长势弱时易发生。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2024, 1011, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8048, '黄瓜白粉病在湛江地区应该怎么综合防治？', '综合防治建议：选抗病品种，及时摘除老病叶，保持通风透光，药剂交替使用。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2024, 1011, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8049, '芒果炭疽病主要危害什么作物，田间如何识别？', '芒果炭疽病属于病害，在本系统中主要关联作物包括：芒果。识别要点：花穗变褐枯死，幼果黑斑脱落，成熟果出现黑褐色凹陷病斑。。发生原因和诱因：高湿、多雨、花期阴雨和果园郁闭有利于侵染。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2025, 1012, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8050, '芒果炭疽病在湛江地区应该怎么综合防治？', '综合防治建议：修剪通风，花前、谢花后和幼果期重点预防，采后减少伤口。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2025, 1012, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8051, '蓟马主要危害什么作物，田间如何识别？', '蓟马属于虫害，在本系统中主要关联作物包括：花生、辣椒、芒果。识别要点：锉吸嫩叶、花器和幼果，造成银白斑、畸形、落花落果，也可传播病毒。。发生原因和诱因：高温偏干、嫩梢多和杂草寄主丰富有利于繁殖。。巡查时建议优先检查低洼、郁闭、历年重发和长势偏嫩的田块，并记录发生部位、发生比例和风险等级。', 2026, 1007, 0, '2026-04-10 09:00:00', '2026-04-29 09:00:00'),
(8052, '蓟马在湛江地区应该怎么综合防治？', '综合防治建议：蓝板诱杀，清除杂草，保护天敌；花期和嫩梢期加强监测。。演示口径可概括为“先监测、再判断、后处置”：先结合天气、作物生育期和田间调查判断是否达到防治指标；再优先采取农业防治、清园排水、通风降湿、诱捕、生物防治等措施；确需用药时，应选择登记作物和对象的药剂，遵守标签、轮换作用机理，并注意安全间隔期。', 2026, 1007, 0, '2026-04-10 09:05:00', '2026-04-29 09:05:00'),
(8053, '湛江雨季为什么容易诱发病虫害预警？', '湛江属于华南沿海热区，春末夏初温度升高、湿度增大、降雨频繁，容易形成高温高湿和田间郁闭小气候。真菌性病害如水稻纹枯病、香蕉叶斑病、荔枝霜疫霉病、辣椒疫病等在叶面湿润时间较长时风险升高；迁飞性害虫如稻飞虱、稻纵卷叶螟也会受季风、台风外围气流和作物生育期影响而集中发生。', NULL, NULL, 0, '2026-04-28 10:00:00', '2026-04-29 10:00:00'),
(8054, '系统里的预警规则为什么要同时看温度、湿度、降雨和风速？', '单一气象因子很难准确描述病虫害发生环境。温度决定病原菌和昆虫发育速度，湿度和降雨影响孢子萌发、传播和叶面湿润时间，风速会影响虫源迁飞、病菌传播和强对流天气后的伤口风险。因此规则同时配置温度、湿度、降雨和风速，可以让预警更贴近真实农业场景。', NULL, NULL, 0, '2026-04-28 10:00:00', '2026-04-29 10:00:00'),
(8055, '今天湛江多云但有降雨，为什么仍会有水稻和蔬菜病害风险？', '即使天气描述为多云，只要日降雨、空气湿度和田间郁闭条件满足，病害仍可能发生。水稻纹枯病、辣椒疫病等更关注田间湿度、积水和植株郁闭，而不是只看天气文字描述。因此系统使用数值化天气指标进行匹配。', NULL, NULL, 0, '2026-04-28 10:00:00', '2026-04-29 10:00:00'),
(8056, '管理员手动生成预警时应该怎么看生成结果？', '手动生成预警适合在演示中展示“天气数据进入系统后，规则自动命中并生成预警”的过程。建议先展示当前已有预警，再点击手动生成；如果新增数量不为0，说明有部分规则此前未生成；如果重复规则被跳过，则说明系统去重机制生效，避免同一作物、病虫害、规则、日期重复预警。', NULL, NULL, 0, '2026-04-28 10:00:00', '2026-04-29 10:00:00');
INSERT INTO `ai_chat_session` (`chat_id`, `user_id`, `session_type`, `context_type`, `context_id`, `title`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
('admin_chat_01', 1, 'CHAT', 'NONE', NULL, '水稻纹枯病巡田判断', 0, '2026-04-29 09:17:00', '2026-04-29 09:17:00'),
('admin_chat_02', 1, 'CHAT', 'CROP', 1003, '香蕉叶斑病防治安排', 0, '2026-04-29 09:34:00', '2026-04-29 09:34:00'),
('admin_chat_03', 1, 'CHAT', 'PEST', 2013, '荔枝蒂蛀虫果期管理', 0, '2026-04-29 09:51:00', '2026-04-29 09:51:00'),
('admin_chat_04', 1, 'CHAT', 'NONE', NULL, '预警规则怎么配置', 0, '2026-04-29 10:08:00', '2026-04-29 10:08:00'),
('admin_chat_05', 1, 'CHAT', 'PEST', 2019, '辣椒疫病雨后处理', 0, '2026-04-29 10:25:00', '2026-04-29 10:25:00'),
('admin_chat_06', 1, 'CHAT', 'PEST', 2023, '黄瓜霜霉病识别', 0, '2026-04-29 10:42:00', '2026-04-29 10:42:00'),
('admin_chat_07', 1, 'CHAT', 'PEST', 2016, '花生青枯病和缺水区别', 0, '2026-04-29 10:59:00', '2026-04-29 10:59:00'),
('admin_chat_08', 1, 'CHAT', 'CROP', 1008, '甘薯象甲防控', 0, '2026-04-29 11:16:00', '2026-04-29 11:16:00'),
('admin_chat_09', 1, 'CHAT', 'PEST', 2021, '番茄晚疫病天气判断', 0, '2026-04-29 11:33:00', '2026-04-29 11:33:00'),
('admin_chat_10', 1, 'CHAT', 'NONE', NULL, '知识库回答来源说明', 0, '2026-04-29 11:50:00', '2026-04-29 11:50:00'),
('admin_chat_11', 1, 'CHAT', 'PEST', 2005, '水稻白叶枯病台风后', 0, '2026-04-29 12:07:00', '2026-04-29 12:07:00'),
('admin_chat_12', 1, 'CHAT', 'CROP', 1012, '芒果炭疽病花期', 0, '2026-04-29 12:24:00', '2026-04-29 12:24:00'),
('user_chat_01', 2, 'CHAT', 'NONE', NULL, '水稻纹枯病巡田判断', 0, '2026-04-29 09:23:00', '2026-04-29 09:23:00'),
('user_chat_02', 2, 'CHAT', 'CROP', 1003, '香蕉叶斑病防治安排', 0, '2026-04-29 09:40:00', '2026-04-29 09:40:00'),
('user_chat_03', 2, 'CHAT', 'PEST', 2013, '荔枝蒂蛀虫果期管理', 0, '2026-04-29 09:57:00', '2026-04-29 09:57:00'),
('user_chat_04', 2, 'CHAT', 'NONE', NULL, '预警规则怎么配置', 0, '2026-04-29 10:14:00', '2026-04-29 10:14:00'),
('user_chat_05', 2, 'CHAT', 'PEST', 2019, '辣椒疫病雨后处理', 0, '2026-04-29 10:31:00', '2026-04-29 10:31:00'),
('user_chat_06', 2, 'CHAT', 'PEST', 2023, '黄瓜霜霉病识别', 0, '2026-04-29 10:48:00', '2026-04-29 10:48:00'),
('user_chat_07', 2, 'CHAT', 'PEST', 2016, '花生青枯病和缺水区别', 0, '2026-04-29 11:05:00', '2026-04-29 11:05:00'),
('user_chat_08', 2, 'CHAT', 'CROP', 1008, '甘薯象甲防控', 0, '2026-04-29 11:22:00', '2026-04-29 11:22:00'),
('user_chat_09', 2, 'CHAT', 'PEST', 2021, '番茄晚疫病天气判断', 0, '2026-04-29 11:39:00', '2026-04-29 11:39:00'),
('user_chat_10', 2, 'CHAT', 'NONE', NULL, '知识库回答来源说明', 0, '2026-04-29 11:56:00', '2026-04-29 11:56:00'),
('user_chat_11', 2, 'CHAT', 'PEST', 2005, '水稻白叶枯病台风后', 0, '2026-04-29 12:13:00', '2026-04-29 12:13:00'),
('user_chat_12', 2, 'CHAT', 'CROP', 1012, '芒果炭疽病花期', 0, '2026-04-29 12:30:00', '2026-04-29 12:30:00');
INSERT INTO `ai_chat_message` (`id`, `chat_id`, `user_id`, `role`, `content`, `message_status`, `delete_flag`, `gmt_create`, `gmt_modified`) VALUES
(9001, 'admin_chat_01', 1, 'user', '今天湛江这种湿度，水稻纹枯病要不要重点看？', 'COMPLETED', 0, '2026-04-29 09:17:00', '2026-04-29 09:17:00'),
(9002, 'admin_chat_01', 1, 'assistant', '建议重点查看稻丛基部叶鞘，尤其是密植、偏施氮肥和低洼田块。当前湿度和小雨条件会提高纹枯病扩展风险。', 'COMPLETED', 0, '2026-04-29 09:17:12', '2026-04-29 09:17:12'),
(9003, 'admin_chat_01', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 09:17:50', '2026-04-29 09:17:50'),
(9004, 'admin_chat_01', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 09:18:02', '2026-04-29 09:18:02'),
(9005, 'admin_chat_02', 1, 'user', '香蕉叶片已经有褐色斑，雨后怎么处理？', 'COMPLETED', 0, '2026-04-29 09:34:00', '2026-04-29 09:34:00'),
(9006, 'admin_chat_02', 1, 'assistant', '先剪除严重病枯叶并带出园外，改善通风透光；雨季可结合发病程度使用登记杀菌剂，并注意轮换作用机理。', 'COMPLETED', 0, '2026-04-29 09:34:12', '2026-04-29 09:34:12'),
(9007, 'admin_chat_02', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 09:34:50', '2026-04-29 09:34:50'),
(9008, 'admin_chat_02', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 09:35:02', '2026-04-29 09:35:02'),
(9009, 'admin_chat_03', 1, 'user', '荔枝幼果期怎么判断蒂蛀虫要不要防？', 'COMPLETED', 0, '2026-04-29 09:51:00', '2026-04-29 09:51:00'),
(9010, 'admin_chat_03', 1, 'assistant', '建议结合落果解剖、成虫诱测和果穗调查。若蛀果、落果上升，应抓住卵孵化至低龄幼虫期防治。', 'COMPLETED', 0, '2026-04-29 09:51:12', '2026-04-29 09:51:12'),
(9011, 'admin_chat_03', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 09:51:50', '2026-04-29 09:51:50'),
(9012, 'admin_chat_03', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 09:52:02', '2026-04-29 09:52:02'),
(9013, 'admin_chat_04', 1, 'user', '预警规则里面降雨和湿度都要填吗？', 'COMPLETED', 0, '2026-04-29 10:08:00', '2026-04-29 10:08:00'),
(9014, 'admin_chat_04', 1, 'assistant', '不一定。若某个指标不是该规则核心因素可以留空；系统会只判断已配置的边界条件。', 'COMPLETED', 0, '2026-04-29 10:08:12', '2026-04-29 10:08:12'),
(9015, 'admin_chat_04', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 10:08:50', '2026-04-29 10:08:50'),
(9016, 'admin_chat_04', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 10:09:02', '2026-04-29 10:09:02'),
(9017, 'admin_chat_05', 1, 'user', '辣椒地雨后有积水，疫病风险高吗？', 'COMPLETED', 0, '2026-04-29 10:25:00', '2026-04-29 10:25:00'),
(9018, 'admin_chat_05', 1, 'assistant', '高。辣椒疫病与高湿、积水、雨水飞溅关系密切，建议先排水降湿，再检查茎基部水渍状病斑。', 'COMPLETED', 0, '2026-04-29 10:25:12', '2026-04-29 10:25:12'),
(9019, 'admin_chat_05', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 10:25:50', '2026-04-29 10:25:50'),
(9020, 'admin_chat_05', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 10:26:02', '2026-04-29 10:26:02'),
(9021, 'admin_chat_06', 1, 'user', '黄瓜叶子背面有灰黑色霉层是什么？', 'COMPLETED', 0, '2026-04-29 10:42:00', '2026-04-29 10:42:00'),
(9022, 'admin_chat_06', 1, 'assistant', '如果叶面有受叶脉限制的多角形黄斑，叶背潮湿时出现灰黑色霉层，比较符合黄瓜霜霉病特征。', 'COMPLETED', 0, '2026-04-29 10:42:12', '2026-04-29 10:42:12'),
(9023, 'admin_chat_06', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 10:42:50', '2026-04-29 10:42:50'),
(9024, 'admin_chat_06', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 10:43:02', '2026-04-29 10:43:02'),
(9025, 'admin_chat_07', 1, 'user', '花生白天萎蔫晚上恢复，是缺水还是病？', 'COMPLETED', 0, '2026-04-29 10:59:00', '2026-04-29 10:59:00'),
(9026, 'admin_chat_07', 1, 'assistant', '这种现象既可能是缺水，也可能是青枯病早期。若根茎部维管束褐变、拔株后病穴集中，应警惕青枯病。', 'COMPLETED', 0, '2026-04-29 10:59:12', '2026-04-29 10:59:12'),
(9027, 'admin_chat_07', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 10:59:50', '2026-04-29 10:59:50'),
(9028, 'admin_chat_07', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:00:02', '2026-04-29 11:00:02'),
(9029, 'admin_chat_08', 1, 'user', '番薯地怎么减少象甲？', 'COMPLETED', 0, '2026-04-29 11:16:00', '2026-04-29 11:16:00'),
(9030, 'admin_chat_08', 1, 'assistant', '用无虫种苗，及时培土减少裂缝，收获后清除残薯和藤蔓，必要时配合性诱监测。', 'COMPLETED', 0, '2026-04-29 11:16:12', '2026-04-29 11:16:12'),
(9031, 'admin_chat_08', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 11:16:50', '2026-04-29 11:16:50'),
(9032, 'admin_chat_08', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:17:02', '2026-04-29 11:17:02'),
(9033, 'admin_chat_09', 1, 'user', '番茄晚疫病是不是高温才严重？', 'COMPLETED', 0, '2026-04-29 11:33:00', '2026-04-29 11:33:00'),
(9034, 'admin_chat_09', 1, 'assistant', '不是。晚疫病更偏凉湿和长时间结露，低温高湿、通风差更容易流行。', 'COMPLETED', 0, '2026-04-29 11:33:12', '2026-04-29 11:33:12'),
(9035, 'admin_chat_09', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 11:33:50', '2026-04-29 11:33:50'),
(9036, 'admin_chat_09', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:34:02', '2026-04-29 11:34:02'),
(9037, 'admin_chat_10', 1, 'user', 'AI回答能不能引用知识库？', 'COMPLETED', 0, '2026-04-29 11:50:00', '2026-04-29 11:50:00'),
(9038, 'admin_chat_10', 1, 'assistant', '可以。系统会根据用户问题检索轻量知识库，再结合当前作物、病虫害或预警上下文生成回答。', 'COMPLETED', 0, '2026-04-29 11:50:12', '2026-04-29 11:50:12'),
(9039, 'admin_chat_10', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 11:50:50', '2026-04-29 11:50:50'),
(9040, 'admin_chat_10', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:51:02', '2026-04-29 11:51:02'),
(9041, 'admin_chat_11', 1, 'user', '台风雨后水稻叶缘发白要注意什么？', 'COMPLETED', 0, '2026-04-29 12:07:00', '2026-04-29 12:07:00'),
(9042, 'admin_chat_11', 1, 'assistant', '强风雨会造成伤口，利于白叶枯病传播。建议排水、避免串灌，并查看叶缘水渍状黄绿斑是否扩展。', 'COMPLETED', 0, '2026-04-29 12:07:12', '2026-04-29 12:07:12'),
(9043, 'admin_chat_11', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 12:07:50', '2026-04-29 12:07:50'),
(9044, 'admin_chat_11', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 12:08:02', '2026-04-29 12:08:02'),
(9045, 'admin_chat_12', 1, 'user', '芒果花期遇雨，炭疽病怎么预防？', 'COMPLETED', 0, '2026-04-29 12:24:00', '2026-04-29 12:24:00'),
(9046, 'admin_chat_12', 1, 'assistant', '重点是修剪通风、花前和谢花后预防，减少花穗长时间湿润。雨后应及时检查花穗褐变和幼果黑斑。', 'COMPLETED', 0, '2026-04-29 12:24:12', '2026-04-29 12:24:12'),
(9047, 'admin_chat_12', 1, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 12:24:50', '2026-04-29 12:24:50'),
(9048, 'admin_chat_12', 1, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 12:25:02', '2026-04-29 12:25:02'),
(9049, 'user_chat_01', 2, 'user', '今天湛江这种湿度，水稻纹枯病要不要重点看？', 'COMPLETED', 0, '2026-04-29 09:23:00', '2026-04-29 09:23:00'),
(9050, 'user_chat_01', 2, 'assistant', '建议重点查看稻丛基部叶鞘，尤其是密植、偏施氮肥和低洼田块。当前湿度和小雨条件会提高纹枯病扩展风险。', 'COMPLETED', 0, '2026-04-29 09:23:12', '2026-04-29 09:23:12'),
(9051, 'user_chat_01', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 09:23:50', '2026-04-29 09:23:50'),
(9052, 'user_chat_01', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 09:24:02', '2026-04-29 09:24:02'),
(9053, 'user_chat_02', 2, 'user', '香蕉叶片已经有褐色斑，雨后怎么处理？', 'COMPLETED', 0, '2026-04-29 09:40:00', '2026-04-29 09:40:00'),
(9054, 'user_chat_02', 2, 'assistant', '先剪除严重病枯叶并带出园外，改善通风透光；雨季可结合发病程度使用登记杀菌剂，并注意轮换作用机理。', 'COMPLETED', 0, '2026-04-29 09:40:12', '2026-04-29 09:40:12'),
(9055, 'user_chat_02', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 09:40:50', '2026-04-29 09:40:50'),
(9056, 'user_chat_02', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 09:41:02', '2026-04-29 09:41:02'),
(9057, 'user_chat_03', 2, 'user', '荔枝幼果期怎么判断蒂蛀虫要不要防？', 'COMPLETED', 0, '2026-04-29 09:57:00', '2026-04-29 09:57:00'),
(9058, 'user_chat_03', 2, 'assistant', '建议结合落果解剖、成虫诱测和果穗调查。若蛀果、落果上升，应抓住卵孵化至低龄幼虫期防治。', 'COMPLETED', 0, '2026-04-29 09:57:12', '2026-04-29 09:57:12'),
(9059, 'user_chat_03', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 09:57:50', '2026-04-29 09:57:50'),
(9060, 'user_chat_03', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 09:58:02', '2026-04-29 09:58:02'),
(9061, 'user_chat_04', 2, 'user', '预警规则里面降雨和湿度都要填吗？', 'COMPLETED', 0, '2026-04-29 10:14:00', '2026-04-29 10:14:00'),
(9062, 'user_chat_04', 2, 'assistant', '不一定。若某个指标不是该规则核心因素可以留空；系统会只判断已配置的边界条件。', 'COMPLETED', 0, '2026-04-29 10:14:12', '2026-04-29 10:14:12'),
(9063, 'user_chat_04', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 10:14:50', '2026-04-29 10:14:50'),
(9064, 'user_chat_04', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 10:15:02', '2026-04-29 10:15:02'),
(9065, 'user_chat_05', 2, 'user', '辣椒地雨后有积水，疫病风险高吗？', 'COMPLETED', 0, '2026-04-29 10:31:00', '2026-04-29 10:31:00'),
(9066, 'user_chat_05', 2, 'assistant', '高。辣椒疫病与高湿、积水、雨水飞溅关系密切，建议先排水降湿，再检查茎基部水渍状病斑。', 'COMPLETED', 0, '2026-04-29 10:31:12', '2026-04-29 10:31:12'),
(9067, 'user_chat_05', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 10:31:50', '2026-04-29 10:31:50'),
(9068, 'user_chat_05', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 10:32:02', '2026-04-29 10:32:02'),
(9069, 'user_chat_06', 2, 'user', '黄瓜叶子背面有灰黑色霉层是什么？', 'COMPLETED', 0, '2026-04-29 10:48:00', '2026-04-29 10:48:00'),
(9070, 'user_chat_06', 2, 'assistant', '如果叶面有受叶脉限制的多角形黄斑，叶背潮湿时出现灰黑色霉层，比较符合黄瓜霜霉病特征。', 'COMPLETED', 0, '2026-04-29 10:48:12', '2026-04-29 10:48:12'),
(9071, 'user_chat_06', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 10:48:50', '2026-04-29 10:48:50'),
(9072, 'user_chat_06', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 10:49:02', '2026-04-29 10:49:02'),
(9073, 'user_chat_07', 2, 'user', '花生白天萎蔫晚上恢复，是缺水还是病？', 'COMPLETED', 0, '2026-04-29 11:05:00', '2026-04-29 11:05:00'),
(9074, 'user_chat_07', 2, 'assistant', '这种现象既可能是缺水，也可能是青枯病早期。若根茎部维管束褐变、拔株后病穴集中，应警惕青枯病。', 'COMPLETED', 0, '2026-04-29 11:05:12', '2026-04-29 11:05:12'),
(9075, 'user_chat_07', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 11:05:50', '2026-04-29 11:05:50'),
(9076, 'user_chat_07', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:06:02', '2026-04-29 11:06:02'),
(9077, 'user_chat_08', 2, 'user', '番薯地怎么减少象甲？', 'COMPLETED', 0, '2026-04-29 11:22:00', '2026-04-29 11:22:00'),
(9078, 'user_chat_08', 2, 'assistant', '用无虫种苗，及时培土减少裂缝，收获后清除残薯和藤蔓，必要时配合性诱监测。', 'COMPLETED', 0, '2026-04-29 11:22:12', '2026-04-29 11:22:12'),
(9079, 'user_chat_08', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 11:22:50', '2026-04-29 11:22:50'),
(9080, 'user_chat_08', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:23:02', '2026-04-29 11:23:02'),
(9081, 'user_chat_09', 2, 'user', '番茄晚疫病是不是高温才严重？', 'COMPLETED', 0, '2026-04-29 11:39:00', '2026-04-29 11:39:00'),
(9082, 'user_chat_09', 2, 'assistant', '不是。晚疫病更偏凉湿和长时间结露，低温高湿、通风差更容易流行。', 'COMPLETED', 0, '2026-04-29 11:39:12', '2026-04-29 11:39:12'),
(9083, 'user_chat_09', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 11:39:50', '2026-04-29 11:39:50'),
(9084, 'user_chat_09', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:40:02', '2026-04-29 11:40:02'),
(9085, 'user_chat_10', 2, 'user', 'AI回答能不能引用知识库？', 'COMPLETED', 0, '2026-04-29 11:56:00', '2026-04-29 11:56:00'),
(9086, 'user_chat_10', 2, 'assistant', '可以。系统会根据用户问题检索轻量知识库，再结合当前作物、病虫害或预警上下文生成回答。', 'COMPLETED', 0, '2026-04-29 11:56:12', '2026-04-29 11:56:12'),
(9087, 'user_chat_10', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 11:56:50', '2026-04-29 11:56:50'),
(9088, 'user_chat_10', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 11:57:02', '2026-04-29 11:57:02'),
(9089, 'user_chat_11', 2, 'user', '台风雨后水稻叶缘发白要注意什么？', 'COMPLETED', 0, '2026-04-29 12:13:00', '2026-04-29 12:13:00'),
(9090, 'user_chat_11', 2, 'assistant', '强风雨会造成伤口，利于白叶枯病传播。建议排水、避免串灌，并查看叶缘水渍状黄绿斑是否扩展。', 'COMPLETED', 0, '2026-04-29 12:13:12', '2026-04-29 12:13:12'),
(9091, 'user_chat_11', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 12:13:50', '2026-04-29 12:13:50'),
(9092, 'user_chat_11', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 12:14:02', '2026-04-29 12:14:02'),
(9093, 'user_chat_12', 2, 'user', '芒果花期遇雨，炭疽病怎么预防？', 'COMPLETED', 0, '2026-04-29 12:30:00', '2026-04-29 12:30:00'),
(9094, 'user_chat_12', 2, 'assistant', '重点是修剪通风、花前和谢花后预防，减少花穗长时间湿润。雨后应及时检查花穗褐变和幼果黑斑。', 'COMPLETED', 0, '2026-04-29 12:30:12', '2026-04-29 12:30:12'),
(9095, 'user_chat_12', 2, 'user', '如果要在演示视频里讲，能不能用三句话概括？', 'COMPLETED', 0, '2026-04-29 12:30:50', '2026-04-29 12:30:50'),
(9096, 'user_chat_12', 2, 'assistant', '可以概括为：第一，先说明该问题与天气、作物生育期和田间环境有关；第二，展示系统会结合知识库和当前页面上下文回答；第三，给出可执行的巡查和处置建议，避免空泛回答。', 'COMPLETED', 0, '2026-04-29 12:31:02', '2026-04-29 12:31:02');

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 演示核验 SQL
-- =========================================
SELECT 'users' AS module, COUNT(*) AS total FROM `user`
UNION ALL SELECT 'crops', COUNT(*) FROM `crop` WHERE delete_flag = 0
UNION ALL SELECT 'pests', COUNT(*) FROM `pest` WHERE delete_flag = 0
UNION ALL SELECT 'crop_pest_rel', COUNT(*) FROM `crop_pest_rel` WHERE delete_flag = 0
UNION ALL SELECT 'knowledge_base', COUNT(*) FROM `lightweight_knowledge_base_enhanced_qa` WHERE delete_flag = 0
UNION ALL SELECT 'rules_enabled', COUNT(*) FROM `prewarning_rules` WHERE delete_flag = 0 AND rule_status = 'ENABLED'
UNION ALL SELECT 'warnings_inserted', COUNT(*) FROM `warning` WHERE delete_flag = 0
UNION ALL SELECT 'ai_sessions', COUNT(*) FROM `ai_chat_session` WHERE delete_flag = 0
UNION ALL SELECT 'ai_messages', COUNT(*) FROM `ai_chat_message` WHERE delete_flag = 0;

-- 首页今日预警预览
SELECT w.id, w.title, c.name AS crop_name, p.name AS pest_name, w.risk_level, w.warning_type, w.warning_date, w.gmt_create
FROM warning w
JOIN crop c ON c.id = w.crop_id
JOIN pest p ON p.id = w.pest_id
WHERE w.delete_flag = 0 AND w.warning_type = 'TODAY' AND w.warning_date = '2026-04-29'
ORDER BY FIELD(w.risk_level, '高', '中', '低'), w.gmt_create DESC;

-- AI会话列表预览
SELECT s.user_id, u.username, s.chat_id, s.title, s.context_type, s.context_id, s.gmt_create
FROM ai_chat_session s
JOIN user u ON u.id = s.user_id
WHERE s.delete_flag = 0
ORDER BY s.user_id, s.gmt_create DESC;





























-- =========================================
-- 预警规则表演示数据
-- =========================================

INSERT INTO `prewarning_rules`
(`id`,`rule_name`,`crop_id`,`pest_id`,
 `min_temp`,`max_temp`,
 `min_humidity`,`max_humidity`,
 `min_precipitation`,`max_precipitation`,
 `min_wind_speed`,`max_wind_speed`,
 `risk_level`,`suggestion`,
 `rule_status`,`delete_flag`,
 `gmt_create`,`gmt_modified`)
VALUES

-- 水稻纹枯病
(5001,'水稻纹枯病高湿高风险规则',
1001,2003,
25,31,
82,98,
6,20,
2,9,
'高',
'近期持续高湿并伴随降雨，建议及时排水控湿，减少田间郁闭环境，并加强纹枯病巡查。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 稻瘟病
(5002,'水稻稻瘟病连续阴雨风险规则',
1001,2004,
22,28,
85,100,
8,25,
NULL,15,
'高',
'连续阴雨和高湿环境容易诱发稻瘟病，建议重点关注叶瘟和穗颈瘟发生情况。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 香蕉叶斑病
(5003,'香蕉叶斑病高温高湿规则',
1003,2008,
24,32,
80,96,
5,18,
3,12,
'高',
'蕉园近期湿度较高，建议及时清理病叶，加强通风透光，避免病斑快速扩展。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 荔枝霜疫霉病
(5004,'荔枝霜疫霉病阴雨扩散规则',
1005,2014,
23,30,
84,100,
10,30,
NULL,18,
'高',
'近期阴雨天气较多，荔枝果园湿度偏高，需重点关注霜疫霉病扩散风险。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 花生叶斑病
(5005,'花生叶斑病高湿环境规则',
1007,2017,
24,31,
78,95,
4,16,
2,10,
'中',
'建议加强花生田间巡查，及时清除病叶，连续降雨后注意预防叶斑病扩展。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 黄瓜霜霉病
(5006,'黄瓜霜霉病高湿规则',
1011,2023,
22,29,
85,100,
6,20,
NULL,14,
'高',
'当前空气湿度较高，黄瓜叶面结露风险增加，需重点防范霜霉病。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 辣椒疫病
(5007,'辣椒疫病连续降雨规则',
1009,2019,
24,31,
80,98,
10,28,
2,12,
'高',
'近期降雨偏多，田间湿度较大，建议及时排水并加强辣椒疫病监测。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 芒果炭疽病
(5008,'芒果炭疽病果园高湿规则',
1012,2025,
25,32,
82,97,
5,20,
NULL,16,
'中',
'高湿环境下芒果炭疽病风险增加，建议做好修剪通风与病果清理。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 稻飞虱
(5009,'稻飞虱迁飞高湿风险规则',
1001,2001,
26,32,
75,95,
2,15,
1,8,
'中',
'近期天气条件适宜稻飞虱活动与扩散，建议加强虫口密度监测。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00'),

-- 白粉虱（边缘命中型）
(5010,'白粉虱持续繁殖监测规则',
1010,2022,
25,33,
65,85,
0,10,
NULL,20,
'低',
'当前环境对白粉虱存在一定适宜性，建议持续观察叶背虫口变化。',
'ENABLED',0,
'2026-05-01 09:00:00','2026-05-08 09:00:00');
```



-- =========================================
-- warning 演示数据（新版：包含 risk_score / risk_score_detail / match_detail）
-- 基于 2026-05-08 ~ 2026-05-16 湛江天气生成
-- =========================================

INSERT INTO `warning`
(
 `id`,
 `title`,
 `crop_id`,
 `pest_id`,
 `risk_level`,
 `risk_score`,
 `risk_score_detail`,
 `match_detail`,
 `warning_type`,
 `warning_date`,
 `rule_id`,
 `delete_flag`,
 `gmt_create`,
 `gmt_modified`
)
VALUES

-- =====================================
-- TODAY
-- =====================================

(
7001,
'水稻纹枯病高湿扩散高风险预警',
1001,
2003,
'高',
88,

'[{"factor":"湿度","score":35,"maxScore":35},{"factor":"温度","score":26,"maxScore":30},{"factor":"降水","score":20,"maxScore":25},{"factor":"风速","score":7,"maxScore":10}]',

'[{"metric":"温度","actualValue":24.40,"unit":"℃","minValue":25.00,"maxValue":31.00,"operator":"BETWEEN","matched":false},{"metric":"湿度","actualValue":95.00,"unit":"%","minValue":82.00,"maxValue":98.00,"operator":"BETWEEN","matched":true},{"metric":"降水","actualValue":13.20,"unit":"mm","minValue":6.00,"maxValue":20.00,"operator":"BETWEEN","matched":true},{"metric":"风速","actualValue":5.10,"unit":"m/s","minValue":2.00,"maxValue":9.00,"operator":"BETWEEN","matched":true}]',

'TODAY',
'2026-05-08',
5001,
0,
'2026-05-08 22:05:00',
'2026-05-08 22:05:00'
),

(
7002,
'水稻稻瘟病连续阴雨高风险预警',
1001,
2004,
'高',
93,

'[{"factor":"湿度","score":35,"maxScore":35},{"factor":"温度","score":30,"maxScore":30},{"factor":"降水","score":20,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":24.40,"unit":"℃","minValue":22.00,"maxValue":28.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":95.00,"unit":"%","minValue":85.00,"maxValue":null,"operator":"GTE","matched":true},{"metric":"降水","actualValue":13.20,"unit":"mm","minValue":8.00,"maxValue":null,"operator":"GTE","matched":true},{"metric":"风速","actualValue":5.10,"unit":"m/s","minValue":null,"maxValue":15.00,"operator":"LTE","matched":true}]',

'TODAY',
'2026-05-08',
5002,
0,
'2026-05-08 22:06:00',
'2026-05-08 22:06:00'
),

(
7003,
'香蕉叶斑病高湿环境风险预警',
1003,
2008,
'高',
84,

'[{"factor":"湿度","score":35,"maxScore":35},{"factor":"温度","score":24,"maxScore":30},{"factor":"降水","score":17,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":24.40,"unit":"℃","minValue":24.00,"maxValue":32.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":95.00,"unit":"%","minValue":80.00,"maxValue":96.00,"operator":"BETWEEN","matched":true},{"metric":"降水","actualValue":13.20,"unit":"mm","minValue":5.00,"maxValue":18.00,"operator":"BETWEEN","matched":true},{"metric":"风速","actualValue":5.10,"unit":"m/s","minValue":3.00,"maxValue":12.00,"operator":"BETWEEN","matched":true}]',

'TODAY',
'2026-05-08',
5003,
0,
'2026-05-08 22:07:00',
'2026-05-08 22:07:00'
),

(
7004,
'花生叶斑病中风险监测预警',
1007,
2017,
'中',
74,

'[{"factor":"湿度","score":32,"maxScore":35},{"factor":"温度","score":18,"maxScore":30},{"factor":"降水","score":16,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":24.40,"unit":"℃","minValue":24.00,"maxValue":31.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":95.00,"unit":"%","minValue":78.00,"maxValue":95.00,"operator":"BETWEEN","matched":true},{"metric":"降水","actualValue":13.20,"unit":"mm","minValue":4.00,"maxValue":16.00,"operator":"BETWEEN","matched":true},{"metric":"风速","actualValue":5.10,"unit":"m/s","minValue":2.00,"maxValue":10.00,"operator":"BETWEEN","matched":true}]',

'TODAY',
'2026-05-08',
5005,
0,
'2026-05-08 22:09:00',
'2026-05-08 22:09:00'
),

(
7005,
'稻飞虱活跃风险监测预警',
1001,
2001,
'中',
72,

'[{"factor":"湿度","score":30,"maxScore":35},{"factor":"温度","score":22,"maxScore":30},{"factor":"降水","score":12,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":24.40,"unit":"℃","minValue":26.00,"maxValue":32.00,"operator":"BETWEEN","matched":false},{"metric":"湿度","actualValue":95.00,"unit":"%","minValue":75.00,"maxValue":95.00,"operator":"BETWEEN","matched":true},{"metric":"降水","actualValue":13.20,"unit":"mm","minValue":2.00,"maxValue":15.00,"operator":"BETWEEN","matched":true},{"metric":"风速","actualValue":5.10,"unit":"m/s","minValue":1.00,"maxValue":8.00,"operator":"BETWEEN","matched":true}]',

'TODAY',
'2026-05-08',
5009,
0,
'2026-05-08 22:12:00',
'2026-05-08 22:12:00'
),

-- =====================================
-- FORECAST
-- =====================================

(
7101,
'未来连续降雨导致水稻纹枯病风险升高预警',
1001,
2003,
'高',
92,

'[{"factor":"湿度","score":35,"maxScore":35},{"factor":"温度","score":28,"maxScore":30},{"factor":"降水","score":21,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":28.40,"unit":"℃","minValue":25.00,"maxValue":31.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":85.13,"unit":"%","minValue":82.00,"maxValue":98.00,"operator":"BETWEEN","matched":true},{"metric":"降水","actualValue":2.00,"unit":"mm","minValue":6.00,"maxValue":20.00,"operator":"BETWEEN","matched":false},{"metric":"风速","actualValue":15.50,"unit":"m/s","minValue":null,"maxValue":18.00,"operator":"LTE","matched":true}]',

'FORECAST',
'2026-05-10',
5001,
0,
'2026-05-08 22:20:00',
'2026-05-08 22:20:00'
),

(
7102,
'未来高湿环境下荔枝霜疫霉病风险预警',
1005,
2014,
'高',
87,

'[{"factor":"湿度","score":35,"maxScore":35},{"factor":"温度","score":25,"maxScore":30},{"factor":"降水","score":18,"maxScore":25},{"factor":"风速","score":9,"maxScore":10}]',

'[{"metric":"温度","actualValue":29.60,"unit":"℃","minValue":23.00,"maxValue":30.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":81.63,"unit":"%","minValue":84.00,"maxValue":null,"operator":"GTE","matched":false},{"metric":"降水","actualValue":0.90,"unit":"mm","minValue":10.00,"maxValue":30.00,"operator":"BETWEEN","matched":false},{"metric":"风速","actualValue":14.70,"unit":"m/s","minValue":null,"maxValue":18.00,"operator":"LTE","matched":true}]',

'FORECAST',
'2026-05-11',
5004,
0,
'2026-05-08 22:21:00',
'2026-05-08 22:21:00'
),

(
7103,
'芒果炭疽病未来高湿风险预警',
1012,
2025,
'中',
76,

'[{"factor":"湿度","score":31,"maxScore":35},{"factor":"温度","score":22,"maxScore":30},{"factor":"降水","score":14,"maxScore":25},{"factor":"风速","score":9,"maxScore":10}]',

'[{"metric":"温度","actualValue":31.00,"unit":"℃","minValue":25.00,"maxValue":32.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":83.71,"unit":"%","minValue":82.00,"maxValue":97.00,"operator":"BETWEEN","matched":true},{"metric":"降水","actualValue":2.00,"unit":"mm","minValue":5.00,"maxValue":20.00,"operator":"BETWEEN","matched":false},{"metric":"风速","actualValue":15.20,"unit":"m/s","minValue":null,"maxValue":16.00,"operator":"LTE","matched":true}]',

'FORECAST',
'2026-05-12',
5008,
0,
'2026-05-08 22:24:00',
'2026-05-08 22:24:00'
),

(
7104,
'香蕉叶斑病雷暴天气风险预警',
1003,
2008,
'高',
81,

'[{"factor":"湿度","score":34,"maxScore":35},{"factor":"温度","score":25,"maxScore":30},{"factor":"降水","score":14,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":31.60,"unit":"℃","minValue":24.00,"maxValue":32.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":84.71,"unit":"%","minValue":80.00,"maxValue":96.00,"operator":"BETWEEN","matched":true},{"metric":"降水","actualValue":15.00,"unit":"mm","minValue":5.00,"maxValue":18.00,"operator":"BETWEEN","matched":true},{"metric":"风速","actualValue":17.80,"unit":"m/s","minValue":null,"maxValue":20.00,"operator":"LTE","matched":true}]',

'FORECAST',
'2026-05-14',
5003,
0,
'2026-05-08 22:25:00',
'2026-05-08 22:25:00'
),

(
7105,
'未来高湿天气导致稻瘟病风险预警',
1001,
2004,
'高',
83,

'[{"factor":"湿度","score":35,"maxScore":35},{"factor":"温度","score":24,"maxScore":30},{"factor":"降水","score":16,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":29.60,"unit":"℃","minValue":22.00,"maxValue":28.00,"operator":"BETWEEN","matched":false},{"metric":"湿度","actualValue":87.21,"unit":"%","minValue":85.00,"maxValue":null,"operator":"GTE","matched":true},{"metric":"降水","actualValue":11.40,"unit":"mm","minValue":8.00,"maxValue":null,"operator":"GTE","matched":true},{"metric":"风速","actualValue":13.20,"unit":"m/s","minValue":null,"maxValue":15.00,"operator":"LTE","matched":true}]',

'FORECAST',
'2026-05-15',
5002,
0,
'2026-05-08 22:27:00',
'2026-05-08 22:27:00'
),

(
7106,
'荔枝霜疫霉病未来持续风险预警',
1005,
2014,
'高',
79,

'[{"factor":"湿度","score":34,"maxScore":35},{"factor":"温度","score":21,"maxScore":30},{"factor":"降水","score":16,"maxScore":25},{"factor":"风速","score":8,"maxScore":10}]',

'[{"metric":"温度","actualValue":29.40,"unit":"℃","minValue":23.00,"maxValue":30.00,"operator":"BETWEEN","matched":true},{"metric":"湿度","actualValue":86.42,"unit":"%","minValue":84.00,"maxValue":null,"operator":"GTE","matched":true},{"metric":"降水","actualValue":10.80,"unit":"mm","minValue":10.00,"maxValue":30.00,"operator":"BETWEEN","matched":true},{"metric":"风速","actualValue":18.30,"unit":"m/s","minValue":null,"maxValue":18.00,"operator":"LTE","matched":false}]',

'FORECAST',
'2026-05-16',
5004,
0,
'2026-05-08 22:28:00',
'2026-05-08 22:28:00'
);
```





-- =========================================
-- feedback 演示数据
-- =========================================

INSERT INTO `feedback`
(`id`,`user_id`,
 `target_type`,`target_id`,
 `feedback_result`,
 `content`,
 `delete_flag`,
 `gmt_create`,`gmt_modified`)
VALUES

-- WARNING

(9001,2,
'WARNING','7001',
'YES',
'这两天田里湿度明显比较大，部分稻株底部已经开始出现病斑，和系统预警情况比较接近。',
0,
'2026-05-08 22:40:00','2026-05-08 22:40:00'),

(9002,3,
'WARNING','7002',
'UNCERTAIN',
'目前还没有发现明显穗颈瘟，但连续阴雨天气确实持续了几天，准备继续观察。',
0,
'2026-05-08 22:42:00','2026-05-08 22:42:00'),

(9003,4,
'WARNING','7003',
'YES',
'香蕉园最近叶片黑斑明显增多，尤其雨后扩展比较快，系统提醒比较及时。',
0,
'2026-05-08 22:44:00','2026-05-08 22:44:00'),

(9004,5,
'WARNING','7005',
'NO',
'目前花生田通风情况还可以，暂时没有发现明显叶斑扩散。',
0,
'2026-05-08 22:45:00','2026-05-08 22:45:00'),

(9005,6,
'WARNING','7006',
'YES',
'最近棚内湿气很重，部分黄瓜叶背已经开始出现霉层，和预警描述比较一致。',
0,
'2026-05-08 22:47:00','2026-05-08 22:47:00'),

(9006,7,
'WARNING','7101',
'YES',
'连续降雨后田里一直比较潮湿，担心纹枯病进一步扩散，准备提前处理。',
0,
'2026-05-08 22:48:00','2026-05-08 22:48:00'),

(9007,8,
'WARNING','7105',
'UNCERTAIN',
'芒果园目前只有少量病果，后面还要再观察几天。',
0,
'2026-05-08 22:50:00','2026-05-08 22:50:00'),

-- AI_CHAT

(9010,2,
'AI_CHAT','chat_001',
'YES',
'AI回答比较详细，把高湿环境和病害风险关系解释得很清楚。',
0,
'2026-05-08 22:55:00','2026-05-08 22:55:00'),

(9011,3,
'AI_CHAT','chat_002',
'UNCERTAIN',
'回答内容比较多，但有些地方还是不太容易理解。',
0,
'2026-05-08 22:56:00','2026-05-08 22:56:00'),

(9012,5,
'AI_CHAT','chat_003',
'YES',
'上传图片后识别结果基本符合实际情况，对田间巡查有帮助。',
0,
'2026-05-08 22:57:00','2026-05-08 22:57:00'),

(9013,6,
'AI_CHAT','chat_004',
'NO',
'这次图片识别速度有点慢，而且识别结果和实际情况不太一致。',
0,
'2026-05-08 22:58:00','2026-05-08 22:58:00'),

-- AI_IMAGE

(9020,7,
'AI_IMAGE','image_001',
'YES',
'图片识别出的香蕉叶斑病和现场情况基本一致。',
0,
'2026-05-08 23:00:00','2026-05-08 23:00:00'),

(9021,8,
'AI_IMAGE','image_002',
'UNCERTAIN',
'识别结果给了几个可能病害，目前还不能完全确认。',
0,
'2026-05-08 23:01:00','2026-05-08 23:01:00'),

(9022,9,
'AI_IMAGE','image_003',
'YES',
'系统对黄瓜霜霉病识别比较准确，还给出了对应处理建议。',
0,
'2026-05-08 23:02:00','2026-05-08 23:02:00');


-- =========================================
-- feedback 演示数据（扩展版）
-- 包含 WARNING / AI_CHAT / AI_IMAGE
-- =========================================

```sql
INSERT INTO `feedback`
(
 `id`,
 `user_id`,
 `target_type`,
 `target_id`,
 `feedback_result`,
 `content`,
 `delete_flag`,
 `gmt_create`,
 `gmt_modified`
)
VALUES

-- =====================================
-- WARNING
-- =====================================

(9101,2,'WARNING',7001,'YES',
'最近连续下雨之后，田里底部稻叶开始出现褐色病斑，和系统预警情况比较接近，已经安排排水处理。',
0,'2026-05-08 22:40:00','2026-05-08 22:40:00'),

(9102,3,'WARNING',7002,'UNCERTAIN',
'目前暂时还没有发现明显穗颈瘟，但最近湿度确实很高，后面准备再观察几天。',
0,'2026-05-08 22:42:00','2026-05-08 22:42:00'),

(9103,4,'WARNING',7003,'YES',
'香蕉园最近叶片黑斑明显增多，尤其是雨后扩散速度比较快，系统提醒比较及时。',
0,'2026-05-08 22:44:00','2026-05-08 22:44:00'),

(9104,5,'WARNING',7004,'YES',
'荔枝果园最近连续阴雨，部分果实已经开始出现霉斑，和预警描述比较一致。',
0,'2026-05-08 22:45:00','2026-05-08 22:45:00'),

(9105,6,'WARNING',7005,'NO',
'目前花生田通风情况还可以，暂时没有发现明显叶斑扩散，预警可能偏高。',
0,'2026-05-08 22:47:00','2026-05-08 22:47:00'),

(9106,7,'WARNING',7006,'YES',
'棚内最近湿气很重，黄瓜叶背已经开始出现霉层，系统提醒比较符合实际情况。',
0,'2026-05-08 22:49:00','2026-05-08 22:49:00'),

(9107,8,'WARNING',7007,'YES',
'最近连续降雨后辣椒田积水比较严重，部分植株已经开始出现萎蔫。',
0,'2026-05-08 22:50:00','2026-05-08 22:50:00'),

(9108,9,'WARNING',7008,'UNCERTAIN',
'目前虫口数量还不算特别高，不过天气条件确实比较适合稻飞虱活动。',
0,'2026-05-08 22:52:00','2026-05-08 22:52:00'),

(9109,2,'WARNING',7101,'YES',
'连续降雨之后田间湿度一直降不下来，已经提前准备药剂防治纹枯病。',
0,'2026-05-08 22:53:00','2026-05-08 22:53:00'),

(9110,3,'WARNING',7102,'YES',
'果园最近早晚湿气很重，担心霜疫霉病继续扩散，准备加强巡查。',
0,'2026-05-08 22:55:00','2026-05-08 22:55:00'),

(9111,4,'WARNING',7103,'UNCERTAIN',
'目前芒果园只有少量病果，风险可能没有系统提示那么高。',
0,'2026-05-08 22:57:00','2026-05-08 22:57:00'),

(9112,5,'WARNING',7104,'YES',
'雷暴天气之后蕉园湿度明显增加，部分老叶病斑扩散速度加快。',
0,'2026-05-08 22:58:00','2026-05-08 22:58:00'),

(9113,6,'WARNING',7105,'YES',
'最近稻田一直比较潮湿，而且温度偏高，担心后面会出现稻瘟病。',
0,'2026-05-08 23:00:00','2026-05-08 23:00:00'),

(9114,7,'WARNING',7106,'NO',
'目前果园排水做得比较及时，还没有发现明显病害扩散。',
0,'2026-05-08 23:02:00','2026-05-08 23:02:00'),

(9115,8,'WARNING',7001,'YES',
'系统这次预警比较准确，田里已经开始有纹枯病扩散迹象。',
0,'2026-05-08 23:04:00','2026-05-08 23:04:00'),

(9116,9,'WARNING',7003,'YES',
'香蕉叶片最近病斑明显增多，尤其高湿天气之后扩散比较明显。',
0,'2026-05-08 23:05:00','2026-05-08 23:05:00'),

(9117,10,'WARNING',7006,'YES',
'最近大棚通风不太好，黄瓜霜霉病确实有扩散趋势。',
0,'2026-05-08 23:07:00','2026-05-08 23:07:00'),

(9118,11,'WARNING',7007,'UNCERTAIN',
'辣椒田部分区域有轻微积水，不过目前还没有出现明显疫病症状。',
0,'2026-05-08 23:08:00','2026-05-08 23:08:00'),

-- =====================================
-- AI_CHAT
-- =====================================

(9201,2,'AI_CHAT',8001,'YES',
'AI回答比较详细，把高湿环境和病害风险之间的关系解释得很清楚。',
0,'2026-05-08 23:10:00','2026-05-08 23:10:00'),

(9202,3,'AI_CHAT',8002,'UNCERTAIN',
'回答内容比较多，不过有些专业术语还是不太容易理解。',
0,'2026-05-08 23:11:00','2026-05-08 23:11:00'),

(9203,4,'AI_CHAT',8003,'YES',
'系统给出的防治建议比较实用，已经按照建议开始加强排水。',
0,'2026-05-08 23:12:00','2026-05-08 23:12:00'),

(9204,5,'AI_CHAT',8004,'NO',
'这次AI回答和现场情况有一点偏差，病害程度没有那么严重。',
0,'2026-05-08 23:13:00','2026-05-08 23:13:00'),

(9205,6,'AI_CHAT',8005,'YES',
'AI不仅解释了风险原因，还给出了后续巡查建议，整体体验不错。',
0,'2026-05-08 23:15:00','2026-05-08 23:15:00'),

(9206,7,'AI_CHAT',8006,'YES',
'问题回答速度还可以，而且结合了当前天气情况，感觉比较智能。',
0,'2026-05-08 23:16:00','2026-05-08 23:16:00'),

(9207,8,'AI_CHAT',8007,'UNCERTAIN',
'有些回答内容比较偏理论，希望后面能再贴近田间实际一点。',
0,'2026-05-08 23:18:00','2026-05-08 23:18:00'),

(9208,9,'AI_CHAT',8008,'YES',
'AI解释连续降雨为什么容易诱发病害这一点讲得比较清楚。',
0,'2026-05-08 23:20:00','2026-05-08 23:20:00'),

(9209,10,'AI_CHAT',8009,'NO',
'这次图片分析等待时间有点长，中间差点以为卡住了。',
0,'2026-05-08 23:21:00','2026-05-08 23:21:00'),

-- =====================================
-- AI_IMAGE
-- =====================================

(9301,2,'AI_IMAGE',8101,'YES',
'系统识别出的香蕉叶斑病和现场情况基本一致，还给出了防治建议。',
0,'2026-05-08 23:25:00','2026-05-08 23:25:00'),

(9302,3,'AI_IMAGE',8102,'UNCERTAIN',
'识别结果给出了几个可能病害，目前还不能完全确认。',
0,'2026-05-08 23:26:00','2026-05-08 23:26:00'),

(9303,4,'AI_IMAGE',8103,'YES',
'黄瓜霜霉病识别准确率还不错，和实际情况比较接近。',
0,'2026-05-08 23:28:00','2026-05-08 23:28:00'),

(9304,5,'AI_IMAGE',8104,'YES',
'系统对病斑区域标识比较明显，方便后面进行田间巡查。',
0,'2026-05-08 23:29:00','2026-05-08 23:29:00'),

(9305,6,'AI_IMAGE',8105,'NO',
'这次识别结果偏差有点大，实际情况更像缺素症状。',
0,'2026-05-08 23:30:00','2026-05-08 23:30:00'),

(9306,7,'AI_IMAGE',8106,'YES',
'上传图片后识别速度还可以，而且给出的处理建议比较详细。',
0,'2026-05-08 23:31:00','2026-05-08 23:31:00'),

(9307,8,'AI_IMAGE',8107,'UNCERTAIN',
'目前田间症状还比较轻，AI给出的结果需要后续继续观察确认。',
0,'2026-05-08 23:33:00','2026-05-08 23:33:00'),

(9308,9,'AI_IMAGE',8108,'YES',
'系统对叶片病斑识别比较准确，尤其是潮湿环境下的病害分析。',
0,'2026-05-08 23:34:00','2026-05-08 23:34:00');
