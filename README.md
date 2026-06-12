# 农业病虫害智能预警辅助决策系统

## 项目简介

农业病虫害智能预警辅助决策系统是一个面向智慧农业场景的全栈项目。

系统基于 Open-Meteo 气象数据、农业专家规则模型与 AI 大模型能力，实现病虫害风险分析、智能预警、农业知识问答、AI 图文分析及辅助决策等功能。

项目采用 Spring Boot + MySQL + Redis + Spring AI 技术栈，结合 RAG（Retrieval-Augmented Generation）知识增强生成与 SSE（Server-Sent Events）流式响应技术，提高农业场景下 AI 分析的专业性与可解释性。

---

## 项目架构

```text
前端

Vue3 + Element Plus + ECharts

        ↓

后端

Spring Boot + MyBatis-Plus

        ↓

MySQL + Redis

        ↓

Spring AI + DeepSeek + RAG

        ↓

Open-Meteo 天气数据接口
```

---

## 技术栈

### 后端

* Java 17
* Spring Boot 3.x
* MyBatis-Plus
* MySQL 8
* Redis
* Maven

### AI能力

* Spring AI
* DeepSeek
* RAG知识增强生成
* 向量检索
* 多轮上下文对话
* SSE流式输出

### 前端

* Vue3
* Element Plus
* ECharts

---

## 核心功能

### 病虫害智能预警

* TODAY 实时预警
* FORECAST 未来预警
* 自动规则匹配
* 风险评分分析
* 命中依据展示

### AI农业助手

* 农业知识问答
* 悬浮AI助手
* 独立AI会话
* 多轮上下文对话
* SSE流式输出

### AI图文分析

* 病虫害图像识别
* 环境风险分析
* AI诊断建议生成
* 图像与环境协同分析

### 数据分析

* 风险等级统计
* 作物预警统计
* 季节趋势分析
* 高风险病虫害排行

### 用户反馈

* AI分析结果反馈
* 预警结果反馈
* 反馈统计分析

---

## 项目亮点

### 风险评分机制

基于温度、湿度、降水量、风速等环境因素构建风险评分模型，实现病虫害风险量化评估。

### 可解释型预警

支持预警命中依据展示，记录各环境指标与规则条件的匹配情况，提高预警结果可信度。

### RAG农业知识库

基于农业知识库与向量检索技术，实现农业知识增强问答，降低大模型幻觉问题。

### AI图文协同分析

结合病虫害图片、实时天气、未来天气、风险预警及农业知识库内容进行综合分析。

### SSE流式响应

采用 Server-Sent Events 实现 AI 分析结果流式返回，提升用户交互体验。

---

## 数据库设计

<img src="images/agri_platform_database_er.svg" width="1000">

---

## 项目截图

### 系统首页

![首页](images/首页.png)

---

### AI图文分析

![AI图文分析](images/AI图文分析页面.png)

---

### 风险评分分析

![风险评分](images/风险评分页面.png)

---

### 命中依据展示

![命中依据](images/命中依据.png)

---

### 数据统计看板

![统计面板](images/统计面板.png)

---

## 项目结构

```text
agriwarningplatform
├── common
│   ├── config
│   ├── constant
│   ├── enums
│   ├── exception
│   ├── handler
│   ├── interceptor
│   └── util
│
├── module
│   ├── ai
│   ├── auth
│   ├── crop
│   ├── feedback
│   ├── knowledgeqa
│   ├── pest
│   ├── pestenvironment
│   ├── prewarningrule
│   ├── stats
│   ├── warning
│   └── weather
│
└── AgriWarningPlatformApplication
```

---

## 快速启动

### 环境要求

* JDK 17+
* MySQL 8+
* Redis 7+
* Maven 3.9+

### 启动步骤

1. 创建数据库

```sql
CREATE DATABASE agri_pest_platform;
```

2. 执行数据库初始化脚本

3. 修改 application.yml 配置

4. 启动 Redis

5. 启动 Spring Boot 项目

```bash
mvn spring-boot:run
```

---

## 项目特色

* 国家级大学生创新创业训练计划项目
* 面向智慧农业场景的病虫害智能预警平台
* 融合气象分析、规则引擎、RAG知识库与AI能力
* 支持风险评分、可解释预警与AI辅助决策

---

## License

MIT License

```
```
