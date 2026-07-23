# 音乐平台后端 — 数据库持久层与互动功能技术报告

> **负责人职责**：数据库及 MyBatis-Plus 持久层建设，歌曲收藏、帖子点赞、歌曲与帖子评论回复等互动功能  
> **技术栈**：Java 17 + Spring Boot 2.6.2 + MyBatis-Plus 3.5.1 + MySQL 8.0 + Redis  
> **项目名称**：music (music-new)

---

## 一、整体架构说明

本项目采用标准的 **Controller → Service → Mapper** 三层架构。

```
┌──────────────────────────────────────────────┐
│  浏览器 / Vue 前端                            │
│  发送 HTTP 请求（GET/POST/DELETE）             │
└──────────────────┬───────────────────────────┘
                   │ JSON / FormData
                   ▼
┌──────────────────────────────────────────────┐
│  Controller 层（控制器）                       │
│  职责：接收请求参数，调用 Service，返回结果      │
│  注解：@RestController + @RequestMapping      │
│  例如：SongController、PostController 等       │
└──────────────────┬───────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────┐
│  Service 层（业务逻辑）                        │
│  职责：处理业务规则、数据校验、事务控制          │
│  注解：@Service                               │
│  例如：CollectServiceImpl、CommentLikeService  │
└──────────────────┬───────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────┐
│  Mapper 层（数据访问）                         │
│  职责：与数据库交互（增删改查）                 │
│  基于 MyBatis-Plus 的 BaseMapper<T>           │
│  例如：CollectMapper、PostSupportMapper 等     │
└──────────────────┬───────────────────────────┘
                   │ SQL
                   ▼
┌──────────────────────────────────────────────┐
│  MySQL 数据库（musictest）                     │
│  8 张业务表，utf8mb4 字符集                    │
└──────────────────────────────────────────────┘
```

**关键概念解释**：

| 术语 | 通俗解释 |
|------|----------|
| **Controller** | 就像餐厅的服务员，接收客人的点单（HTTP请求），转达给后厨（Service），再把菜端回来（返回响应） |
| **Service** | 就像后厨的大厨，负责真正的"烹饪"——处理业务逻辑，判断该做什么、怎么做 |
| **Mapper** | 就像仓库管理员，大厨说要什么食材，他就去仓库（数据库）取，或者把新食材存进去 |
| **MyBatis-Plus** | 一个"自动仓库管理员"框架，大多数增删改查不需要手写SQL，框架自动生成 |
| **Spring Boot** | 一个"一键启动"的Java框架，把服务器、数据库连接等所有组件自动装配好 |

---

## 二、数据库设计（8 张业务表）

数据库名为 `musictest`，使用 utf8mb4 字符集（支持 emoji 和所有 Unicode 字符）。

### 2.1 用户表 — `user`

存储所有用户的基本信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 用户唯一编号 |
| username | VARCHAR(64) | 用户名 |
| password | VARCHAR(255) | 加密后的密码（BCrypt） |
| sex | VARCHAR(10) | 性别 |
| phone_num | VARCHAR(20) | 手机号 |
| email | VARCHAR(128) | 电子邮箱 |
| birth | DATE | 出生日期 |
| introduction | VARCHAR(512) | 个人简介 |
| avatar | VARCHAR(255) | 头像图片路径 |
| create_time | DATETIME | 注册时间 |
| update_time | DATETIME | 最后更新时间 |

### 2.2 歌曲表 — `song`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 歌曲编号 |
| user_id | INT | 上传者ID |
| name | VARCHAR(128) | 歌曲名称 |
| introduction | VARCHAR(512) | 歌曲简介/演唱者 |
| singer_id | INT | 关联的歌手ID |
| status | INT | 审核状态：0=待审核, 1=已通过, 2=已驳回 |
| audit_reason | VARCHAR(255) | 审核驳回原因 |
| play_count | INT | 播放次数 |
| collect_count | INT | 被收藏次数（冗余字段，加速查询） |
| like_count | INT | 被点赞次数（冗余字段） |
| pic | VARCHAR(255) | 封面图片路径 |
| lyric | TEXT | 歌词内容 |
| url | VARCHAR(255) | 音频文件路径 |
| create_time / update_time | DATETIME | 创建/更新时间 |

