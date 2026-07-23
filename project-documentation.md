# 音乐系统项目详细文档

> 文档版本：v1.0
> 适用项目：music-new（音乐分享与互动平台）
> 文档说明：本文档详细描述项目整体架构、技术栈、模块划分、数据库设计、API 接口及部署运维

---

## 目录

- [第一章 项目概述](#第一章-项目概述)
- [第二章 技术栈说明](#第二章-技术栈说明)
- [第三章 系统架构设计](#第三章-系统架构设计)
- [第四章 项目目录结构](#第四章-项目目录结构)
- [第五章 数据库设计](#第五章-数据库设计)
- [第六章 核心模块详细设计](#第六章-核心模块详细设计)
- [第七章 API 接口文档](#第七章-api-接口文档)
- [第八章 部署与运维](#第八章-部署与运维)
- [第九章 测试说明](#第九章-测试说明)
- [第十章 常见问题 FAQ](#第十章-常见问题-faq)

---

## 第一章 项目概述

### 1.1 项目简介

本项目是一个**音乐分享与互动平台**，用户可以在平台上：
- 注册账号、登录系统
- 上传自己的原创歌曲或翻唱作品
- 发布音乐相关的帖子（乐评、推荐、心情等）
- 收藏喜欢的歌曲
- 给歌曲、帖子点赞
- 评论与回复互动
- 管理员对歌曲和帖子进行审核

### 1.2 项目背景

随着音乐消费场景的多样化，用户不仅听歌，也愿意分享和交流。本项目旨在为音乐爱好者提供一个**UGC（用户生产内容）** 平台。

### 1.3 核心功能

| 功能模块 | 描述 |
| :--- | :--- |
| 用户管理 | 注册、登录、个人信息维护、密码修改、头像上传 |
| 歌曲管理 | 歌曲上传、编辑、删除、查询、热门推荐 |
| 帖子管理 | 帖子发布、编辑、删除、点赞、评论、回复 |
| 评论管理 | 歌曲评论、帖子评论、评论点赞 |
| 收藏管理 | 歌曲收藏/取消收藏、收藏列表 |
| 管理员后台 | 内容审核、用户管理、数据统计 |
| 文件上传 | 头像、歌曲文件、封面图片等的上传与管理 |

### 1.4 项目亮点

- **前后端分离**：后端提供 RESTful API，前端独立开发
- **完整审核流程**：所有用户发布的内容需经管理员审核
- **角色权限控制**：普通用户与管理员有不同操作权限
- **数据安全**：密码 BCrypt 加密，操作带归属校验
- **统一规范**：接口统一返回格式、错误码规范统一

---

## 第二章 技术栈说明

### 2.1 后端技术栈

| 技术 | 版本 | 作用 |
| :--- | :--- | :--- |
| Spring Boot | 2.6.2 | 主框架，快速构建企业级应用 |
| MyBatis-Plus | 3.5.1 | ORM 框架，简化数据库操作 |
| MySQL | 8.0+ | 关系型数据库 |
| Spring Security Crypto | - | 密码加密（BCrypt） |
| Lombok | - | 简化 Java 代码（自动生成 getter/setter） |
| Maven | 3.6+ | 项目构建与依赖管理 |

### 2.2 后端核心依赖

[pom.xml](file:///c:/Users/niu%20xiaohan/music-new/pom.xml) 中声明的关键依赖：

```xml
<!-- Spring Web：提供 RESTful 接口能力 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- MyBatis-Plus：ORM 框架 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.1</version>
</dependency>

<!-- MySQL 驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>

<!-- 密码加密 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>

<!-- 测试 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 2.3 前端技术栈（参考）

| 技术 | 作用 |
| :--- | :--- |
| Vue 3 | 前端框架 |
| TypeScript | 类型系统 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router | 路由管理 |
| Axios | HTTP 请求 |
| ECharts | 数据可视化（管理员后台） |

---

## 第三章 系统架构设计

### 3.1 整体架构

项目采用经典的**前后端分离 + 三层架构**：

```
┌─────────────────────────────────────────────┐
│              前端（Vue 3 + Element Plus）        │
│  - 用户端：注册登录、歌曲浏览、帖子发布、互动     │
│  - 管理端：内容审核、用户管理、数据统计            │
└──────────────────┬──────────────────────────┘
                   │ HTTP/HTTPS (RESTful API)
                   │ JSON 数据格式
┌──────────────────▼──────────────────────────┐
│           后端（Spring Boot）                  │
│  ┌─────────────────────────────────────┐     │
│  │  Controller 层（接收请求、返回数据）     │     │
│  └──────────────┬──────────────────────┘     │
│  ┌──────────────▼──────────────────────┐     │
│  │  Service 层（业务逻辑处理）              │     │
│  └──────────────┬──────────────────────┘     │
│  ┌──────────────▼──────────────────────┐     │
│  │  Mapper 层（数据库访问）                │     │
│  └──────────────┬──────────────────────┘     │
└──────────────────┼──────────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────────┐
│            MySQL 数据库                       │
│  存储用户、歌曲、帖子、评论、收藏等数据             │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│        本地文件存储                              │
│  存储上传的歌曲文件、图片文件                       │
└─────────────────────────────────────────────┘
```

### 3.2 三层架构说明

#### Controller 层（控制层）
- 职责：接收 HTTP 请求、参数校验、调用 Service、返回数据
- 包路径：`com.music.controller`
- 示例文件：[UserController.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/UserController.java)

#### Service 层（业务层）
- 职责：处理业务逻辑、事务控制、组合多个 Mapper
- 包路径：`com.music.service.impl`
- 示例文件：[UserServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/UserServiceImpl.java)

#### Mapper 层（数据访问层）
- 职责：与数据库交互，执行 SQL
- 包路径：`com.music.mapper`
- 示例文件：[UserMapper.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/mapper/UserMapper.java)

### 3.3 请求处理流程

以"用户登录"为例：

```
前端发起请求
    ↓
POST /user/login
    ↓
Tomcat 接收请求
    ↓
UserController.login() 接收参数
    ↓
UserServiceImpl.loginStatus() 处理业务
    ↓
UserMapper 查数据库（SELECT * FROM user WHERE username=?）
    ↓
BCrypt 校验密码
    ↓
登录成功 → 存 Session → 返回用户信息
    ↓
R 统一封装 → JSON 返回前端
    ↓
前端拿到数据 → 跳转页面
```

---

## 第四章 项目目录结构

```
music-new/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/music/
│   │   │       ├── MusicApplication.java          # 启动类
│   │   │       ├── common/                          # 公共类
│   │   │       │   ├── R.java                       # 统一返回格式
│   │   │       │   └── Constants.java               # 常量定义
│   │   │       ├── config/                          # 配置类
│   │   │       │   ├── CorsConfig.java              # 跨域配置
│   │   │       │   ├── SecurityConfig.java          # 密码加密配置
│   │   │       │   ├── FileUploadConfig.java        # 文件上传工具
│   │   │       │   ├── FileConfig.java              # 文件路径配置
│   │   │       │   ├── MybatisPlusConfig.java       # MyBatis-Plus 配置
│   │   │       │   └── WebMvcConfig.java            # 静态资源配置
│   │   │       ├── controller/                      # 控制层
│   │   │       │   ├── UserController.java          # 用户接口
│   │   │       │   ├── SongController.java          # 歌曲接口
│   │   │       │   ├── PostController.java          # 帖子接口
│   │   │       │   ├── CommentController.java       # 评论接口
│   │   │       │   ├── CollectionController.java    # 收藏接口
│   │   │       │   ├── SingerController.java        # 歌手接口
│   │   │       │   ├── SongSheetController.java     # 歌单接口
│   │   │       │   ├── BannerController.java        # 轮播图接口
│   │   │       │   └── AdminController.java         # 管理员接口
│   │   │       ├── service/                         # 服务层
│   │   │       │   ├── impl/                        # 实现类
│   │   │       │   └── (各 Service 接口)
│   │   │       ├── mapper/                          # 数据访问层
│   │   │       ├── model/                           # 数据模型
│   │   │       │   ├── domain/                      # 实体类
│   │   │       │   └── request/                     # 请求参数封装
│   │   │       └── handler/                         # 处理器
│   │   │           └── MyMetaObjectHandler.java     # MyBatis-Plus 字段自动填充
│   │   └── resources/
│   │       ├── application.properties               # 主配置文件
│   │       ├── application-dev.properties           # 开发环境配置
│   │       └── mapper/                              # 自定义 SQL
│   └── test/
│       └── java/com/music/
│           └── MusicApplicationTests.java           # 启动测试
├── init.sql                                          # 数据库初始化脚本
├── pom.xml                                           # Maven 配置
└── mvnw, mvnw.cmd                                    # Maven Wrapper
```

---

## 第五章 数据库设计

### 5.1 数据库概览

数据库名称：`music`（开发环境可命名为 `music_dev`）

主要数据表：

| 表名 | 中文名 | 用途 |
| :--- | :--- | :--- |
| `user` | 用户表 | 存储用户基本信息 |
| `song` | 歌曲表 | 存储上传的歌曲 |
| `singer` | 歌手表 | 存储歌手信息 |
| `song_sheet` | 歌单表 | 存储歌单 |
| `post` | 帖子表 | 存储用户发布的帖子 |
| `song_comment` | 歌曲评论表 | 歌曲的评论 |
| `post_comment` | 帖子评论表 | 帖子的评论 |
| `post_support` | 帖子点赞表 | 记录帖子点赞 |
| `comment_like` | 评论点赞表 | 记录评论点赞 |
| `collect` | 收藏表 | 记录歌曲收藏 |
| `banner` | 轮播图表 | 首页轮播图 |

### 5.2 核心表结构

#### 5.2.1 用户表 `user`

| 字段 | 类型 | 是否为空 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | AUTO_INCREMENT | 主键 |
| username | VARCHAR(255) | NOT NULL | - | 用户名（唯一） |
| password | VARCHAR(255) | NOT NULL | - | 密码（BCrypt 加密） |
| sex | VARCHAR(10) | NULL | - | 性别 |
| phone_num | VARCHAR(20) | NULL | - | 手机号 |
| email | VARCHAR(255) | NULL | - | 邮箱 |
| birth | DATE | NULL | - | 生日 |
| introduction | VARCHAR(500) | NULL | - | 个人简介 |
| avatar | VARCHAR(255) | NULL | 'avatar/user.jpg' | 头像路径 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

#### 5.2.2 歌曲表 `song`

| 字段 | 类型 | 是否为空 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | AUTO_INCREMENT | 主键 |
| singer_id | INT | NULL | - | 歌手 ID |
| name | VARCHAR(255) | NOT NULL | - | 歌曲名 |
| introduction | VARCHAR(500) | NULL | - | 歌曲简介 |
| pic | VARCHAR(255) | NULL | - | 封面路径 |
| url | VARCHAR(255) | NOT NULL | - | 歌曲文件路径 |
| lyric | TEXT | NULL | - | 歌词 |
| status | TINYINT | NOT NULL | 0 | 状态：0待审核 1通过 2驳回 |
| audit_reason | VARCHAR(500) | NULL | - | 审核原因 |
| play_count | INT | NOT NULL | 0 | 播放次数 |
| collect_count | INT | NOT NULL | 0 | 收藏数 |
| like_count | INT | NOT NULL | 0 | 点赞数 |
| user_id | INT | NULL | - | 上传者用户 ID |
| create_time | DATETIME | NOT NULL | - | 创建时间 |
| update_time | DATETIME | NOT NULL | - | 更新时间 |

#### 5.2.3 帖子表 `post`

| 字段 | 类型 | 是否为空 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | AUTO_INCREMENT | 主键 |
| user_id | INT | NOT NULL | - | 发帖用户 ID |
| title | VARCHAR(255) | NOT NULL | - | 帖子标题 |
| content | TEXT | NULL | - | 帖子内容 |
| cover | VARCHAR(255) | NULL | '/post/default.jpg' | 封面图 |
| status | TINYINT | NOT NULL | 0 | 状态：0待审核 1通过 2驳回 |
| audit_reason | VARCHAR(500) | NULL | - | 审核原因 |
| like_count | INT | NOT NULL | 0 | 点赞数 |
| comment_count | INT | NOT NULL | 0 | 评论数 |
| create_time | DATETIME | NOT NULL | - | 创建时间 |
| update_time | DATETIME | NOT NULL | - | 更新时间 |

#### 5.2.4 帖子点赞表 `post_support`

| 字段 | 类型 | 是否为空 | 说明 |
| :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | 主键 |
| user_id | INT | NOT NULL | 点赞用户 ID |
| post_id | INT | NOT NULL | 被点赞帖子 ID |
| create_time | DATETIME | NOT NULL | 点赞时间 |

#### 5.2.5 评论点赞表 `comment_like`

| 字段 | 类型 | 是否为空 | 说明 |
| :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | 主键 |
| user_id | INT | NOT NULL | 点赞用户 ID |
| comment_id | INT | NOT NULL | 被点赞评论 ID |
| comment_type | VARCHAR(20) | NOT NULL | 评论类型：song/post |
| create_time | DATETIME | NOT NULL | 点赞时间 |

#### 5.2.6 收藏表 `collect`

| 字段 | 类型 | 是否为空 | 说明 |
| :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | 主键 |
| user_id | INT | NOT NULL | 收藏用户 ID |
| song_id | INT | NOT NULL | 被收藏歌曲 ID |
| create_time | DATETIME | NOT NULL | 收藏时间 |

#### 5.2.7 歌曲评论表 `song_comment`

| 字段 | 类型 | 是否为空 | 说明 |
| :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | 主键 |
| user_id | INT | NOT NULL | 评论用户 ID |
| song_id | INT | NOT NULL | 被评论歌曲 ID |
| content | TEXT | NOT NULL | 评论内容 |
| parent_id | INT | NOT NULL | 父评论 ID（0=一级评论） |
| like_count | INT | NOT NULL | 点赞数 |
| create_time | DATETIME | NOT NULL | 评论时间 |

#### 5.2.8 帖子评论表 `post_comment`

| 字段 | 类型 | 是否为空 | 说明 |
| :--- | :--- | :--- | :--- |
| id | INT | NOT NULL | 主键 |
| user_id | INT | NOT NULL | 评论用户 ID |
| post_id | INT | NOT NULL | 被评论帖子 ID |
| content | TEXT | NOT NULL | 评论内容 |
| parent_id | INT | NOT NULL | 父评论 ID（0=一级评论） |
| like_count | INT | NOT NULL | 点赞数 |
| create_time | DATETIME | NOT NULL | 评论时间 |

### 5.3 表关系 ER 图

```
user (1) ──< (n) song              用户发布歌曲
user (1) ──< (n) post              用户发布帖子
user (1) ──< (n) song_comment      用户发表歌曲评论
user (1) ──< (n) post_comment      用户发表帖子评论
user (1) ──< (n) collect           用户收藏歌曲
user (1) ──< (n) post_support      用户点赞帖子
user (1) ──< (n) comment_like      用户点赞评论

song (1) ──< (n) song_comment      歌曲有多个评论
song (1) ──< (n) collect           歌曲被多人收藏
singer (1) ──< (n) song            歌手有多首歌曲

post (1) ──< (n) post_comment      帖子有多个评论
post (1) ──< (n) post_support      帖子被多人点赞
```

### 5.4 数据库初始化

执行 [init.sql](file:///c:/Users/niu%20xiaohan/music-new/init.sql) 即可初始化所有表结构和基础数据。

---

## 第六章 核心模块详细设计

### 6.1 用户管理模块

#### 6.1.1 用户注册

**业务流程**：
```
用户填写注册信息（用户名、密码等）
    ↓
后端校验用户名是否已存在
    ↓
不存在 → 密码 BCrypt 加密
    ↓
插入 user 表
    ↓
返回注册成功
```

**关键代码**（[UserServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/UserServiceImpl.java#L42-L71)）：

```java
public R addUser(UserRequest registryRequest) {
    // 1. 校验用户名是否已存在
    if (this.existUser(registryRequest.getUsername())) {
        return R.warning("用户名已注册");
    }
    
    // 2. 密码 BCrypt 加密
    String password = passwordEncoder.encode(registryRequest.getPassword());
    appUser.setPassword(password);
    
    // 3. 默认头像
    if (StringUtils.isBlank(appUser.getAvatar())) {
        appUser.setAvatar(DEFAULT_AVATAR);
    }
    
    // 4. 插入数据库
    appUserMapper.insert(appUser);
    return R.success("注册成功");
}
```

#### 6.1.2 用户登录

**业务流程**：
```
用户输入用户名、密码
    ↓
后端根据用户名查 user 表
    ↓
BCrypt 校验密码是否正确
    ↓
正确 → 存 Session → 返回用户信息
错误 → 返回"用户名或密码错误"
```

**关键代码**（[UserServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/UserServiceImpl.java#L199-L210)）：

```java
public R loginStatus(UserRequest loginRequest, HttpSession session) {
    String account = loginRequest.getUsername();
    String password = loginRequest.getPassword();
    
    // 1. 校验密码
    if (!this.verityPasswd(account, password)) {
        return R.error("用户名或密码错误");
    }
    
    // 2. 查询用户信息
    User appUser = findAppUserByLoginAccount(account);
    
    // 3. 存 Session
    session.setAttribute("username", appUser.getUsername());
    
    return R.success("登录成功", appUser);
}
```

**密码校验双支持**（兼容历史 MD5 密码）：

```java
private boolean matchesStoredPassword(User appUser, String password) {
    String storedPassword = appUser.getPassword();
    
    // 1. 先按 BCrypt 校验
    if (passwordEncoder.matches(password, storedPassword)) {
        return true;
    }
    
    // 2. 兼容历史 MD5 密码（带 SALT）
    String legacyPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    if (legacyPassword.equals(storedPassword)) {
        // 自动升级为 BCrypt
        User update = new User();
        update.setId(appUser.getId());
        update.setPassword(passwordEncoder.encode(password));
        appUserMapper.updateById(update);
        return true;
    }
    return false;
}
```

### 6.2 歌曲管理模块

#### 6.2.1 歌曲上传

**业务流程**：
```
用户选择 MP3 文件 + 填写歌曲信息
    ↓
文件传到后端
    ↓
FileUploadConfig 把文件存到本地
    ↓
往 song 表插入记录（status=0 待审核）
    ↓
返回"上传成功，等待审核"
```

**关键代码**（[SongServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/SongServiceImpl.java#L43-L90)）：

```java
public R uploadSong(SongRequest request, MultipartFile songFile, MultipartFile coverFile) {
    Song song = new Song();
    BeanUtils.copyProperties(request, song);
    
    try {
        // 1. 上传歌曲文件
        String songUrl = uploadUtil.upload(songFile, "song");
        song.setUrl(songUrl);
        
        // 2. 上传封面（可选）
        if (coverFile != null && !coverFile.isEmpty()) {
            song.setPic(uploadUtil.upload(coverFile, "songPic"));
        } else {
            song.setPic(DEFAULT_AVATAR);
        }
        
        // 3. 设置初始状态和计数
        song.setStatus(0);            // 待审核
        song.setPlayCount(0);
        song.setCollectCount(0);
        song.setLikeCount(0);
        song.setCreateTime(new Date());
        song.setUpdateTime(new Date());
        
        // 4. 入库
        songMapper.insert(song);
        return R.success("上传成功，等待管理员审核");
    } catch (Exception e) {
        return R.error(e.getMessage());
    }
}
```

#### 6.2.2 歌曲分页查询

**关键代码**：

```java
public R adminPageSong(Integer page, Integer size, Integer status) {
    Page<Song> pageInfo = new Page<>(page, size);
    QueryWrapper<Song> wrapper = new QueryWrapper<>();
    
    // 按状态过滤（可选）
    if (status != null) {
        wrapper.eq("status", status);
    }
    wrapper.orderByDesc("create_time");
    
    songMapper.selectPage(pageInfo, wrapper);
    
    // 批量填充上传者用户名（避免 N+1 查询）
    List<Song> records = pageInfo.getRecords();
    Set<Integer> userIds = records.stream()
            .map(Song::getUserId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    if (!userIds.isEmpty()) {
        // 一次性查所有用户，再 toMap 映射
        Map<Integer, User> userMap = userMapper.selectList(...).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        records.forEach(s -> {
            User u = userMap.get(s.getUserId());
            if (u != null) s.setUsername(u.getUsername());
        });
    }
    return R.success("查询成功", pageInfo);
}
```

### 6.3 帖子管理模块

#### 6.3.1 帖子发布

**关键代码**（[PostServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/PostServiceImpl.java#L50-L73)）：

```java
@Transactional
public R publishPost(PostRequest request) {
    Post post = new Post();
    BeanUtils.copyProperties(request, post);
    MultipartFile coverFile = request.getCoverFile();
    
    try {
        // 1. 处理封面
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverUrl = uploadUtil.upload(coverFile, "post");
            post.setCover(coverUrl);
        } else {
            post.setCover("/post/default.jpg");
        }
        
        // 2. 设置初始值
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        post.setStatus(0);            // 待审核
        post.setLikeCount(0);
        post.setCommentCount(0);
        
        // 3. 入库
        postMapper.insert(post);
        return R.success("发布成功，等待管理员审核");
    } catch (Exception e) {
        return R.error("发布异常：" + e.getMessage());
    }
}
```

#### 6.3.2 帖子点赞（Toggle 切换）

**关键代码**（[PostSupportServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/PostSupportServiceImpl.java#L24-L49)）：

```java
@Transactional
public R likePost(PostLikeRequest request) {
    Integer userId = request.getUserId();
    Integer postId = request.getPostId();
    
    // 查是否已点赞
    PostSupport record = getOne(wrapper);
    Post post = postMapper.selectById(postId);
    if (post == null) return R.error("帖子不存在");
    
    if (record == null) {
        // 没点过 → 点赞：新增 + 计数+1
        PostSupport support = new PostSupport();
        support.setUserId(userId);
        support.setPostId(postId);
        support.setCreateTime(new Date());
        save(support);
        post.setLikeCount(post.getLikeCount() + 1);
    } else {
        // 点过 → 取消点赞：删除 + 计数-1
        remove(wrapper);
        post.setLikeCount(post.getLikeCount() - 1);
    }
    postMapper.updateById(post);
    return R.success("操作成功");
}
```

### 6.4 管理员审核模块

#### 6.4.1 管理员登录

**关键代码**（[AdminController.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/AdminController.java#L25-L39)）：

```java
@PostMapping("/login")
public R login(@RequestBody Map<String, String> data, HttpSession session) {
    String username = data.get("username");
    String password = data.get("password");
    
    // 1. 校验密码
    if (!appUserService.verityPasswd(username, password)) {
        return R.error("用户名或密码错误");
    }
    
    // 2. 查询用户
    User user = appUserService.findAppUserByLoginAccount(username);
    session.setAttribute("username", user.getUsername());
    
    // 3. 返回角色信息
    Map<String, Object> result = new HashMap<>();
    result.put("username", user.getUsername());
    result.put("roles", Arrays.asList("admin"));  // 关键：返回 admin 角色
    return R.success("登录成功", result);
}
```

#### 6.4.2 歌曲审核

**关键代码**（[SongServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/SongServiceImpl.java#L165-L189)）：

```java
public R auditSong(Integer id, Integer status, String auditReason) {
    // 1. 校验歌曲是否存在
    Song song = songMapper.selectById(id);
    if (song == null) return R.error("不存在该歌曲");
    
    // 2. 校验状态合法性
    if (!status.equals(1) && !status.equals(2)) {
        return R.error("审核状态仅支持：1通过 / 2驳回");
    }
    
    // 3. 更新审核状态
    Song updateSong = new Song();
    updateSong.setId(id);
    updateSong.setStatus(status);
    updateSong.setAuditReason(auditReason);
    updateSong.setUpdateTime(new Date());
    songMapper.updateById(updateSong);
    
    return R.success("歌曲审核操作完成");
}
```

#### 6.4.3 仪表盘统计

**关键代码**（[AdminController.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/AdminController.java#L55-L79)）：

```java
@GetMapping("/dashboard")
public R dashboard() {
    Map<String, Object> data = new HashMap<>();
    
    // 已发布歌曲数
    data.put("songCount", songService.count(
        new QueryWrapper<Song>().eq("status", 1)));
    // 待审核歌曲数
    data.put("pendingSongCount", songService.count(
        new QueryWrapper<Song>().eq("status", 0)));
    
    // 已发布帖子数
    data.put("postCount", postService.count(
        new QueryWrapper<Post>().eq("status", 1)));
    // 待审核帖子数
    data.put("pendingPostCount", postService.count(
        new QueryWrapper<Post>().eq("status", 0)));
    
    // 总用户数
    data.put("userCount", appUserService.count());
    
    return R.success("ok", data);
}
```

### 6.5 文件上传模块

#### 6.5.1 文件上传核心实现

**文件**：[FileUploadConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/FileUploadConfig.java)

**完整流程**：

```java
public String upload(MultipartFile file, String folder) throws IOException {
    // 1. 校验文件非空
    if (file == null || file.isEmpty()) {
        throw new RuntimeException("文件不能为空");
    }
    
    // 2. 提取文件后缀
    String filename = file.getOriginalFilename();
    String suffix = "";
    if (filename != null && filename.contains(".")) {
        suffix = filename.substring(filename.lastIndexOf("."));
    }
    
    // 3. UUID 重命名（避免冲突）
    String newName = UUID.randomUUID() + suffix;
    
    // 4. 拼接保存路径
    String dir = fileConfig.getPath() + File.separator + folder;
    
    // 5. 目录不存在则自动创建
    File uploadDir = new File(dir);
    if (!uploadDir.exists()) {
        uploadDir.mkdirs();
    }
    
    // 6. 落盘
    File dest = new File(uploadDir, newName);
    file.transferTo(dest);
    
    // 7. 返回相对路径（用于存数据库）
    return "/" + folder + "/" + newName;
}
```

**文件命名规则**：UUID 随机字符串 + 原始后缀，例如 `a3f5b8c9-1234-5678-90ab-cdef12345678.mp3`

**文件夹分类**：
- `avatar/` —— 用户头像
- `post/` —— 帖子封面
- `song/` —— 歌曲文件
- `songPic/` —— 歌曲封面

#### 6.5.2 静态资源访问

**文件**：[WebMvcConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/WebMvcConfig.java)

**配置示例**：

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 让浏览器能通过 URL 访问本地文件
    registry.addResourceHandler("/song/**")
            .addResourceLocations("file:" + fileConfig.getSongPath());
    registry.addResourceHandler("/avatar/**")
            .addResourceLocations("file:" + fileConfig.getAvatarPath());
    // ... 其他静态资源
}
```

**效果**：用户上传一首歌到 `D:/uploads/song/xxx.mp3`，前端可以通过 `http://localhost:8888/song/xxx.mp3` 直接访问。

---

## 第七章 API 接口文档

### 7.1 统一规范

#### 7.1.1 基础地址

```
开发环境：http://localhost:8888
```

#### 7.1.2 统一返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "success": true,
  "type": "success",
  "data": null
}
```

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| code | int | 状态码：200成功，500失败 |
| message | String | 提示信息 |
| success | Boolean | 是否成功 |
| type | String | success/warning/error/fatal |
| data | Object | 业务数据 |

#### 7.1.3 错误码说明

| 状态码 | 含义 | 使用场景 |
| :--- | :--- | :--- |
| 200 | 成功 | 操作成功 |
| 500 | 服务器错误 | 系统异常、致命错误 |

### 7.2 用户模块接口

#### 7.2.1 用户注册

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/user/add` |
| 请求方式 | POST |
| Content-Type | application/json |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| sex | String | 否 | 性别 |
| phoneNum | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| birth | Date | 否 | 生日 |

**请求示例**：

```json
{
  "username": "test001",
  "password": "123456",
  "sex": "男",
  "email": "test001@example.com"
}
```

**返回示例**：

```json
{
  "code": 200,
  "message": "注册成功",
  "success": true
}
```

#### 7.2.2 用户登录

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/user/login` |
| 请求方式 | POST |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**返回示例（成功）**：

```json
{
  "code": 200,
  "message": "登录成功",
  "success": true,
  "data": {
    "id": 1,
    "username": "test001",
    "avatar": "avatar/user.jpg"
  }
}
```

#### 7.2.3 用户分页查询

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/user/page` |
| 请求方式 | GET |

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |

#### 7.2.4 更新用户信息

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/user/update` |
| 请求方式 | POST |

#### 7.2.5 修改密码

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/user/updatePassword` |
| 请求方式 | POST |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 用户名 |
| oldPassword | String | 是 | 旧密码 |
| password | String | 是 | 新密码 |

#### 7.2.6 上传头像

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/user/avatar/update` |
| 请求方式 | POST |
| Content-Type | multipart/form-data |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| file | File | 是 | 头像图片 |
| id | int | 是 | 用户 ID |

### 7.3 歌曲模块接口

#### 7.3.1 歌曲上传

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/upload` |
| 请求方式 | POST |
| Content-Type | multipart/form-data |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| name | String | 是 | 歌曲名 |
| singerId | int | 否 | 歌手 ID |
| introduction | String | 否 | 歌曲简介 |
| lyric | String | 否 | 歌词 |
| songFile | File | 是 | 歌曲文件（MP3） |
| coverFile | File | 否 | 歌曲封面 |

#### 7.3.2 歌曲修改

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/update` |
| 请求方式 | POST |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| id | int | 是 | 歌曲 ID |
| name | String | 否 | 新名称 |
| ... | | | 其他字段可选更新 |
| songFile | File | 否 | 新歌曲文件 |
| coverFile | File | 否 | 新封面 |

#### 7.3.3 歌曲删除

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/delete` |
| 请求方式 | DELETE |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| id | int | 是 | 歌曲 ID |

#### 7.3.4 查询我的歌曲

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/user` |
| 请求方式 | GET |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | int | 是 | 用户 ID |

#### 7.3.5 歌曲详情

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/detail` |
| 请求方式 | GET |

#### 7.3.6 热门歌曲

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/hot` |
| 请求方式 | GET |

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| limit | int | 否 | 5 | 返回条数 |

#### 7.3.7 歌曲收藏 Toggle

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/collect` |
| 请求方式 | POST |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | int | 是 | 用户 ID |
| songId | int | 是 | 歌曲 ID |

#### 7.3.8 用户收藏列表

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/collect/list` |
| 请求方式 | GET |

### 7.4 帖子模块接口

#### 7.4.1 帖子发布

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/publish` |
| 请求方式 | POST |
| Content-Type | multipart/form-data |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | int | 是 | 发帖用户 ID |
| title | String | 是 | 标题 |
| content | String | 是 | 内容 |
| coverFile | File | 否 | 封面图 |

#### 7.4.2 帖子修改

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/update` |
| 请求方式 | POST |

#### 7.4.3 帖子删除

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/delete` |
| 请求方式 | DELETE |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| postId | int | 是 | 帖子 ID |
| loginUserId | int | 是 | 当前登录用户 ID（用于归属校验） |

#### 7.4.4 帖子详情

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/detail` |
| 请求方式 | GET |

#### 7.4.5 帖子分页（首页）

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/page` |
| 请求方式 | GET |

**请求参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |

#### 7.4.6 我的帖子

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/user/list` |
| 请求方式 | GET |

#### 7.4.7 帖子点赞

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/like` |
| 请求方式 | POST |

### 7.5 评论模块接口

#### 7.5.1 帖子发表评论

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/comment/add` |
| 请求方式 | POST |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | int | 是 | 评论用户 ID |
| targetId | int | 是 | 帖子 ID |
| content | String | 是 | 评论内容 |
| parentId | int | 否 | 父评论 ID（0=一级评论） |

#### 7.5.2 帖子评论列表

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/comment/list` |
| 请求方式 | GET |

#### 7.5.3 歌曲发表评论

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/comment/add` |
| 请求方式 | POST |

#### 7.5.4 歌曲评论列表

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/song/comment/list` |
| 请求方式 | GET |

#### 7.5.5 评论点赞

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/post/comment/like` 或 `/song/comment/like` |
| 请求方式 | POST |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | int | 是 | 用户 ID |
| commentId | int | 是 | 评论 ID |

### 7.6 管理员模块接口

#### 7.6.1 管理员登录

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/login` |
| 请求方式 | POST |

**返回示例**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "username": "admin",
    "roles": ["admin"],
    "avatar": "avatar/admin.jpg"
  }
}
```

#### 7.6.2 仪表盘统计

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/dashboard` |
| 请求方式 | GET |

**返回示例**：

```json
{
  "code": 200,
  "data": {
    "songCount": 100,
    "pendingSongCount": 5,
    "postCount": 200,
    "pendingPostCount": 3,
    "userCount": 50
  }
}
```

#### 7.6.3 管理员分页查询歌曲

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/song/page` |
| 请求方式 | GET |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| status | int | 否 | 0待审核 1通过 2驳回 |

#### 7.6.4 歌曲审核

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/song/audit` |
| 请求方式 | POST |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| id | int | 是 | 歌曲 ID |
| status | int | 是 | 1通过 2驳回 |
| auditReason | String | 否 | 审核原因 |

#### 7.6.5 管理员分页查询帖子

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/post/page` |
| 请求方式 | GET |

#### 7.6.6 帖子审核

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/post/audit` |
| 请求方式 | POST |

#### 7.6.7 管理员删除歌曲

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/song/delete` |
| 请求方式 | GET |

#### 7.6.8 管理员删除帖子

| 项目 | 内容 |
| :--- | :--- |
| 接口地址 | `/admin/post/delete` |
| 请求方式 | DELETE |

---

## 第八章 部署与运维

### 8.1 环境要求

| 环境 | 版本要求 |
| :--- | :--- |
| JDK | 1.8+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| 操作系统 | Windows / Linux / macOS |

### 8.2 本地启动步骤

#### 步骤 1：初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE music DEFAULT CHARACTER SET utf8mb4;

# 初始化表结构
source /path/to/init.sql;
```

#### 步骤 2：修改配置

编辑 [application-dev.properties](file:///c:/Users/niu%20xiaohan/music-new/src/main/resources/application-dev.properties)：

```properties
# 数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/music?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的密码

# 服务端口
server.port=8888

# 文件上传路径
file.path=D:/uploads/
```

#### 步骤 3：启动项目

```bash
# 在项目根目录执行
mvn spring-boot:run

# 或先打包再运行
mvn clean package
java -jar target/music-0.0.1-SNAPSHOT.jar
```

#### 步骤 4：验证启动

看到以下日志说明启动成功：

```
Started MusicApplication in 3.456 seconds
Tomcat started on port(s): 8888 (http)
```

### 8.3 服务器部署

#### 8.3.1 上传 JAR 包

```bash
scp target/music-0.0.1-SNAPSHOT.jar user@server:/app/
```

#### 8.3.2 后台运行

```bash
# Linux 服务器
nohup java -jar /app/music-0.0.1-SNAPSHOT.jar > /app/logs/music.log 2>&1 &

# 查看日志
tail -f /app/logs/music.log
```

#### 8.3.3 配置开机自启（Linux）

创建 `/etc/systemd/system/music.service`：

```ini
[Unit]
Description=Music Service
After=network.target

[Service]
Type=simple
User=app
ExecStart=/usr/bin/java -jar /app/music-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
systemctl enable music.service
systemctl start music.service
```

### 8.4 常见运维问题

| 问题 | 解决方案 |
| :--- | :--- |
| 端口被占用 | `netstat -ano | findstr 8888` 查进程，杀掉 |
| 数据库连不上 | 检查 MySQL 是否启动、账号密码是否正确 |
| 上传文件 404 | 检查 `WebMvcConfig` 的静态资源映射路径 |
| 中文乱码 | 确认数据库字符集为 utf8mb4，URL 包含 `characterEncoding=utf-8` |

---

## 第九章 测试说明

### 9.1 测试策略

项目采用**分层测试**策略：

| 层级 | 测试方式 | 工具 |
| :--- | :--- | :--- |
| 单元测试 | 测试 Service 核心方法 | JUnit 5 + Mockito |
| 集成测试 | 测试 Controller 接口 | Spring Boot Test |
| 接口测试 | 手动测试所有 RESTful API | Postman / Apifox |
| 联调测试 | 前后端联调 | 浏览器 + 开发者工具 |

### 9.2 接口测试清单

参考文档 [backend-test-report.md](file:///c:/Users/niu%20xiaohan/music-new/backend-test-report.md)，共 **63 个测试用例**，全部通过。

### 9.3 Postman 测试示例

#### 测试用例：用户登录

```
请求方式：POST
URL：http://localhost:8888/user/login
Headers：Content-Type: application/json
Body：
{
  "username": "admin",
  "password": "123456"
}
```

预期响应：

```json
{
  "code": 200,
  "message": "登录成功",
  "success": true,
  "data": {
    "id": 1,
    "username": "admin"
  }
}
```

### 9.4 Bug 记录模板

| 编号 | 描述 | 复现步骤 | 修复方案 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| BUG-001 | 用户名重复时返回码不明确 | 重复注册同一用户名 | 统一返回 warning 类型 | 已修复 |

---

## 第十章 常见问题 FAQ

### Q1：跨域问题如何解决？

**答**：在 [CorsConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/CorsConfig.java) 中配置跨域，允许所有来源访问。

### Q2：密码忘记如何重置？

**答**：当前没有自助找回密码功能，需要管理员在数据库直接重置（使用 BCrypt 加密后的密码）。

### Q3：上传文件大小有限制吗？

**答**：Spring Boot 默认单文件最大 1MB，可在 `application.properties` 中调整：

```properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=100MB
```

### Q4：如何添加新的审核状态？

**答**：修改 `song` 和 `post` 表的 `status` 字段，扩展业务逻辑（0=待审核，1=已通过，2=已驳回，3=...）。

### Q5：管理员账号如何创建？

**答**：管理员账号本质就是 `user` 表中的一条普通用户记录，前端根据登录后返回的 `roles` 字段判断是否有管理权限。

### Q6：分页查询如何按指定字段排序？

**答**：在 Service 中使用 `wrapper.orderByDesc("字段名")` 或 `wrapper.orderByAsc("字段名")`。

### Q7：如何提升查询性能？

**答**：
1. 关联查询用 in 批量查，避免 N+1
2. 数据库表高频查询字段加索引
3. 大数据量场景考虑引入 Redis 缓存

### Q8：项目如何打包发布？

**答**：

```bash
mvn clean package
# 生成的 jar 包在 target/ 目录下
java -jar target/music-0.0.1-SNAPSHOT.jar
```

---

## 附录

### 附录 A：相关文件链接

- [项目启动类](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/MusicApplication.java)
- [配置文件](file:///c:/Users/niu%20xiaohan/music-new/src/main/resources/application.properties)
- [数据库初始化脚本](file:///c:/Users/niu%20xiaohan/music-new/init.sql)
- [Maven 配置](file:///c:/Users/niu%20xiaohan/music-new/pom.xml)
- [后端测试报告](file:///c:/Users/niu%20xiaohan/music-new/backend-test-report.md)

### 附录 B：常用命令速查

```bash
# 启动项目
mvn spring-boot:run

# 清理 + 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 查看依赖树
mvn dependency:tree

# 运行单个测试类
mvn test -Dtest=UserServiceTest
```

### 附录 C：技术关键词

- **Spring Boot**：Java 企业级开发框架
- **MyBatis-Plus**：MyBatis 增强工具
- **BCrypt**：密码哈希算法
- **RESTful**：一种 API 设计风格
- **JSON**：数据交换格式
- **HTTP**：超文本传输协议
- **Session**：服务端会话管理
- **MVC**：Model-View-Controller 架构模式
- **ORM**：对象关系映射
- **MultipartFile**：Spring 提供的文件上传对象

---

**文档结束**

如需补充或修改任何部分，请联系项目后端负责人。
