-- =========================================
-- 农业病虫害综合平台数据库初始化脚本
-- 适用数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_general_ci
-- =========================================
drop DATABASE agri_pest_platform;
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
-- 4.5 ai_qa_record（AI问答记录表）
-- =========================================
DROP TABLE IF EXISTS `ai_qa_record`;
CREATE TABLE `ai_qa_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问答记录唯一标识',
    `user_id` BIGINT DEFAULT NULL COMMENT '提问用户ID，关联用户表',
    `user_question` TEXT NOT NULL COMMENT '用户提问内容',
    `ai_answer` LONGTEXT DEFAULT NULL COMMENT 'AI 回答内容',
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_ai_qa_record_user_id` (`user_id`),
    KEY `idx_ai_qa_record_timestamp` (`timestamp`),
    CONSTRAINT `fk_ai_qa_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_ai_qa_record_delete_flag` CHECK (`delete_flag` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI问答记录表';

-- =========================================
-- 4.6 pest_environment_conditions（适宜病虫害发生的环境条件表）
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
-- 4.7 prewarning_rules（预警规则表）
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
-- 4.8 warning（预警表）
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
-- 4.9 lightweight_knowledge_base_enhanced_qa（轻量版知识库增强问答表）
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