**设计要点**：`collect_count` 和 `like_count` 是**冗余字段**——也就是说，这些数字实际上是由收藏表和点赞表算出来的，但我们把它直接存在歌曲表里。这样每次展示歌曲时不用去其他表里数一遍，大大加快查询速度。每次有人收藏/取消收藏时，同步更新这个数字即可。

### 2.3 帖子表 — `post`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 帖子编号 |
| user_id | INT | 发帖用户ID |
| title | VARCHAR(255) | 帖子标题 |
| content | TEXT | 帖子正文 |
| cover | VARCHAR(255) | 封面图片路径 |
| status | INT | 审核状态 |
| like_count | INT | 被点赞次数（冗余字段） |
| comment_count | INT | 评论总数（冗余字段） |
| create_time / update_time | DATETIME | 创建/更新时间 |

### 2.4 收藏表 — `collect`

记录"哪个用户收藏了哪首歌"。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 记录编号 |
| user_id | INT | 用户ID |
| song_id | INT | 歌曲ID |
| create_time | DATETIME | 收藏时间 |

**设计要点**：这是一张"中间表"（也叫关联表 / 桥表），它的唯一作用就是记录"用户-歌曲"之间的收藏关系。一行记录 = 一次收藏。如果用户取消收藏，就删除这一行。

### 2.5 帖子点赞表 — `post_support`

记录"哪个用户点赞了哪个帖子"。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 记录编号 |
| user_id | INT | 用户ID |
| post_id | INT | 帖子ID |
| create_time | DATETIME | 点赞时间 |

### 2.6 歌曲评论表 — `song_comment`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 评论编号 |
| song_id | INT | 所属歌曲ID |
| user_id | INT | 评论者ID |
| content | TEXT | 评论内容 |
| parent_id | INT | 父评论ID（0=一级评论, >0=回复某条评论） |
| like_count | INT | 被点赞次数（冗余字段） |
| create_time | DATETIME | 评论时间 |

**关键设计 — parent_id**：这个字段是实现"评论回复"功能的核心。当 `parent_id = 0` 时，这是一条**一级评论**（直接评论歌曲）；当 `parent_id = 某个评论的id` 时，这是一条**回复**（回复别人的评论）。这样就能实现类似贴吧的楼中楼效果。

### 2.7 帖子评论表 — `post_comment`

结构与 `song_comment` 完全对称，只是把 `song_id` 换成 `post_id`。

### 2.8 评论点赞表 — `comment_like`

记录"哪个用户点赞了哪条评论"。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT (主键, 自增) | 记录编号 |
| user_id | INT | 用户ID |
| comment_id | INT | 评论ID |
| comment_type | VARCHAR(10) | 区分来源：`song` 或 `post` |
| create_time | DATETIME | 点赞时间 |

**唯一约束**：`(user_id, comment_id, comment_type)` 三者组合唯一——同一个人对同一条评论只能点赞一次，不能重复点赞。

**设计要点**：`comment_type` 字段让一张表同时服务于歌曲评论和帖子评论两种场景，避免了建两张几乎一样的表。

### 数据库表关系图

```
user ──┬── song ──┬── collect (用户收藏歌曲)
       │          ├── song_comment (歌曲评论)
       │          │     └── comment_like (评论点赞)
       │          └── singer (歌手)
       │
       ├── post ──┬── post_support (用户点赞帖子)
       │          ├── post_comment (帖子评论)
       │          │     └── comment_like (评论点赞)
       │          └── *审核管理*
       │
       └── *登录认证*
```

---

## 三、MyBatis-Plus 持久层建设

