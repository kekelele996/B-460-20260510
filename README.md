# 音乐播放器

一个基于 SpringBoot + Vue + MySQL 的现代化音乐播放器应用。

## 🛠 技术栈

- **前端**: Vue 3 + Vite + Element Plus
- **后端**: Spring Boot 3.2 + JPA + MySQL
- **数据库**: MySQL 8.0
- **容器化**: Docker + Docker Compose

## 🚀 启动指南

### 前置要求

- Docker Desktop 已安装并运行
- Docker Compose 已安装

### 启动步骤

1. 克隆项目到本地
2. 在项目根目录执行以下命令：

```bash
docker compose up --build
```

3. 等待所有容器启动完成（首次启动需要下载依赖和构建镜像，可能需要几分钟）

## 🔗 服务地址

- **前端应用**: http://localhost:3000
- **后端 API**: http://localhost:8000
- **数据库**: localhost:3307 (用户名: root / 密码: root)

## 📱 功能特性

### 歌曲管理
- 查看所有歌曲
- 搜索歌曲（按歌名、歌手、专辑）
- 播放歌曲
- 喜欢/取消喜欢歌曲
- 查看播放次数和喜欢数

### 播放列表
- 查看所有播放列表
- 创建播放列表
- 编辑播放列表
- 删除播放列表
- 向播放列表添加/移除歌曲

### 播放器
- 播放/暂停控制
- 上一首/下一首切换
- 进度条拖动
- 音量调节
- 自动播放下一首

### 预设数据

系统已预置以下数据供测试：

- **歌曲**: 10 首示例歌曲（包含周杰伦的经典歌曲）
- **播放列表**: 3 个预设播放列表
  - 我喜欢的音乐
  - 流行热歌
  - 周杰伦精选

## 📂 项目结构

```
music-player/
├── frontend/              # 前端 Vue 项目
│   ├── src/
│   │   ├── api/          # API 调用
│   │   ├── components/   # Vue 组件
│   │   ├── App.vue       # 主应用组件
│   │   └── main.js       # 入口文件
│   ├── Dockerfile        # 前端 Docker 镜像
│   ├── nginx.conf        # Nginx 配置
│   └── package.json
├── backend/              # 后端 SpringBoot 项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/musicplayer/
│   │       │   ├── controller/   # 控制器
│   │       │   ├── service/      # 服务层
│   │       │   ├── repository/   # 数据访问层
│   │       │   ├── entity/       # 实体类
│   │       │   ├── dto/          # 数据传输对象
│   │       │   └── MusicPlayerApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── init.sql      # 数据库初始化脚本
│   ├── Dockerfile        # 后端 Docker 镜像
│   ├── settings.xml     # Maven 配置
│   └── pom.xml
├── docker-compose.yml   # Docker Compose 配置
└── README.md
```

## 🔧 开发说明

### 前端开发

前端使用 Vue 3 + Vite + Element Plus 构建，主要技术特点：

- **组件化开发**: 使用 Vue 3 Composition API
- **状态管理**: 使用 Vue 3 响应式系统
- **UI 组件库**: Element Plus 提供现代化 UI
- **HTTP 客户端**: Axios 进行 API 调用

### 后端开发

后端使用 Spring Boot 3.2 构建，主要技术特点：

- **RESTful API**: 标准的 REST 接口设计
- **数据持久化**: Spring Data JPA + MySQL
- **跨域支持**: CORS 配置支持前端调用
- **日志系统**: SLF4J + Logback

### 数据库设计

数据库包含以下主要表：

- **songs**: 歌曲信息表
- **playlists**: 播放列表表
- **playlist_songs**: 播放列表歌曲关联表

## 🐳 Docker 配置说明

### 镜像加速

项目已配置国内镜像源以加速构建：

- **npm**: 使用淘宝镜像源
- **Maven**: 使用阿里云镜像源

### 资源限制

为防止系统资源占用过高，已配置容器资源限制：

- **MySQL**: 最大内存 1GB
- **Backend**: 最大内存 1.5GB
- **Frontend**: 最大内存 2GB

### 数据持久化

MySQL 数据通过 Docker Volume 持久化存储，容器重启后数据不会丢失。

## 📝 注意事项

1. 首次启动需要等待数据库初始化完成
2. 歌曲音频使用在线示例资源，需要网络连接
3. 如需修改数据库配置，请编辑 `docker-compose.yml` 中的环境变量
4. 前端开发时，API 地址配置为 `http://backend:8000`（Docker 内部网络）

## 📄 许可证

MIT License
