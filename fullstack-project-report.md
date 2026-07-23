# 音乐平台 — 数据库持久层与互动功能技术报告（全栈）

> **负责人职责**：数据库及 MyBatis-Plus 持久层建设，以及歌曲收藏、帖子点赞、歌曲与帖子评论回复等互动功能
> **技术栈**：  
> 后端 — Java 17 + Spring Boot 2.6.2 + MyBatis-Plus 3.5.1 + MySQL 8.0 + Redis  
> 前端 — Vue 3 + Element Plus + Pinia + Axios + Vue Router  
> **日期**：2026-07-23

---

## 目录

1. [整体架构概览](#一整体架构概览)
2. [数据库设计](#二数据库设计)
3. [MyBatis-Plus 持久层](#三mybatis-plus-持久层)
4. [互动功能 — 后端实现](#四互动功能后端实现)
5. [互动功能 — 前端实现](#五互动功能前端实现)
6. [前后端数据流完整链路](#六前后端数据流完整链路)
7. [API 接口全表](#七api-接口全表)
8. [关键技术要点总结](#八关键技术要点总结)

---

## 一、整体架构概览

### 1.1 项目组成

本项目由三个子项目构成：

| 项目 | 路径 | 说明 |
|------|------|------|
| music-new | `C:\Users\niu xiaohan\music-new\` | Java 后端，Spring Boot 2.6.2 |
| music_frontend | `C:\Users\niu xiaohan\music_frontend\` | Vue 3 用户端前端 |
| music_admin | `C:\Users\niu xiaohan\music_admin\` | Vue 3 管理后台前端 |

### 1.2 三层架构（后端）

```
用户浏览器 / Vue 前端
       │  HTTP 请求（GET/POST/DELETE）
       │  数据格式：JSON / FormData
       ▼
┌─────────────────────────────────────────┐
│  Controller 层（控制器）                  │
│  职责：接收请求 → 参数校验 → 调用 Service   │
│  注解：@RestController + @RequestMapping │
│  文件：SongController、PostController 等  │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│  Service 层（业务逻辑）                   │
│  职责：业务规则处理、数据组装、事务控制     │
│  注解：@Service                          │
│  文件：CollectServiceImpl 等              │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│  Mapper 层（数据访问）                    │
│  职责：与 MySQL 数据库交互                │
│  基于 MyBatis-Plus 的 BaseMapper<T>      │
│  无需手写 SQL 即可完成常规 CRUD 操作      │
└──────────────────┬──────────────────────┘
                   │ SQL
                   ▼
┌─────────────────────────────────────────┐
│  MySQL 8.0 数据库（musictest）            │
│  8 张业务表，utf8mb4 字符集               │
└─────────────────────────────────────────┘
```

### 1.3 前端架构

```
Vue 3 应用
    │
    ├── API 层（api/）
    │   - song.js：歌曲接口封装
    │   - post.js：帖子接口封装
    │   - comment.js：评论接口封装
    │
    ├── 状态管理（store/）
    │   - user.js（Pinia）：用户登录状态、个人信息
    │
    ├── 页面组件（pages/）
    │   - song/SongDetail.vue：歌曲详情 + 收藏 + 评论
    │   - post/PostDetail.vue：帖子详情 + 点赞 + 评论
    │   - user/UserPersonal.vue：个人主页（作品/收藏/点赞）
    │   - user/UserFavorite.vue：收藏列表
    │   - user/UserLike.vue：点赞记录列表
    │
    └── 工具函数（utils/）
        - request.js：Axios 请求封装
        - index.js：图片 URL 拼接
```

### 1.4 关键概念通俗解释

| 术语 | 通俗比喻 | 在项目中的体现 |
|------|----------|---------------|
| **Controller** | 餐厅服务员 — 接待客人，传达订单，端菜 | `@RestController` 类，处理 HTTP 请求 |
| **Service** | 后厨大厨 — 真正的烹饪逻辑 | `@Service` 类，业务规则 + 事务控制 |
| **Mapper** | 仓库管理员 — 存取食材 | `BaseMapper<T>` 接口，操作数据库 |
| **MyBatis-Plus** | 自动仓库管理员 — 不用手写 SQL | 自动生成增删改查 |
| **Vue 组件** | 乐高积木 — 每个 UI 块是一个组件 | `.vue` 单文件组件 |
| **Pinia Store** | 全局共享的公告栏 — 所有组件都能读写 | `store/user.js` 管理用户状态 |
| **Axios** | 邮递员 — 前端向后端发送请求 | `utils/request.js` 封装 HTTP 调用 |

---

## 二、数据库设计

### 2.1 数据库基本信息

- **数据库名**：`musictest`
- **字符集**：`utf8mb4`（支持所有 Unicode 字符，包括 emoji）
- **引擎**：InnoDB（支持事务、外键）
- **创建脚本**：`music-new/init.sql`

### 2.2 核心业务表（互动功能相关）

#### 2.2.1 歌曲表 — `song`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 歌曲唯一编号 |
| user_id | INT | 上传者 ID，关联 user 表 |
| name | VARCHAR(128) | 歌曲名称 |
| introduction | VARCHAR(512) | 歌曲简介 / 演唱者 |
| singer_id | INT | 关联歌手 ID |
| status | INT | 0=待审核, 1=已通过, 2=已驳回 |
| play_count | INT | 播放次数 |
| **collect_count** | INT | **被收藏次数（冗余字段）** |
| **like_count** | INT | **被点赞次数（冗余字段）** |
| pic | VARCHAR(255) | 封面图片路径 |
| lyric | TEXT | 歌词 |
| url | VARCHAR(255) | 音频文件路径 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 最后更新时间 |

#### 2.2.2 帖子表 — `post`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 帖子编号 |
| user_id | INT | 发帖用户 ID |
| title | VARCHAR(255) | 帖子标题 |
| content | TEXT | 帖子正文 |
| cover | VARCHAR(255) | 封面图 |
| status | INT | 审核状态 |
| **like_count** | INT | **被点赞次数（冗余字段）** |
| **comment_count** | INT | **评论总数（冗余字段）** |
| create_time | DATETIME | 创建时间 |

#### 2.2.3 收藏表 — `collect`

记录用户-歌曲收藏关系，是一张**中间表 / 关联表**。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 记录编号 |
| user_id | INT | 用户 ID |
| song_id | INT | 歌曲 ID |
| create_time | DATETIME | 收藏时间 |

```
一行记录 = 一次收藏
有记录 = 已收藏
无记录 = 未收藏
```

#### 2.2.4 帖子点赞表 — `post_support`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 记录编号 |
| user_id | INT | 用户 ID |
| post_id | INT | 帖子 ID |
| create_time | DATETIME | 点赞时间 |

#### 2.2.5 歌曲评论表 — `song_comment`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 评论编号 |
| song_id | INT | 所属歌曲 ID |
| user_id | INT | 评论者 ID |
| content | TEXT | 评论内容 |
| **parent_id** | INT | **0=一级评论, >0=回复某条评论** |
| **like_count** | INT | **被点赞次数（冗余字段）** |
| create_time | DATETIME | 评论时间 |

#### 2.2.6 帖子评论表 — `post_comment`

与 `song_comment` 结构完全相同，只是 `song_id` 换成了 `post_id`。

#### 2.2.7 评论点赞表 — `comment_like`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 记录编号 |
| user_id | INT | 用户 ID |
| comment_id | INT | 评论 ID |
| **comment_type** | VARCHAR(10) | `song` 或 `post`，区分来源 |
| create_time | DATETIME | 点赞时间 |

**唯一约束**：`UNIQUE (user_id, comment_id, comment_type)` — 同一用户对同一评论只能点赞一次。

#### 2.2.8 用户表 — `user`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 用户编号 |
| username | VARCHAR(64) | 用户名 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| sex | VARCHAR(10) | 性别 |
| avatar | VARCHAR(255) | 头像图片路径 |
| introduction | VARCHAR(512) | 个人简介 |
| create_time | DATETIME | 注册时间 |

### 2.3 表关系全景图

```
                        ┌──────────────────────┐
                        │        user           │
                        │  (用户表，核心实体)      │
                        └──────┬───────┬───────┘
                               │       │
              ┌────────────────┘       └────────────────┐
              ▼                                         ▼
    ┌─────────────────┐                      ┌─────────────────┐
    │      song       │                      │      post        │
    │   (歌曲表)       │                      │   (帖子表)        │
    └───┬─────┬───────┘                      └───┬──────┬──────┘
        │     │                                   │      │
   ┌────┘     └────┐                         ┌────┘      └────┐
   ▼               ▼                         ▼                ▼
┌────────┐  ┌──────────────┐         ┌──────────────┐  ┌──────────────┐
│collect │  │song_comment  │         │post_support  │  │post_comment  │
│(收藏表) │  │(歌曲评论表)   │         │(帖子点赞表)    │  │(帖子评论表)   │
└────────┘  └──────┬───────┘         └──────────────┘  └──────┬───────┘
                   │                                          │
                   └──────────────┬───────────────────────────┘
                                  ▼
                        ┌──────────────────┐
                        │  comment_like    │
                        │ (评论点赞表，共用)  │
                        └──────────────────┘
```

### 2.4 设计要点

#### 冗余计数字段
`song.collect_count`、`post.like_count`、`song_comment.like_count` 这些数字可以从关联表中实时 COUNT 出来。但我们把它们**直接存**在表里，原因是：

- 展示歌曲列表时，不需要 JOIN 其他表来做子查询
- 查询从 N+1 次变成 1 次
- 代价：每次互动操作时需要同步更新这个字段（必须在事务中完成）

**举例**：如果不冗余存储 `like_count`，每次展示帖子列表时，执行流程是：
```
❌ SELECT * FROM post                           -- 查帖子（1 次）
❌ SELECT COUNT(*) FROM post_support WHERE post_id=1  -- 查点赞数
❌ SELECT COUNT(*) FROM post_support WHERE post_id=2
❌ ...（每个帖子要单独查一次，N 次查询）

✅ 有冗余字段后：
✅ SELECT * FROM post                           -- 查帖子（1 次）
✅ like_count 直接就在查询结果里，不需要额外查询
```

#### 唯一约束
`comment_like` 表的 `UNIQUE (user_id, comment_id, comment_type)` 从数据库层面禁止了重复点赞。即使代码有 bug 尝试重复插入，数据库也会直接拒绝，保证数据一致性。

---

## 三、MyBatis-Plus 持久层

### 3.1 什么是 MyBatis-Plus

传统方式操作 MySQL 需要手写大量 SQL 语句。MyBatis-Plus 把这个过程自动化了，它根据 Java 类定义自动生成对应的 SQL，开发者只需要调用几个方法名就能完成增删改查。

### 3.2 三步搭建持久层

**第一步：定义实体类 (Domain)**

每个数据库表对应一个 Java 类。以 `Collect.java` 为例：

```java
@Data                              // Lombok：自动生成 getter/setter/toString
@TableName("collect")              // 映射到数据库的 collect 表
public class Collect {
    @TableId(type = IdType.AUTO)   // 主键，数据库自增
    private Integer id;
    private Integer userId;        // 驼峰命名 → 自动映射到 user_id 列
    private Integer songId;        // 自动映射到 song_id
    private Date createTime;       // 自动映射到 create_time
}
```

**命名规则**：Java 驼峰命名 `userId` 会自动转换为数据库下划线命名 `user_id`，反之亦然。

**第二步：定义 Mapper 接口**

```java
@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
    // 不需要写任何方法！
    // BaseMapper 已经提供了所有基本操作
}
```

`BaseMapper<Collect>` 自动提供的方法（无需手写 SQL）：

| 方法 | 功能 |
|------|------|
| `selectById(id)` | 根据主键查一条记录 |
| `selectList(wrapper)` | 根据条件查多条 |
| `selectPage(page, wrapper)` | 分页查询 |
| `insert(entity)` | 插入一条 |
| `updateById(entity)` | 根据主键更新 |
| `deleteById(id)` | 根据主键删除 |
| `count(wrapper)` | 统计数量 |

**第三步：在 Service 中用 QueryWrapper 构建查询**

```java
// 传统方式需要手写 SQL：
// SELECT * FROM collect WHERE user_id = 1 ORDER BY create_time DESC

// MyBatis-Plus 方式（纯 Java 代码，不需要 SQL）：
QueryWrapper<Collect> wrapper = new QueryWrapper<>();
wrapper.eq("user_id", userId)            // eq = equals, WHERE user_id = ?
       .orderByDesc("create_time");      // ORDER BY create_time DESC
List<Collect> list = collectMapper.selectList(wrapper);
```

`QueryWrapper` 是一个"查询条件构造器"，通过链式调用 Java 方法生成对应的 SQL。常用方法：

| 方法 | SQL 等价 |
|------|----------|
| `eq("user_id", 1)` | `WHERE user_id = 1` |
| `in("id", list)` | `WHERE id IN (1,2,3)` |
| `orderByDesc("create_time")` | `ORDER BY create_time DESC` |
| `like("name", "周")` | `WHERE name LIKE '%周%'` |

**文件对照**：项目中 11 个实体类对应的 Mapper 接口：

| 实体类 | Mapper 接口 | 对应表 |
|--------|------------|--------|
| User.java | UserMapper.java | user |
| Song.java | SongMapper.java | song |
| Post.java | PostMapper.java | post |
| Collect.java | CollectMapper.java | collect |
| PostSupport.java | PostSupportMapper.java | post_support |
| SongComment.java | SongCommentMapper.java | song_comment |
| PostComment.java | PostCommentMapper.java | post_comment |
| CommentLike.java | CommentLikeMapper.java | comment_like |
| Singer.java | SingerMapper.java | singer |
| Banner.java | BannerMapper.java | banner |
| SongSheet.java | SongSheetMapper.java | song_sheet |

### 3.3 分页支持

在 `MybatisPlusConfig.java` 中注册分页插件：

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(
            new PaginationInnerInterceptor(DbType.MYSQL)  // 针对 MySQL 优化
        );
        return interceptor;
    }
}
```

使用方式：

```java
Page<Song> page = new Page<>(pageNum, pageSize);  // 第几页，每页几条
songMapper.selectPage(page, wrapper);
// page.getRecords() → 当前页数据
// page.getTotal()   → 总条数
// page.getPages()   → 总页数
```

### 3.4 统一响应格式

所有接口都使用 `R` 类统一响应格式：

```java
public class R {
    private int code;        // 状态码（200）
    private String message;  // 提示信息（如"收藏成功"）
    private String type;     // 类型（success / error / warning）
    private Boolean success; // 是否成功
    private Object data;     // 实际数据（可以是对象、列表、Map 等）
}
```

使用示例：

```java
// 成功，带数据
R.success("查询成功", songList);

// 成功，不带数据
R.success("删除成功");

// 失败
R.error("评论不存在");
// 返回 JSON：{"code":200, "message":"评论不存在", "type":"error", "success":false}
```

这样做的好处是前端处理响应时格式完全一致，无需为每个接口写不同的解析逻辑。

---

## 四、互动功能 — 后端实现

### 4.1 歌曲收藏

**功能**：用户点击收藏按钮 → 收藏歌曲；再次点击 → 取消收藏。

**涉及文件**：

| 层 | 文件 |
|----|------|
| 实体 | `model/domain/Collect.java` |
| Mapper | `mapper/CollectMapper.java` |
| 请求 | `model/request/CollectRequest.java` |
| 接口 | `service/CollectService.java` |
| 实现 | `service/impl/CollectServiceImpl.java` |
| 控制器 | `controller/CollectionController.java` |

**核心逻辑流程图**：

```
POST /song/collect
请求体：{ userId: 1, songId: 100 }
              │
              ▼
    ┌─────────────────────┐
    │ 查询 collect 表：     │
    │ userId=1 AND songId=100 ? │
    └─────────┬───────────┘
              │
    ┌─────────┴──────────┐
    │                    │
    ▼                    ▼
  有记录                 无记录
    │                    │
    ▼                    ▼
 执行 DELETE           执行 INSERT
 取消收藏               添加收藏
    │                    │
    └────────┬───────────┘
             ▼
  返回 R.success("操作成功")
```

这就是 **Toggle（切换）模式**：同一个接口，同一个按钮，点一下是收藏，再点一下是取消。前端不需要区分当前状态。

**关键代码（CollectServiceImpl.java）**：

```java
@Transactional  // 确保操作原子性
public R collectSong(CollectRequest request) {
    // 1. 查是否已收藏
    QueryWrapper<Collect> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id", request.getUserId())
           .eq("song_id", request.getSongId());
    Collect record = getOne(wrapper);

    if (record == null) {
        // 2a. 没收藏 → 新增
        Collect collect = new Collect();
        collect.setUserId(request.getUserId());
        collect.setSongId(request.getSongId());
        collect.setCreateTime(new Date());
        save(collect);
    } else {
        // 2b. 已收藏 → 删除（取消收藏）
        remove(wrapper);
    }
    return R.success("操作成功");
}
```

**获取收藏列表 — 批量查询优化**：

```java
public R getUserCollect(Integer userId) {
    // 1. 查该用户所有收藏记录
    List<Collect> collectList = ...;
    
    // 2. 收集所有 songId
    List<Integer> songIds = collectList.stream()
        .map(Collect::getSongId).collect(Collectors.toList());
    
    // 3. 一次性批量查询所有歌曲（避免 N+1）
    List<Song> songs = songMapper.selectList(
        new QueryWrapper<Song>().in("id", songIds)
    );
    
    // 4. 建立 songId → Song 映射
    Map<Integer, Song> songMap = songs.stream()
        .collect(Collectors.toMap(Song::getId, s -> s));
    
    // 5. 组装返回数据
    // 每条记录包含：收藏时间 + 歌曲名 + 歌手 + 封面 + 音频URL
}
```

### 4.2 帖子点赞

**功能**：在帖子详情页，点击星星图标点赞/取消点赞帖子。

**涉及文件**：

| 层 | 文件 |
|----|------|
| 实体 | `model/domain/PostSupport.java` |
| Mapper | `mapper/PostSupportMapper.java` |
| 请求 | `model/request/PostLikeRequest.java` |
| 接口 | `service/PostSupportService.java` |
| 实现 | `service/impl/PostSupportServiceImpl.java` |
| 控制器 | `controller/PostController.java`（`/post/like`） |

**核心逻辑流程图**：

```
POST /post/like
请求体：{ userId: 1, postId: 50 }
              │
              ▼
    ┌──────────────────────────┐
    │ 查 post_support 表：       │
    │ userId=1 AND postId=50 ?  │
    └─────────┬────────────────┘
              │
    ┌─────────┴────────────┐
    │                      │
    ▼ 有记录               ▼ 无记录
DELETE 点赞记录           INSERT 点赞记录
UPDATE post 表             UPDATE post 表
  like_count - 1            like_count + 1
  （帖子点赞数-1）            （帖子点赞数+1）
    │                      │
    └──────────┬───────────┘
               ▼
   返回 R.success("操作成功")
```

和收藏最大的不同：这里不仅要插入/删除 `post_support` 记录，还要**同步更新 `post` 表的 `like_count` 字段**。两个操作必须同时成功或同时失败，所以需要 `@Transactional`。

```java
@Transactional  // 两个数据库操作绑定为一个事务
public R likePost(PostLikeRequest request) {
    PostSupport record = getOne(wrapper);
    Post post = postMapper.selectById(postId);
    
    if (record == null) {
        // 点赞
        save(newSupport);
        post.setLikeCount(post.getLikeCount() + 1);  // +1
    } else {
        // 取消点赞
        remove(wrapper);
        post.setLikeCount(post.getLikeCount() - 1);  // -1
    }
    postMapper.updateById(post);  // 同步更新冗余字段
    return R.success("操作成功");
}
```

### 4.3 评论回复

**功能**：用户对歌曲/帖子发表评论，也可以回复别人的评论，形成嵌套的"楼中楼"结构。

**涉及文件**：

| 层 | 文件 |
|----|------|
| 实体 | `model/domain/SongComment.java`, `model/domain/PostComment.java` |
| Mapper | `mapper/SongCommentMapper.java`, `mapper/PostCommentMapper.java` |
| 请求 | `model/request/CommentRequest.java` |
| 接口 | `service/SongCommentService.java`, `service/PostCommentService.java` |
| 实现 | `service/impl/SongCommentServiceImpl.java`, `service/impl/PostCommentServiceImpl.java` |
| 控制器 | `controller/SongController.java`（`/song/comment/add` 等）<br>`controller/PostController.java`（`/post/comment/add` 等） |

**核心设计 — parent_id 实现无限层级回复**：

```
歌曲《七里香》
  │
  ├── 💬 评论A (id=1, parent_id=0)  ← 一级评论
  │     ├── 💬 回复B (id=2, parent_id=1)  ← 回复 评论A
  │     └── 💬 回复C (id=3, parent_id=1)  ← 回复 评论A
  │
  ├── 💬 评论D (id=4, parent_id=0)  ← 一级评论
  │     └── 💬 回复E (id=5, parent_id=4)  ← 回复 评论D
  │           └── 💬 回复F (id=6, parent_id=5)  ← 回复 回复E（无限嵌套）
  │
  └── 💬 评论G (id=7, parent_id=0)  ← 一级评论
```

`parent_id` 的值决定层级关系：
- `parent_id = 0`：一级评论（直接对歌曲/帖子发表）
- `parent_id = N`：回复 id=N 的评论

**发表评论的代码逻辑**：

```java
public R addSongComment(CommentRequest request) {
    SongComment comment = new SongComment();
    comment.setSongId(request.getTargetId());   // 目标歌曲 ID
    comment.setUserId(request.getUserId());      // 谁发的
    comment.setContent(request.getContent());    // 评论内容
    comment.setParentId(                         // 层级判断
        request.getParentId() == null ? 0 : request.getParentId()
    );
    comment.setLikeCount(0);                     // 初始点赞数为 0
    comment.setCreateTime(new Date());
    commentMapper.insert(comment);
    return R.success("评论发布成功");
}
```

前后端配合方式：**后端返回一维列表，前端组装成树**。

后端从数据库查询出来的数据是平的（所有评论混在一起），通过 `parentId` 字段知道每条评论的"父评论是谁"。前端拿到后自己组装成树形结构（详见 5.3 节）。

**删除评论 — 权限校验**：

```java
public R deleteSongComment(Integer commentId, Integer loginUserId) {
    SongComment comment = getById(commentId);
    if (comment == null) return R.error("评论不存在");
    if (!comment.getUserId().equals(loginUserId)) {
        return R.error("仅可删除自己的评论");  // 只能删自己的
    }
    removeById(commentId);
    return R.success("删除评论成功");
}
```

### 4.4 评论点赞

**功能**：对任意评论（歌曲评论或帖子评论）点赞/取消点赞，点赞数实时更新，在个人主页可以查看所有点赞记录。

**涉及文件**：

| 层 | 文件 |
|----|------|
| 实体 | `model/domain/CommentLike.java` |
| Mapper | `mapper/CommentLikeMapper.java` |
| 请求 | `model/request/CommentLikeRequest.java` |
| 接口 | `service/CommentLikeService.java` |
| 实现 | `service/impl/CommentLikeServiceImpl.java` |
| 控制器 | `controller/SongController.java`（`/song/comment/like`）<br>`controller/PostController.java`（`/post/comment/like`）<br>`controller/CommentController.java`（`/comment/liked`） |

**核心逻辑**：

```
POST /song/comment/like
请求体：{ userId: 1, commentId: 10, commentType: "song" }
              │
              ▼
    ┌──────────────────────────────────────┐
    │ 查 comment_like 表：                   │
    │ userId=1 AND commentId=10 AND         │
    │ commentType="song" ?                  │
    └─────────┬────────────────────────────┘
              │
    ┌─────────┴──────────────┐
    │                        │
    ▼ 无记录                  ▼ 有记录
INSERT comment_like          DELETE comment_like
UPDATE song_comment          UPDATE song_comment
  like_count + 1               like_count - 1
    │                        │
    └──────────┬─────────────┘
               ▼
   返回 { isLiked: true/false }
```

**为什么需要 `comment_type` 字段**：歌曲评论和帖子评论存在两张不同的表里，但点赞记录共用一张 `comment_like` 表。如果只有 `comment_id`，会出现"歌曲评论 id=5"和"帖子评论 id=5"分不清的问题。`comment_type` 字段就是用来标注这条点赞是对"歌曲评论"还是"帖子评论"的。

**获取 isLiked 状态 — 批量查询**：加载评论列表时，需要告诉前端"当前用户是否已点赞每条评论"，这样才能正确显示实心★还是空心☆。

```java
// 实现方式：收集所有评论 ID，批量查点赞状态
1. 查出该歌曲所有评论 → [id=1, id=2, id=5, id=8, ...]
2. SELECT * FROM comment_like 
   WHERE user_id=当前用户 
   AND comment_type='song' 
   AND comment_id IN (1,2,5,8,...)     ← 一次查询搞定
3. 得到已点赞的评论 ID 集合 → {1, 5}
4. 为每条评论标记 isLiked：
   评论 id=1 → isLiked=true  (⭐)
   评论 id=2 → isLiked=false (☆)
   评论 id=5 → isLiked=true  (⭐)
   ...
```

**获取用户点赞记录**：个人主页"点赞"标签页的数据来源。这是一个比较复杂的多表关联查询：

```
GET /comment/liked?userId=1
              │
              ▼
1. SELECT * FROM comment_like WHERE user_id=1 ORDER BY create_time DESC
   → [{commentId:3, commentType:"song"}, {commentId:5, commentType:"post"}, ...]
              │
              ▼
2. 分成两组：
   歌曲评论 ID：[3, 7, 12]
   帖子评论 ID：[5, 9]
              │
              ▼
3. 批量查评论内容
   SELECT * FROM song_comment WHERE id IN (3,7,12)
   SELECT * FROM post_comment WHERE id IN (5,9)
              │
              ▼
4. 从评论中提取关联的歌曲ID和帖子ID
   → 批量查歌曲名：SELECT * FROM song WHERE id IN (...)
   → 批量查帖子标题：SELECT * FROM post WHERE id IN (...)
              │
              ▼
5. 批量查评论作者的 username
   SELECT * FROM user WHERE id IN (...)
              │
              ▼
6. 组装结果：
   [
     { commentType:"song", content:"太好听了", 
       sourceName:"七里香", sourceId:100, sourceType:"song", 
       commentUsername:"小明", likeTime:"2026-07-23 10:30" },
     { commentType:"post", content:"同意！",
       sourceName:"周杰伦新专辑讨论", sourceId:50, sourceType:"post",
       commentUsername:"小红", likeTime:"2026-07-23 09:15" },
     ...
   ]
```

---

## 五、互动功能 — 前端实现

### 5.1 API 请求层

前端使用 **Axios** 作为 HTTP 客户端，在 `utils/request.js` 中做了统一封装：

```javascript
// 创建 Axios 实例
const request = axios.create({
    baseURL: 'http://localhost:8081',   // 后端地址
    timeout: 500000,                     // 超时时间
    withCredentials: true                // 发送 Cookie
})

// 统一的 API 方法包装
export function api(options) {
    const method = options.method || "get"
    switch(method) {
        case "post":
            return request.post(options.url, options.data, options.config)
                          .then(res => res.data)
        case "get":
            return request.get(options.url, 
                          { ...options.config, params: options.params })
                          .then(res => res.data)
        // ... delete、put 同理
    }
}
```

响应拦截器统一处理错误：
- 401（未登录）→ 跳转到登录页
- 403（无权限）→ 跳转到首页
- 404（接口不存在）→ 打印日志

API 文件按业务模块分文件组织：

**`api/song.js`** — 歌曲相关的所有前端 API 封装：

| 函数 | 方法 | 后端路径 | 用途 |
|------|------|---------|------|
| `collectSong(data)` | POST | `/song/collect` | 收藏/取消收藏 |
| `getUserCollect(userId)` | GET | `/song/collect/list?userId=` | 获取用户收藏 |
| `addSongComment(data)` | POST | `/song/comment/add` | 发表评论 |
| `deleteSongComment(id, userId)` | DELETE | `/song/comment/delete` | 删除评论 |
| `getSongComments(id, userId)` | GET | `/song/comment/list` | 获取评论列表 |
| `likeSongComment(data)` | POST | `/song/comment/like` | 评论点赞 |

**`api/post.js`** — 帖子相关的所有前端 API 封装：

| 函数 | 方法 | 后端路径 | 用途 |
|------|------|---------|------|
| `likePost(data)` | POST | `/post/like` | 帖子点赞 |
| `addPostComment(data)` | POST | `/post/comment/add` | 发表评论 |
| `deletePostComment(id, userId)` | DELETE | `/post/comment/delete` | 删除评论 |
| `getPostComments(id, userId)` | GET | `/post/comment/list` | 获取评论列表 |
| `likePostComment(data)` | POST | `/post/comment/like` | 评论点赞 |

**`api/comment.js`** — 评论相关 API：

| 函数 | 方法 | 后端路径 | 用途 |
|------|------|---------|------|
| `getUserLikedComments(userId)` | GET | `/comment/liked?userId=` | 获取用户点赞记录 |

### 5.2 用户状态管理（Pinia Store）

```javascript
// store/user.js
export const useUserStore = defineStore("user", {
    state: () => ({
        userId: localStorage.getItem("userId") || "",
        username: localStorage.getItem("username") || "",
        avatar: localStorage.getItem("avatar") || "",
        token: localStorage.getItem("token") || "",
    }),
    getters: {
        isLogin: (state) => !!state.token  // 判断是否已登录
    },
    actions: {
        login(user) {
            // 登录成功后把用户信息存入 state 和 localStorage
            this.userId = user.id
            this.username = user.username
            localStorage.setItem("userId", user.id)
            // ...
        },
        logout() {
            // 退出时清空所有信息
            this.userId = ""
            localStorage.removeItem("userId")
            // ...
        }
    }
})
```

所有页面组件通过 `const userStore = useUserStore()` 获取当前用户信息，`userStore.userId` 就是当前登录用户 ID。

### 5.3 歌曲详情页 (SongDetail.vue) — 评论 + 收藏

这是最核心的互动页面。涉及 4 个互动功能：

#### 5.3.1 收藏歌曲

```javascript
async function toggleCollect() {
    if (!userId.value) { ElMessage.error('请先登录'); return }
    await collectSong({ userId: userId.value, songId: route.params.id })
    song.value.isCollected = !song.value.isCollected  // 乐观更新
    ElMessage.success(song.value.isCollected ? '已收藏' : '已取消收藏')
}
```

**乐观更新 (Optimistic UI)**：不等后端返回新数据，前端直接修改本地状态。因为后端是 Toggle 模式且一定会成功，所以这样做能让用户感觉响应极快（没有等待后端返回的延迟）。

#### 5.3.2 评论回复 — 树形组装

这是前端最核心的逻辑之一。后端返回的是一维列表，前端需要组装成树形结构：

```javascript
async function loadComments() {
    const res = await getSongComments(route.params.id, userId.value)
    const raw = res.data.data || []  // 后端返回的一维数组
    
    // 第一步：建立 id → 评论 的映射
    const map = {}
    raw.forEach(c => { c.replies = []; map[c.id] = c })
    
    // 第二步：将子评论挂到父评论下面
    const roots = []
    raw.forEach(c => {
        if (c.parentId && map[c.parentId]) {
            map[c.parentId].replies.push(c)  // 挂到父评论的 replies 数组
        } else {
            roots.push(c)  // 没有父评论的就是一级评论
        }
    })
    comments.value = roots
}
```

效果：一维数据 `[A, B, C, D, E]` 变成树形结构：
```
A
├── B (parentId=A.id)
└── C (parentId=A.id)
D
└── E (parentId=D.id)
```

#### 5.3.3 评论回复 — 提交

```javascript
async function submitReply(commentId) {
    const text = replyTexts.value[commentId].trim()
    if (!text) { ElMessage.error('回复内容不能为空'); return }
    
    await addSongComment({
        targetId: Number(route.params.id),  // 歌曲 ID
        userId: Number(userId.value),        // 当前用户
        content: text,                       // 回复内容
        parentId: commentId                  // 关键：parentId 标明这是回复哪条评论的
    })
    
    // 清理输入框，刷新评论列表
    replyTexts.value[commentId] = ''
    replyVisible.value[commentId] = false
    await loadComments()
}
```

#### 5.3.4 评论点赞 — 乐观更新

```javascript
async function toggleCommentLike(c) {
    await likeSongComment({ userId: Number(userId.value), commentId: c.id })
    // 乐观更新：直接修改本地数据
    if (c.isLiked) {
        c.isLiked = false
        c.likeCount = Math.max(0, c.likeCount - 1)
    } else {
        c.isLiked = true
        c.likeCount = c.likeCount + 1
    }
}
```

### 5.4 帖子详情页 (PostDetail.vue) — 点赞 + 评论

结构和 SongDetail.vue 高度一致，逻辑相同。差别在于接口前缀不同：
- 点赞用 `likePost()` → `POST /post/like`
- 评论点赞用 `likePostComment()` → `POST /post/comment/like`
- 评论用 `addPostComment()` / `getPostComments()`

```javascript
async function toggleLike() {
    await likePost({ userId: userId.value, postId: route.params.id })
    // 乐观更新
    if (!post.value.isLiked) {
        post.value.isLiked = true
        post.value.likeCount = post.value.likeCount + 1
    } else {
        post.value.isLiked = false
        post.value.likeCount = Math.max(0, post.value.likeCount - 1)
    }
}
```

### 5.5 个人主页 (UserPersonal.vue) — 一个页面三个标签

采用"一页三标签"的设计：

```
┌────────────────────────────────────────────┐
│  头像 + 用户名 + 返回首页 / 退出 / 账号设置  │
├────────────────────────────────────────────┤
│  [ 作品 ]  [ 收藏 ]  [ 点赞 ]              │  ← 标签切换
├────────────────────────────────────────────┤
│                                            │
│  <component :is="currentComponent" />     │  ← 动态组件渲染
│                                            │
└────────────────────────────────────────────┘
```

三个标签分别对应三个子组件：

| 标签 | 组件 | API 调用 | 展示内容 |
|------|------|---------|----------|
| 作品 | UserPost.vue | `getUserPosts()` | 用户发布的帖子 |
| 收藏 | UserFavorite.vue | `getUserCollect()` | 用户收藏的歌曲列表 |
| 点赞 | UserLike.vue | `getUserLikedComments()` | 用户点赞过的评论 |

通过 Vue 的 `<component :is="">` 动态组件机制实现标签切换：

```javascript
const currentComponent = computed(() => {
    switch (activeTab.value) {
        case "1": return UserPost       // 作品
        case "2": return UserFavorite   // 收藏
        case "3": return UserLike       // 点赞
    }
})
```

### 5.6 收藏列表页 (UserFavorite.vue)

展示用户收藏的所有歌曲，每首歌可以播放或取消收藏：

```javascript
async function cancelCollect(song) {
    // 确认弹窗
    await ElMessageBox.confirm("确定要取消收藏该歌曲吗？", "操作确认")
    // 调用 toggle API（收藏/取消收藏是同一个接口）
    await collectSong({ userId: userStore.userId, songId: song.songId })
    ElMessage.success("已取消收藏")
    loadFavorite()  // 重新加载列表
}
```

### 5.7 点赞记录页 (UserLike.vue)

展示用户点赞过的评论，可以点击跳转到来源：

```javascript
onMounted(async () => {
    const res = await getUserLikedComments(userStore.userId)
    likedItems.value = res?.data || []  // 拿到点赞记录列表
})

function goSource(item) {
    if (item.sourceType === 'song') {
        router.push(`/song/${item.sourceId}`)        // 跳转到歌曲详情
    } else if (item.sourceType === 'post') {
        router.push(`/post/detail/${item.sourceId}`)  // 跳转到帖子详情
    }
}
```

展示的内容包括：来源类型标签（歌曲/帖子）、来源名称、评论内容（斜体引用）、评论作者、点赞时间。

---

## 六、前后端数据流完整链路

以"用户在歌曲详情页给一条评论点赞"为例，展示前后端协作的完整流程：

```
┌─────────────────────────────────────────────────────────────┐
│  前端 (Vue 3)                                                │
│                                                             │
│  1. 用户点击 ⭐ 图标                                          │
│     ↓                                                       │
│  2. toggleCommentLike(c) 函数被调用                           │
│     ↓                                                       │
│  3. likeSongComment({ userId: 1, commentId: 10 })            │
│     → POST http://localhost:8081/song/comment/like           │
│     → Content-Type: application/json                        │
│     → Body: {"userId":1,"commentId":10,"commentType":"song"} │
│                                                             │
│  4. 不等回包，立即乐观更新本地 UI：                              │
│     c.isLiked = true                                        │
│     c.likeCount++                                           │
│     ⭐ 变实心，数字+1                                         │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP Request
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  后端 (Spring Boot)                                          │
│                                                             │
│  5. SongController.likeComment() 接收请求                     │
│     → 调用 commentLikeService.likeComment(request)            │
│                                                             │
│  6. CommentLikeServiceImpl.likeComment():                    │
│     a. 查 comment_like 表 → 无记录                            │
│     b. INSERT INTO comment_like                             │
│        (user_id, comment_id, comment_type, create_time)      │
│     c. UPDATE song_comment                                  │
│        SET like_count = like_count + 1                      │
│        WHERE id = 10                                        │
│     d. 返回 {"code":200, "success":true,                     │
│              "data":{"isLiked":true}}                        │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP Response
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  前端 (Vue 3)                                                │
│                                                             │
│  7. 收到回包，数据已一致（乐观更新猜对了）                       │
│     如果失败则回滚 isLiked 和 likeCount                        │
└─────────────────────────────────────────────────────────────┘

数据库最终状态：
  comment_like: 新增一行 { userId:1, commentId:10, commentType:"song" }
  song_comment: id=10 的 like_count 从 5 变成 6
```

**完整链路用时**：乐观更新下用户感知不到延迟（≈0ms），后端实际处理约 10-50ms。

---

## 七、API 接口全表

### 7.1 歌曲收藏

| 方法 | 路径 | 请求参数 | 返回 | 说明 |
|------|------|---------|------|------|
| POST | `/song/collect` | `{userId, songId}` (JSON) | R.success | 收藏/取消收藏 |
| GET | `/song/collect/list` | `?userId=` | R + 歌曲列表 | 获取用户收藏 |

### 7.2 帖子点赞

| 方法 | 路径 | 请求参数 | 返回 | 说明 |
|------|------|---------|------|------|
| POST | `/post/like` | `{userId, postId}` (JSON) | R.success | 点赞/取消点赞 |

### 7.3 歌曲评论

| 方法 | 路径 | 请求参数 | 返回 | 说明 |
|------|------|---------|------|------|
| POST | `/song/comment/add` | `{targetId, userId, content, parentId?}` | R.success | 发表评论/回复 |
| DELETE | `/song/comment/delete` | `?commentId=&userId=` | R.success | 删除评论 |
| GET | `/song/comment/list` | `?songId=&userId=` | R + 评论列表(带isLiked) | 获取评论 |
| POST | `/song/comment/like` | `{userId, commentId, commentType:"song"}` | R + `{isLiked}` | 点赞评论 |

### 7.4 帖子评论

| 方法 | 路径 | 请求参数 | 返回 | 说明 |
|------|------|---------|------|------|
| POST | `/post/comment/add` | `{targetId, userId, content, parentId?}` | R.success | 发表评论/回复 |
| DELETE | `/post/comment/delete` | `?commentId=&userId=` | R.success | 删除评论 |
| GET | `/post/comment/list` | `?postId=&userId=` | R + 评论列表(带isLiked) | 获取评论 |
| POST | `/post/comment/like` | `{userId, commentId, commentType:"post"}` | R + `{isLiked}` | 点赞评论 |

### 7.5 评论点赞记录

| 方法 | 路径 | 请求参数 | 返回 | 说明 |
|------|------|---------|------|------|
| GET | `/comment/liked` | `?userId=` | R + 点赞记录列表 | 用户点赞过的评论 |

---

## 八、关键技术要点总结

### 8.1 Toggle 模式（切换模式）

收藏、帖子点赞、评论点赞 —— 三个功能全部采用同一个模式：
- **同一接口同时处理点赞和取消点赞**
- 查记录 → 有则删（取消），无则增（点赞）
- 前端不需要判断当前状态，直接调用同一个 API

### 8.2 乐观更新 (Optimistic UI)

前端在发送完请求后，**不等待后端回包就直接更新本地 UI**。因为 Toggle 模式下后端一定成功，所以用户感知到的响应时间为 0ms。

```javascript
// 不等待后端返回，直接改
c.isLiked = !c.isLiked
c.likeCount += c.isLiked ? 1 : -1
```

### 8.3 冗余计数字段

`like_count`、`collect_count` 等字段存在表中而不是实时计算，大幅减少查询次数：
- 展示列表时不需要 JOIN 或子查询
- 代价是点赞时需要在事务中同步更新

### 8.4 批量查询优化（避免 N+1 问题）

所有涉及列表展示的接口，都使用"收集 ID → IN 查询"的批量模式：

```
❌ 循环查：for each → SELECT ... WHERE id = ?  （N+1 次查询）
✅ 批量查：collect IDs → SELECT ... WHERE id IN (?,?,?...) （2 次查询）
```

### 8.5 事务控制 (@Transactional)

所有涉及多表操作的方法都添加了事务注解：

| 方法 | 涉及的表 |
|------|---------|
| likePost | post_support + post（两张表） |
| likeComment | comment_like + song_comment/post_comment（两张表） |

`@Transactional` 确保：要么所有操作全部成功，要么全部回滚，不会出现"点赞记录插入了但计数没更新"这种半成品状态。

### 8.6 parent_id 实现评论嵌套

单表 + `parent_id` 字段实现无限层级的评论回复：
- 后端：存一条记录，`parentId=0` 是一级评论，`parentId>0` 是回复
- 前端：收到一维列表后，通过 `parentId` 组装成树形结构展示

### 8.7 comment_type 字段区分来源

`comment_like` 表用一个 `comment_type` 字段（值为 `"song"` 或 `"post"`）同时服务歌曲评论和帖子评论两种场景，避免建两张几乎一样的表。

---

## 项目文件索引

### 后端核心文件

```
music-new/src/main/java/com/music/
├── MusicApplication.java              # 启动类
├── common/R.java                      # 统一响应格式
├── config/
│   ├── MybatisPlusConfig.java         # 分页插件配置
│   ├── CorsConfig.java                # 跨域配置
│   └── FileUploadConfig.java          # 文件上传工具
├── controller/
│   ├── SongController.java            # 歌曲控制器（收藏+评论+评论点赞）
│   ├── PostController.java            # 帖子控制器（点赞+评论+评论点赞）
│   ├── CommentController.java         # 评论控制器（用户点赞记录）
│   └── CollectionController.java      # 收藏管理控制器
├── mapper/
│   ├── CollectMapper.java
│   ├── PostSupportMapper.java
│   ├── SongCommentMapper.java
│   ├── PostCommentMapper.java
│   └── CommentLikeMapper.java
├── model/
│   ├── domain/                        # 11 个数据库实体类
│   │   ├── Collect.java
│   │   ├── PostSupport.java
│   │   ├── SongComment.java
│   │   ├── PostComment.java
│   │   └── CommentLike.java
│   └── request/                       # 请求参数对象
│       ├── CollectRequest.java
│       ├── PostLikeRequest.java
│       ├── CommentRequest.java
│       └── CommentLikeRequest.java
└── service/
    ├── CollectService.java
    ├── PostSupportService.java
    ├── SongCommentService.java
    ├── PostCommentService.java
    ├── CommentLikeService.java
    └── impl/                          # 上述接口的实现类
        ├── CollectServiceImpl.java
        ├── PostSupportServiceImpl.java
        ├── SongCommentServiceImpl.java
        ├── PostCommentServiceImpl.java
        └── CommentLikeServiceImpl.java
```

### 前端核心文件

```
music_frontend/src/
├── api/
│   ├── song.js          # 歌曲 API（收藏+评论+点赞）
│   ├── post.js          # 帖子 API（点赞+评论+点赞）
│   └── comment.js       # 评论 API（点赞记录）
├── store/
│   └── user.js          # Pinia 用户状态管理
├── utils/
│   ├── request.js       # Axios 封装（统一请求/响应处理）
│   └── index.js         # 图片 URL 工具函数
└── pages/
    ├── song/
    │   └── SongDetail.vue    # 歌曲详情（收藏+评论+点赞+回复）
    ├── post/
    │   └── PostDetail.vue    # 帖子详情（点赞+评论+点赞+回复）
    └── user/
        ├── UserPersonal.vue  # 个人主页（作品/收藏/点赞三标签）
        ├── UserFavorite.vue  # 收藏列表
        └── UserLike.vue      # 点赞记录列表
```

---

> **文档版本**：v2.0  
> **生成日期**：2026-07-23  
> **项目**：音乐平台 (music-new + music_frontend)