### 3.1 什么是 MyBatis-Plus

MyBatis-Plus 是 MyBatis 的增强工具。**简单理解**：传统方式操作数据库需要手写大量的 SQL 语句，MyBatis-Plus 把这个过程自动化了——它会根据你的 Java 类自动生成对应的 SQL，你只需要调用几个简单的方法名就能完成增删改查。

### 3.2 具体实现方式

**第一步：定义实体类（Domain）**

以 `Collect.java` 为例：

```java
@Data                              // Lombok注解：自动生成getter/setter/toString
@TableName("collect")              // 告诉框架：这个类对应数据库的 collect 表
public class Collect {
    @TableId(type = IdType.AUTO)   // 主键，数据库自增
    private Integer id;
    private Integer userId;        // 会自动映射到 user_id 列（驼峰转下划线）
    private Integer songId;        // 自动映射到 song_id
    private Date createTime;       // 自动映射到 create_time
}
```

每一张数据库表都对应一个这样的 Java 类，共 11 个实体类：

| 实体类 | 对应表 | 功能 |
|--------|--------|------|
| User | user | 用户账户 |
| Song | song | 歌曲信息 |
| Post | post | 社区帖子 |
| Collect | collect | 歌曲收藏关系 |
| PostSupport | post_support | 帖子点赞关系 |
| SongComment | song_comment | 歌曲评论 |
| PostComment | post_comment | 帖子评论 |
| CommentLike | comment_like | 评论点赞关系 |
| Singer | singer | 歌手信息 |
| Banner | banner | 首页轮播图 |
| SongSheet | song_sheet | 歌单 |

**第二步：定义 Mapper 接口**

每一个实体类都配一个 Mapper 接口，例如：

```java
@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
    // 完全不需要写任何方法！BaseMapper 已经提供了所有基础操作
}
```

`BaseMapper<Collect>` 自动提供了以下方法（不用手写一行 SQL）：

| 方法 | 作用 |
|------|------|
| `selectById(id)` | 根据ID查一条记录 |
| `selectList(wrapper)` | 根据条件查多条记录 |
| `selectPage(page, wrapper)` | 分页查询 |
| `insert(entity)` | 插入一条记录 |
| `updateById(entity)` | 根据ID更新 |
| `deleteById(id)` | 根据ID删除 |
| `count(wrapper)` | 统计数量 |

**第三步：在 Service 层使用 QueryWrapper 构建查询条件**

```java
// 不使用 MyBatis-Plus 的传统写法（需要手写 SQL）：
// SELECT * FROM collect WHERE user_id = 1 ORDER BY create_time DESC

// MyBatis-Plus 的写法（纯 Java 代码，不需要 SQL）：
QueryWrapper<Collect> wrapper = new QueryWrapper<>();
wrapper.eq("user_id", userId)          // eq = equals，即 WHERE user_id = ?
       .orderByDesc("create_time");    // ORDER BY create_time DESC
List<Collect> list = collectMapper.selectList(wrapper);
```

`QueryWrapper` 就像一个"查询条件构造器"，你用 Java 方法链式调用，它自动帮你翻译成 SQL。常用方法：

| 方法 | SQL 等价 |
|------|----------|
| `eq("user_id", 1)` | `WHERE user_id = 1` |
| `in("id", list)` | `WHERE id IN (1,2,3)` |
| `orderByDesc("create_time")` | `ORDER BY create_time DESC` |
| `like("name", "周")` | `WHERE name LIKE '%周%'` |

### 3.3 分页配置

在 `MybatisPlusConfig.java` 中注册了分页插件：

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

之后在任何 Service 中，只需要：

```java
Page<Collect> page = new Page<>(pageNum, pageSize);  // 第几页，每页几条
collectMapper.selectPage(page, wrapper);
```

框架就会自动在 SQL 末尾加上 `LIMIT 0, 10`（MySQL 分页语法），并返回总条数、总页数等完整分页信息。

