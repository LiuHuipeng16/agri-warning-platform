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


INSERT INTO crop (id,name,category,intro,description,image_url,delete_flag)
VALUES
(1001,'番茄','蔬菜','常见经济作物','番茄是一种常见蔬菜','',0),
(1002,'水稻','粮食','重要粮食作物','水稻是我国主要粮食作物','',0),
(1003,'黄瓜','蔬菜','温室作物','黄瓜常见温室种植','',0),
(1004,'测试删除作物','测试','用于删除测试','测试用','',0);

INSERT INTO pest
(id,name,type,description,symptoms,cause,prevention,risk_level,season,delete_flag)
VALUES
(2001,'番茄灰霉病','病害','常见真菌病害','果实腐烂','湿度高','加强通风','高','春',0),

(2002,'水稻纹枯病','病害','水稻常见病害','叶片枯萎','高温高湿','合理密植','中','夏',0),

(2003,'黄瓜蚜虫','虫害','蚜虫危害','叶片卷曲','虫害侵染','及时防治','低','夏',0),

(2004,'测试删除病虫害','病害','删除测试','测试','测试','测试','低','春',0);


INSERT INTO crop_pest_rel
(crop_id,pest_id,delete_flag)
VALUES
(1001,2001,0),
(1002,2002,0),
(1003,2003,0);

INSERT INTO lightweight_knowledge_base_enhanced_qa
(id,question,answer,crop_id,pest_id,delete_flag)
VALUES
(7001,'番茄灰霉病如何防治','保持通风降低湿度',1001,2001,0),

(7002,'水稻纹枯病怎么办','合理密植并喷药',1002,2002,0);

INSERT INTO prewarning_rules
(id,rule_name,crop_id,pest_id,
min_temp,max_temp,
min_humidity,max_humidity,
min_precipitation,max_precipitation,
min_wind_speed,max_wind_speed,
risk_level,suggestion,rule_status,delete_flag)
VALUES
(5001,
'番茄灰霉病今日预警',
1001,
2001,
22,31,
80,90,
0,5,
5,15,
'高',
'湿度偏高且有降雨，建议注意控湿通风',
'ENABLED',
0);


INSERT INTO prewarning_rules
(id,rule_name,crop_id,pest_id,
min_temp,max_temp,
min_humidity,max_humidity,
min_precipitation,max_precipitation,
min_wind_speed,max_wind_speed,
risk_level,suggestion,rule_status,delete_flag)
VALUES
(5002,
'番茄灰霉病强降雨预警',
1001,
2001,
20,27,
85,95,
5,15,
10,20,
'高',
'持续降雨环境适合灰霉病发生',
'ENABLED',
0);


INSERT INTO prewarning_rules
(id,rule_name,crop_id,pest_id,
min_temp,max_temp,
min_humidity,max_humidity,
min_precipitation,max_precipitation,
min_wind_speed,max_wind_speed,
risk_level,suggestion,rule_status,delete_flag)
VALUES
(5003,
'番茄轻度湿度预警',
1001,
2001,
24,31,
78,85,
0,2,
10,25,
'中',
'湿度较高注意观察病害',
'ENABLED',
0);


INSERT INTO warning
(id,title,crop_id,pest_id,rule_id,risk_level,warning_type,warning_date,delete_flag)
VALUES
(9001,'番茄灰霉病今日预警',1001,2001,5001,'高','TODAY','2026-04-20',0),

(9002,'番茄灰霉病降雨预警',1001,2001,5002,'高','FORECAST','2026-04-23',0);