### 3.4 文件上传机制

项目通过 `FileUploadConfig.java` 统一管理文件上传：

```java
public String upload(MultipartFile file, String folder) {
    // 1. 生成 UUID 作为新文件名（避免重名冲突）
    String newFileName = UUID.randomUUID() + ".jpg";
    // 2. 创建目标目录 C:\musicSystem\{folder}\
    File dir = new File("C:\\musicSystem\\" + folder);
    if (!dir.exists()) dir.mkdirs();
    // 3. 保存文件
    file.transferTo(new File(dir, newFileName));
    // 4. 返回可访问的 URL 路径
    return "/" + folder + "/" + newFileName;
}
```

上传后的文件通过 `WebMvcConfig` 映射为 HTTP 可访问地址。例如图片存到 `C:\musicSystem\singerPic\abc.jpg`，前端通过 `http://localhost:8081/singerPic/abc.jpg` 就能访问。

---

## 四、互动功能实现详解

### 4.1 歌曲收藏功能

**功能描述**：用户可以在歌曲详情页点击"收藏"按钮收藏歌曲，再次点击取消收藏。个人主页的"收藏"标签页展示所有已收藏的歌曲。

**涉及文件**：

| 文件 | 作用 |
|------|------|
| `model/domain/Collect.java` | 收藏实体类，对应 collect 表 |
| `mapper/CollectMapper.java` | 数据访问层 |
| `service/CollectService.java` | 业务接口 |
| `service/impl/CollectServiceImpl.java` | 业务实现 |
| `controller/CollectionController.java` | 控制器 |
| `model/request/CollectRequest.java` | 请求参数对象 |

**核心逻辑（CollectServiceImpl.collectSong）**：

```
用户点击"收藏"
  │
  ▼
接收参数：{ userId: 1, songId: 100 }
  │
  ▼
查 collect 表：是否存在 userId=1 且 songId=100 的记录？
  │
  ├── 不存在 ──→ 插入新记录（执行 INSERT）
  │              └── 收藏成功 ✅
  │
  └── 已存在 ──→ 删除该记录（执行 DELETE）
                 └── 取消收藏 ✅
```

这就是 **Toggle（切换）模式**——同一个按钮，点一下收藏，再点一下取消。这是最常见的交互设计。

**关键代码逻辑**：

```java
@Transactional  // 事务注解：保证数据一致性
public R collectSong(CollectRequest request) {
    // 1. 查是否已收藏
    QueryWrapper<Collect> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id", request.getUserId())
           .eq("song_id", request.getSongId());
    Collect record = getOne(wrapper);

    if (record == null) {
        // 2a. 没收藏过 → 新增收藏
        Collect collect = new Collect();
        collect.setUserId(request.getUserId());
        collect.setSongId(request.getSongId());
        collect.setCreateTime(new Date());
        save(collect);
    } else {
        // 2b. 已收藏 → 删除记录（取消收藏）
        remove(wrapper);
    }
    return R.success("操作成功");
}
```

`@Transactional` 注解的含义：这个方法中的所有数据库操作要么全部成功，要么全部失败回滚。如果中间出错了，不会出现"插入了收藏但没更新计数"这种半成品状态。

**获取收藏列表（CollectServiceImpl.getUserCollect）**：

这里做了一个重要的性能优化——**批量查询**：

```
❌ 不好的做法（N+1 问题）：
   for (Collect c : collectList) {
       Song song = songMapper.selectById(c.getSongId());  // 每循环一次查一次数据库
       // 如果收藏了50首歌，就要查51次数据库！
   }

✅ 好的做法（批量查询）：
   1. 先查出所有收藏记录
   2. 收集所有 songId，组成一个列表 [1, 5, 10, 20, ...]
   3. 用 IN 查询一次性取出所有歌曲：SELECT * FROM song WHERE id IN (1,5,10,20,...)
   4. 在 Java 代码中做匹配拼接
   // 总共只查 2 次数据库！
```

### 4.2 帖子点赞功能

**功能描述**：用户在帖子详情页点击星星图标，点赞/取消点赞帖子。帖子的点赞数实时更新。

**涉及文件**：

| 文件 | 作用 |
|------|------|
| `model/domain/PostSupport.java` | 点赞实体类 |
| `mapper/PostSupportMapper.java` | 数据访问层 |
| `service/PostSupportService.java` | 业务接口 |
| `service/impl/PostSupportServiceImpl.java` | 业务实现 |
| `controller/PostController.java` | 控制器（点赞接口在这里） |
| `model/request/PostLikeRequest.java` | 请求参数对象 |

**核心逻辑（PostSupportServiceImpl.likePost）**：

```
用户点击点赞 ⭐
  │
  ▼
接收参数：{ userId: 1, postId: 50 }
  │
  ▼
查 post_support 表：有没有 userId=1 且 postId=50 的记录？
  │
  ├── 不存在 ──→ ① INSERT 到 post_support
  │              ② UPDATE post SET like_count = like_count + 1
  │              └── 点赞成功 ✅
  │
  └── 已存在 ──→ ① DELETE 从 post_support
                 ② UPDATE post SET like_count = like_count - 1
                 └── 取消点赞 ✅
```

和收藏的区别：点赞成功后，不仅要在 `post_support` 表新增记录，还要**同步更新 `post` 表的 `like_count` 字段 +1**。这个冗余字段让前端展示点赞数时不需要去 `post_support` 表 count，直接读 `post.like_count` 就行，速度快很多。

```java
@Transactional  // 两个数据库操作必须同时成功或同时失败
public R likePost(PostLikeRequest request) {
    // 查是否已点赞
    PostSupport record = getOne(wrapper);
    Post post = postMapper.selectById(postId);
    
    if (record == null) {
        // 点赞：新增记录 + 更新帖子 like_count +1
        save(newSupport);
        post.setLikeCount(post.getLikeCount() + 1);
    } else {
        // 取消：删除记录 + 更新帖子 like_count -1
        remove(wrapper);
        post.setLikeCount(post.getLikeCount() - 1);
    }
    postMapper.updateById(post);  // 同步更新帖子表的冗余计数字段
    return R.success("操作成功");
}
```

### 4.3 评论回复功能

**功能描述**：用户可以对歌曲或帖子发表评论，也可以回复别人的评论，形成嵌套的楼中楼结构。

**涉及文件**：

| 文件 | 作用 |
|------|------|
| `model/domain/SongComment.java` | 歌曲评论实体 |
| `model/domain/PostComment.java` | 帖子评论实体 |
| `service/SongCommentService.java` | 歌曲评论接口 |
| `service/impl/SongCommentServiceImpl.java` | 歌曲评论实现 |
| `service/PostCommentService.java` | 帖子评论接口 |
| `service/impl/PostCommentServiceImpl.java` | 帖子评论实现 |
| `controller/SongController.java` | 歌曲控制器（含评论接口） |
| `controller/PostController.java` | 帖子控制器（含评论接口） |
| `model/request/CommentRequest.java` | 评论请求参数 |

**核心设计 — parent_id 机制**：

```
歌曲《七里香》
  │
  ├── 评论A (id=1, parent_id=0) ← 一级评论（直接评论歌曲）
  │     ├── 回复B (id=2, parent_id=1) ← 回复评论A
  │     └── 回复C (id=3, parent_id=1) ← 回复评论A
  │
  └── 评论D (id=4, parent_id=0) ← 一级评论
        └── 回复E (id=5, parent_id=4) ← 回复评论D
```

`parent_id` 的值决定了层级关系：
- `parent_id = 0`：这是一级评论（直接对歌曲/帖子发表）
- `parent_id = 某条评论的id`：这是对那条评论的回复

一条 CommentRequest 既可以发一级评论，也可以发回复——区别就在于传不传 `parentId`：

```java
// 发一级评论
{ targetId: 100, userId: 1, content: "这首歌真好听！" }
// parentId 不传，默认为 0

// 回复别人的评论
{ targetId: 100, userId: 2, content: "同意！", parentId: 5 }
// parentId = 5，表示这是对 id=5 的评论的回复
```

**前端如何展示嵌套结构（树形组装）**：

后端返回的是一维列表（所有评论混在一起），前端通过 `parent_id` 组装成树形结构：

```javascript
// 1. 先建一个 id → 评论对象 的映射表
const map = {}
raw.forEach(c => { c.replies = []; map[c.id] = c })

// 2. 遍历，把子评论挂到父评论下面
const roots = []
raw.forEach(c => {
    if (c.parentId && map[c.parentId]) {
        map[c.parentId].replies.push(c)  // 挂到父评论的 replies 数组
    } else {
        roots.push(c)  // 没有父评论的，就是一级评论
    }
})
```

**批量查询用户信息避免 N+1 问题**：

```
❌ 差的做法：每条评论单独查用户
   for (Comment c : comments) {
       User u = userMapper.selectById(c.getUserId());  // N次查询
   }

✅ 好的做法：收集所有 userId，一次性批量查询
   1. 收集评论中的所有 userId → [1, 2, 5, 7, ...]
   2. SELECT * FROM user WHERE id IN (1, 2, 5, 7, ...)  // 1次查询
   3. 在内存中按 id 匹配
```

对应代码（SongCommentServiceImpl）：

```java
// 收集所有评论者的 userId
Set<Integer> userIds = comments.stream()
    .map(SongComment::getUserId)
    .collect(Collectors.toSet());

// 一次性查出所有用户
List<User> users = userMapper.selectList(
    new QueryWrapper<User>().in("id", userIds)
);

// 建立 id→User 的映射，后续直接用 map.get() 获取
Map<Integer, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, u -> u));
```

### 4.4 评论点赞功能

**功能描述**：用户可以对任意评论（歌曲评论或帖子评论）进行点赞/取消点赞，点赞数实时更新。在个人主页可以查看自己点赞过的所有评论。

**涉及文件**：

| 文件 | 作用 |
|------|------|
| `model/domain/CommentLike.java` | 评论点赞实体 |
| `mapper/CommentLikeMapper.java` | 数据访问层 |
| `service/CommentLikeService.java` | 业务接口 |
| `service/impl/CommentLikeServiceImpl.java` | 业务实现 |
| `controller/CommentController.java` | 控制器 |
| `model/request/CommentLikeRequest.java` | 请求参数 |

**核心逻辑（CommentLikeServiceImpl.likeComment）**：

```
用户点击评论的 ⭐ 点赞按钮
  │
  ▼
接收参数：{ userId: 1, commentId: 10, commentType: "song" }
  │
  ▼
查 comment_like 表：userId=1 且 commentId=10 且 commentType="song" ？
  │
  ├── 不存在 ──→ ① INSERT 到 comment_like
  │              ② 更新 song_comment 的 like_count +1
  │              └── 点赞成功 ✅  (返回 isLiked: true)
  │
  └── 已存在 ──→ ① DELETE 从 comment_like
                 ② 更新 song_comment 的 like_count -1
                 └── 取消点赞 ✅  (返回 isLiked: false)
```

**为什么要用 comment_type 字段**：

因为歌曲评论和帖子评论都存在 `comment_like` 这张表里。如果只有 `comment_id`，可能会出现：
- 歌曲评论 id=5 被点赞
- 帖子评论 id=5 被点赞

两个 id=5 是不同的评论（一张在 song_comment 表，一张在 post_comment 表），`comment_type` 字段就是用来区分它们的。

**数据库唯一约束保护**：`UNIQUE (user_id, comment_id, comment_type)` 确保同一个人对同一条评论只能有一条点赞记录，从数据库层面杜绝了重复点赞的可能。

**查询用户点赞过的评论（CommentLikeServiceImpl.getUserLikedComments）**：

这是一个比较复杂的多表关联查询，用于个人主页"点赞"标签页：

```
1. 查 comment_like 表：userId=1 的所有点赞记录，按时间倒序
2. 分离成两组：
   - 歌曲评论ID列表 [3, 7, 12]
   - 帖子评论ID列表 [5, 9]
3. 批量查询对应的评论内容
   - SELECT * FROM song_comment WHERE id IN (3,7,12)
   - SELECT * FROM post_comment WHERE id IN (5,9)
4. 从评论中提取关联的歌曲ID和帖子ID，批量查询歌曲名和帖子标题
   - SELECT * FROM song WHERE id IN (...)
   - SELECT * FROM post WHERE id IN (...)
5. 获取所有评论作者的 username
6. 组装成最终结果：
   [
     { commentType: "song", content: "这首歌真好听", sourceName: "七里香", likeTime: ... },
     { commentType: "post", content: "同意楼上", sourceName: "周杰伦新专辑讨论", likeTime: ... },
     ...
   ]
```

**判断用户是否已点赞某条评论（isLiked）**：

在加载歌曲/帖子评论列表时，需要告诉前端"当前用户是否已经点赞了每条评论"，这样才能正确显示⭐实心还是空心。实现方式：

```java
// 1. 先查出该歌曲的所有评论
List<SongComment> comments = ...;
List<Integer> commentIds = comments.stream().map(c->c.getId()).toList();

// 2. 批量查当前用户对这些评论的点赞状态
QueryWrapper<CommentLike> likeWrapper = new QueryWrapper<>();
likeWrapper.eq("user_id", userId)
          .eq("comment_type", "song")
          .in("comment_id", commentIds);  // IN (1,2,3,5,8,...)
List<CommentLike> likes = commentLikeMapper.selectList(likeWrapper);

// 3. 拿到已点赞的评论ID集合
Set<Integer> likedIds = likes.stream().map(CommentLike::getCommentId).collect(toSet());

// 4. 每条评论标记 isLiked
for (SongComment c : comments) {
    map.put("isLiked", likedIds.contains(c.getId()));  // true 或 false
}
```

---

## 五、API 接口汇总

### 5.1 收藏相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/song/collect` | 收藏/取消收藏歌曲 |
| GET | `/song/collect/list?userId=` | 获取用户收藏列表 |
| GET | `/collection` | (管理员) 所有收藏记录 |
| GET | `/collection/page?page=&size=` | (管理员) 分页收藏记录 |
| DELETE | `/collection/delete?id=` | (管理员) 删除收藏记录 |

### 5.2 帖子点赞相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/post/like` | 点赞/取消点赞帖子 |

### 5.3 评论相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/song/comment/add` | 发表歌曲评论/回复 |
| DELETE | `/song/comment/delete?commentId=&userId=` | 删除歌曲评论 |
| GET | `/song/comment/list?songId=&userId=` | 获取歌曲评论列表（带isLiked） |
| POST | `/post/comment/add` | 发表帖子评论/回复 |
| DELETE | `/post/comment/delete?commentId=&userId=` | 删除帖子评论 |
| GET | `/post/comment/list?postId=&userId=` | 获取帖子评论列表（带isLiked） |

### 5.4 评论点赞相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/song/comment/like` | 点赞/取消点赞歌曲评论 |
| POST | `/post/comment/like` | 点赞/取消点赞帖子评论 |
| GET | `/comment/liked?userId=` | 获取用户点赞过的所有评论 |

### 5.5 管理员评论管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/comment` | 所有评论 |
| GET | `/comment/page` | 分页评论 |
| DELETE | `/comment/delete?id=&type=` | 删除评论 |

---

## 六、关键技术要点总结

### 6.1 Toggle 模式（切换模式）

收藏、帖子点赞、评论点赞都采用了同样的 Toggle 模式：
- **同一接口同时处理点赞和取消点赞**
- 查记录 → 有则删（取消），无则增（点赞）
- 这个模式简化了前端逻辑——不需要判断当前状态，调同一个接口就行

### 6.2 冗余计数字段

`song.collect_count`、`post.like_count`、`song_comment.like_count` 等字段是冗余的——它们的值可以从关联表中 count 出来。但存储冗余字段的好处是：
- 展示列表时**不需要 JOIN 或子查询**，直接读一个 int
- 数据库查询次数从 N+1 变成 1

代价是每次点赞/收藏时需要**同步更新**这个字段，这个操作必须在 `@Transactional` 事务中完成，保证一致性。

### 6.3 批量查询优化

所有涉及列表展示的接口，都使用"收集 ID → IN 查询"的批量模式，避免循环查数据库：

```
❌ for each item: SELECT FROM other_table WHERE id = ?
✅ collect all IDs → SELECT FROM other_table WHERE id IN (?,?,?,...)
```

### 6.4 事务控制

所有涉及多表操作的方法都使用 `@Transactional`：

| 方法 | 涉及操作 | 为什么需要事务 |
|------|----------|----------------|
| collectSong | INSERT/DELETE collect | 单表但需要原子性 |
| likePost | INSERT/DELETE post_support + UPDATE post.like_count | 两张表必须同步 |
| likeComment | INSERT/DELETE comment_like + UPDATE comment.like_count | 两张表必须同步 |

### 6.5 评论的 parent_id 设计

使用单表 + `parent_id` 字段实现无限层级嵌套，不需要额外的"回复表"。前端拿到一维数据后通过 `parentId` 组装成树形结构展示。

---

## 七、项目文件结构

```
music-new/
├── pom.xml                          # Maven 依赖配置
├── init.sql                         # 数据库建表脚本
└── src/main/java/com/music/
    ├── MusicApplication.java        # Spring Boot 启动类
    ├── common/
    │   └── R.java                   # 统一响应包装类
    ├── config/
    │   ├── CorsConfig.java          # 跨域配置
    │   ├── FileConfig.java          # 文件路径配置
    │   ├── FileUploadConfig.java    # 文件上传工具
    │   ├── MybatisPlusConfig.java   # MyBatis-Plus 分页插件
    │   ├── SecurityConfig.java      # BCrypt 密码编码器
    │   └── WebMvcConfig.java        # 静态资源映射
    ├── controller/
    │   ├── CollectionController.java  # 收藏控制器
    │   ├── CommentController.java     # 评论控制器（含评论点赞）
    │   ├── PostController.java        # 帖子控制器（含点赞+评论）
    │   ├── SongController.java        # 歌曲控制器（含收藏+评论+评论点赞）
    │   └── ...                        # 其他控制器
    ├── mapper/
    │   ├── CollectMapper.java
    │   ├── CommentLikeMapper.java
    │   ├── PostCommentMapper.java
    │   ├── PostSupportMapper.java
    │   ├── SongCommentMapper.java
    │   └── ...                        # 其他 Mapper
    ├── model/
    │   ├── domain/                    # 数据库实体类 (11个)
    │   └── request/                   # 请求参数对象
    │       ├── CollectRequest.java
    │       ├── CommentLikeRequest.java
    │       ├── CommentRequest.java
    │       └── PostLikeRequest.java
    └── service/
        ├── CollectService.java
        ├── CommentLikeService.java
        ├── PostCommentService.java
        ├── PostSupportService.java
        ├── SongCommentService.java
        └── impl/                      # 接口实现类
            ├── CollectServiceImpl.java
            ├── CommentLikeServiceImpl.java
            ├── PostCommentServiceImpl.java
            ├── PostSupportServiceImpl.java
            └── SongCommentServiceImpl.java
```

---

> **文档版本**：v1.0  
> **生成日期**：2026-07-23  
> **项目**：音乐平台后端 music-new
