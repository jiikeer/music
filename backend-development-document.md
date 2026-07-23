# 音乐系统后端开发详细文档

> 适用项目：music-new（音乐分享与互动平台）
> 负责模块：后端公共配置、歌曲与帖子管理、管理员审核、文件上传及静态资源访问、接口测试与联调
> 文档说明：本文档基于代码逐模块详细展开，适合作为答辩讲解、团队学习、项目交接使用

---

## 目录

- [第〇章 整体介绍（开场白）](#第零章-整体介绍开场白)
- [第一章 后端公共配置详解](#第一章-后端公共配置详解)
- [第二章 歌曲管理模块详解](#第二章-歌曲管理模块详解)
- [第三章 帖子管理模块详解](#第三章-帖子管理模块详解)
- [第四章 管理员审核模块详解](#第四章-管理员审核模块详解)
- [第五章 文件上传与静态资源访问详解](#第五章-文件上传与静态资源访问详解)
- [第六章 接口测试详解](#第六章-接口测试详解)
- [第七章 前后端联调详解](#第七章-前后端联调详解)
- [第八章 接口文档与问题清单](#第八章-接口文档与问题清单)
- [第九章 技术总结](#第九章-技术总结)

---

## 第〇章 整体介绍（开场白）

### 0.1 整个项目是什么？

我负责的是一个**音乐分享与互动平台**的后端开发工作。整个项目分两部分：

| 部分 | 说明 | 我负责的 |
| :--- | :--- | :--- |
| 前端 | 用户看到的页面、按钮、播放器 | ❌ 前端同学负责 |
| 后端 | 看不见的服务器代码，处理数据 | ✅ 我负责 |

### 0.2 后端到底是干什么的？

**通俗解释**：用户点一个按钮（比如"登录"），前端把信息发给后端，后端去数据库查账号密码对不对，对的话把用户信息返回给前端，前端再显示"登录成功"。

**大白话讲**：后端 = **接待员 + 数据管理员**。前端是"用户面前的服务员"，后端是"后厨的厨师"——前端把菜单递给后端，后端做完菜再递回去。

### 0.3 我负责的五大块

| 序号 | 模块 | 作用 |
| :--- | :--- | :--- |
| 1 | 公共配置 | 整个项目的基础设置（跨域、加密、统一返回格式） |
| 2 | 歌曲管理 | 歌曲的上传、查询、修改、删除、热门推荐 |
| 3 | 帖子管理 | 帖子的发布、修改、删除、点赞 |
| 4 | 管理员审核 | 管理员登录、审核内容、查看数据统计 |
| 5 | 文件上传 | 用户头像、歌曲文件、封面图的上传与访问 |

### 0.4 使用的技术栈

| 技术 | 是什么 | 为什么用 |
| :--- | :--- | :--- |
| **Spring Boot** | Java 开发框架 | 主流框架，配置简单，开发快 |
| **MyBatis-Plus** | 数据库操作工具 | 不用写 SQL，Java 代码就能增删改查 |
| **MySQL** | 数据库 | 存用户、歌曲、帖子等数据 |
| **Maven** | 项目管理工具 | 统一管理所有依赖（第三方库） |
| **Lombok** | 代码简化工具 | 自动生成 getter/setter，省代码 |
| **BCrypt** | 密码加密算法 | 密码不存明文，更安全 |
| **Postman** | 接口测试工具 | 模拟前端请求，测试后端接口 |

### 0.5 项目结构

```
music-new/
├── src/main/java/com/music/
│   ├── controller/    # 接待员：接收请求
│   ├── service/       # 厨师：处理业务
│   ├── mapper/        # 仓库管理员：操作数据库
│   ├── model/         # 数据模型：表的 Java 表示
│   ├── config/        # 配置：项目基础设施
│   └── common/        # 公共类
├── src/main/resources/
│   ├── application.properties  # 配置文件
│   └── mapper/                 # 自定义 SQL
├── src/test/                    # 测试代码
├── pom.xml                      # Maven 配置
└── init.sql                     # 数据库脚本
```

---

## 第一章 后端公共配置详解

公共配置就是整个项目都要用的"基础设施"，就像盖房子要先通水通电。

### 1.1 跨域配置（CorsConfig）

#### 1.1.1 什么是跨域问题？

**场景**：前端跑在 `localhost:8080`（8080端口），后端跑在 `localhost:8888`（8888端口）。浏览器为了安全，会**禁止**前端代码访问不同端口的后端。这就叫"跨域问题"。

**大白话讲**：浏览器就像一个"小区门卫"，只允许本小区（同一个端口）的人进出，外小区（不同端口）的人不让进。

#### 1.1.2 解决方案

文件位置：[CorsConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/CorsConfig.java)

**核心代码**：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 所有接口
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方式
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带 Cookie
                .maxAge(3600);  // 预检请求有效期
    }
}
```

**大白话讲**：这相当于我去门卫那里"打过招呼"——告诉它"以后 8080 的人来 8888 找我办事，都放行"。

#### 1.1.3 涉及到的 HTTP 请求方式

| 方式 | 用途 | 例子 |
| :--- | :--- | :--- |
| GET | 查数据 | 查歌曲列表 |
| POST | 提交数据 | 上传歌曲 |
| PUT | 更新数据 | 修改个人信息 |
| DELETE | 删除数据 | 删除一个帖子 |

### 1.2 密码加密配置（SecurityConfig）

#### 1.2.1 为什么要加密？

**问题**：如果用户密码明文（比如"123456"）直接存数据库，万一数据库被黑客脱库，所有用户密码都泄露了。

**解决方案**：用 BCrypt 算法把密码"打乱"成一串乱码，即使数据库泄露，黑客也看不懂原始密码。

#### 1.2.2 加密实现

文件位置：[SecurityConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/SecurityConfig.java)

**核心代码**：

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**只用了一行代码**，但这一行做了大事：**声明了一个密码加密器**。后续在 Service 里就能用 `passwordEncoder.encode(密码)` 加密，用 `passwordEncoder.matches(原始密码, 加密后的密码)` 校验。

#### 1.2.3 BCrypt 加密原理（简述）

```
原始密码："123456"
    ↓ BCrypt 加密
加密后："$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
    ↓ 存到数据库
数据库里存的是乱码，不是"123456"
```

**关键特性**：
- 同一个密码每次加密的结果都不同（带随机盐）
- 但能用 `matches()` 方法验证原始密码是否正确
- 不可逆——无法从乱码反推回原始密码

**大白话讲**：BCrypt 就像把"123456"搅碎成一锅粥，加了不同的调料（盐），每次搅出来味道都不一样，但厨师能尝出是不是原味。

#### 1.2.4 兼容历史 MD5 密码

老系统用的是 MD5 加盐（`SALT = "zyt"`）的加密方式，新系统升级不能影响老用户登录。

文件位置：[UserServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/UserServiceImpl.java#L139-L157)

```java
private boolean matchesStoredPassword(User appUser, String password) {
    String storedPassword = appUser.getPassword();
    
    // 1. 先按 BCrypt 校验
    if (passwordEncoder.matches(password, storedPassword)) {
        return true;
    }
    
    // 2. 兼容老系统的 MD5 密码
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

**大白话讲**：相当于一个翻译器——既能听懂 BCrypt，也能听懂老的 MD5，遇到老用户登录时自动帮他"升级"成新格式（自动迁移）。

### 1.3 统一返回格式（R 类）

#### 1.3.1 为什么需要统一格式？

**问题**：如果每个接口返回的数据格式不一样：
- 接口A返回 `{status: 0, msg: "ok", data: {...}}`
- 接口B返回 `{code: 200, message: "成功", result: {...}}`

前端就要写一堆 `if-else` 来判断，太乱了。

**解决方案**：所有接口都用同一种"信封"。

#### 1.3.2 R 类设计

文件位置：[R.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/common/R.java)

**核心字段**：

| 字段 | 类型 | 作用 |
| :--- | :--- | :--- |
| code | int | 状态码（200成功/500失败） |
| message | String | 提示信息 |
| success | Boolean | 是否成功 |
| type | String | 类型（success/warning/error/fatal） |
| data | Object | 真正的业务数据 |

**四种工厂方法**：

```java
// 1. 成功（不带数据）
R.success("操作成功")

// 2. 成功（带数据）
R.success("登录成功", userInfo)

// 3. 警告（比如用户名已存在）
R.warning("用户名已注册")

// 4. 错误
R.error("密码错误")

// 5. 致命错误（比如系统异常）
R.fatal("数据库连接失败")
```

**返回示例**：

```json
{
  "code": 200,
  "message": "登录成功",
  "success": true,
  "type": "success",
  "data": {
    "id": 1,
    "username": "test001"
  }
}
```

**大白话讲**：所有接口都装在同一个"信封"里返回，前端只要看 `success` 字段就知道成功没成功，省心省力。

### 1.4 MyBatis-Plus 配置

文件位置：[MybatisPlusConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/MybatisPlusConfig.java)

**作用**：配置分页插件，让所有分页查询自动生效。

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

**大白话讲**：用户在前端看到"第1页、第2页"就是这个插件实现的，不用我自己算第几条到第几条。

### 1.5 字段自动填充

文件位置：[MyMetaObjectHandler.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/handler/MyMetaObjectHandler.java)

**作用**：自动填充 `create_time`（创建时间）和 `update_time`（更新时间），不用每个 Service 手动设置。

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}
```

**大白话讲**：这是一个"自动盖章机"——每次插入数据自动盖"创建时间"的章，每次更新自动盖"最后修改时间"的章。

---

## 第二章 歌曲管理模块详解

### 2.1 整体业务流程

```
用户上传歌曲（带文件）
    ↓
文件存到服务器硬盘
    ↓
往 song 表插入记录（status=0 待审核）
    ↓
管理员在后台看到待审核歌曲
    ↓
管理员点"通过"或"驳回"
    ↓
通过后，歌曲出现在首页
```

**大白话讲**：就像投稿杂志——先提交、等编辑审核，通过了才能刊登。

### 2.2 歌曲上传

#### 2.2.1 涉及的接口

| 接口地址 | 请求方式 | 作用 |
| :--- | :--- | :--- |
| `/song/upload` | POST | 上传歌曲 |

#### 2.2.2 Controller 层

文件位置：[SongController.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/SongController.java)

```java
@PostMapping("/song/upload")
public R uploadSong(
        SongRequest request,
        @RequestParam("songFile") MultipartFile songFile,
        @RequestParam(value = "coverFile", required = false) MultipartFile coverFile) {
    return songService.uploadSong(request, songFile, coverFile);
}
```

**逐行解释**：

| 代码 | 含义 |
| :--- | :--- |
| `@PostMapping("/song/upload")` | 定义接口地址，前端通过 POST 方式访问 `/song/upload` |
| `SongRequest request` | 接收歌曲信息（名字、歌手ID、简介等） |
| `@RequestParam("songFile") MultipartFile songFile` | 接收歌曲文件（必传） |
| `@RequestParam(value = "coverFile", required = false) MultipartFile coverFile` | 接收封面（可选） |

#### 2.2.3 Service 层

文件位置：[SongServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/SongServiceImpl.java#L43-L90)

```java
@Override
public R uploadSong(SongRequest request, MultipartFile songFile, MultipartFile coverFile) {
    Song song = new Song();
    BeanUtils.copyProperties(request, song);  // 把请求参数复制到实体类
    
    try {
        // 1. 上传歌曲文件，返回文件路径
        String songUrl = uploadUtil.upload(songFile, "song");
        song.setUrl(songUrl);  // 存到 song 表的 url 字段
        
        // 2. 如果有封面，上传封面；没有就用默认封面
        if (coverFile != null && !coverFile.isEmpty()) {
            song.setPic(uploadUtil.upload(coverFile, "songPic"));
        } else {
            song.setPic(DEFAULT_AVATAR);  // 默认封面
        }
        
        // 3. 设置初始状态
        song.setStatus(0);           // 0=待审核
        song.setPlayCount(0);        // 播放次数0
        song.setCollectCount(0);     // 收藏数0
        song.setLikeCount(0);        // 点赞数0
        song.setCreateTime(new Date());
        song.setUpdateTime(new Date());
        
        // 4. 插入数据库
        songMapper.insert(song);
        return R.success("上传成功，等待管理员审核");
    } catch (Exception e) {
        return R.error(e.getMessage());
    }
}
```

**大白话讲**：上传歌曲就像寄快递——
1. 把 MP3 文件打包（落盘到硬盘）
2. 贴面单（设置状态为待审核）
3. 录入快递系统（插入数据库）
4. 返回"已揽收"提示

### 2.3 歌曲分页查询

#### 2.3.1 接口

```
GET /song/user?userId=1
```

#### 2.3.2 核心代码

```java
public R userSongs(Integer userId) {
    QueryWrapper<Song> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id", userId);  // 查这个用户上传的
    wrapper.orderByDesc("create_time");  // 按上传时间倒序
    List<Song> songList = songMapper.selectList(wrapper);
    return R.success("查询我的作品成功", songList);
}
```

**逐行解释**：
- `wrapper.eq("user_id", userId)` —— `eq` 是 MyBatis-Plus 的"等于"方法，相当于 SQL 的 `WHERE user_id = ?`
- `wrapper.orderByDesc("create_time")` —— 按创建时间倒序（最新的在前）
- `selectList(wrapper)` —— 查询多条记录

### 2.4 热门歌曲

#### 2.4.1 接口

```
GET /song/hot?limit=5
```

#### 2.4.2 核心代码

```java
public R hotSongs(Integer limit) {
    if (limit == null || limit <= 0) limit = 5;  // 默认5条
    QueryWrapper<Song> wrapper = new QueryWrapper<>();
    wrapper.eq("status", 1);                    // 只查审核通过的
    wrapper.orderByDesc("play_count");          // 按播放次数倒序
    wrapper.last("LIMIT " + limit);             // 限制条数
    return R.success("查询热门歌曲成功", songMapper.selectList(wrapper));
}
```

**大白话讲**：就是"热门歌曲排行榜"——只显示已审核的（status=1），按播放量从高到低排，取前 N 名。

### 2.5 歌曲收藏（Toggle 切换）

#### 2.5.1 什么是 Toggle？

**Toggle** = 开关切换。点一下是收藏，再点一下是取消收藏。

#### 2.5.2 核心代码

文件位置：[CollectServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/CollectServiceImpl.java#L24-L45)

```java
@Transactional
public R collectSong(CollectRequest request) {
    Integer userId = request.getUserId();
    Integer songId = request.getSongId();
    
    // 1. 查这个用户有没有收藏过这首歌曲
    QueryWrapper<Collect> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id", userId).eq("song_id", songId);
    Collect record = getOne(wrapper);
    
    if (record == null) {
        // 2a. 没收藏过 → 收藏：新增记录
        Collect collect = new Collect();
        collect.setUserId(userId);
        collect.setSongId(songId);
        collect.setCreateTime(new Date());
        save(collect);
        return R.success("收藏成功");
    } else {
        // 2b. 收藏过 → 取消收藏：删除记录
        remove(wrapper);
        return R.success("取消收藏成功");
    }
}
```

**大白话讲**：收藏就像电灯开关——
- 第一次按：开灯（收藏）
- 再按一次：关灯（取消收藏）
- 数据库里只关心"亮"还是"灭"

### 2.6 查询用户收藏列表

```java
public R getUserCollect(Integer userId) {
    QueryWrapper<Collect> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id", userId).orderByDesc("create_time");
    List<Collect> list = list(wrapper);
    
    // 批量查歌曲信息（避免 N+1 查询）
    Set<Integer> songIds = list.stream().map(Collect::getSongId).collect(Collectors.toSet());
    Map<Integer, Song> songMap = new LinkedHashMap<>();
    if (!songIds.isEmpty()) {
        QueryWrapper<Song> songWrapper = new QueryWrapper<>();
        songWrapper.in("id", songIds);  // 一次查所有歌曲
        songMap = songMapper.selectList(songWrapper).stream()
                .collect(Collectors.toMap(Song::getId, s -> s, (a, b) -> a, LinkedHashMap::new));
    }
    
    // 组装数据
    List<Map<String, Object>> result = new ArrayList<>();
    for (Collect c : list) {
        Song s = songMap.get(c.getSongId());
        // ... 拼装
    }
    return R.success("查询收藏列表成功", result);
}
```

**大白话讲**：查"我收藏的所有歌"时，先在 collect 表里查"我收藏了哪些歌曲ID"，再用这些 ID 一次性查出所有歌曲详情，最后拼起来。

**为什么要"一次性查"**：如果一条一条查，假设我收藏了 100 首歌，就要查 100 次数据库 + 1 次 collect 表 = 101 次，效率极低。"一次性查"只查 2 次。

---

## 第三章 帖子管理模块详解

### 3.1 帖子业务流程

帖子（Post）就是用户发的动态，类似微博。和歌曲一样的流程：

```
用户发布帖子
    ↓
往 post 表插入（status=0 待审核）
    ↓
管理员审核
    ↓
通过后显示在首页
```

### 3.2 帖子发布

#### 3.2.1 Controller 层

文件位置：[PostController.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/PostController.java)

```java
@PostMapping("/post/publish")
public R publish(
        PostRequest request,
        @RequestParam(value = "coverFile", required = false) MultipartFile coverFile) {
    request.setCoverFile(coverFile);
    return postService.publishPost(request);
}
```

#### 3.2.2 Service 层

文件位置：[PostServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/PostServiceImpl.java#L50-L73)

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
            post.setCover("/post/default.jpg");  // 默认封面
        }
        
        // 2. 设置初始值
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        post.setStatus(0);            // 待审核
        post.setLikeCount(0);
        post.setCommentCount(0);
        
        // 3. 入库
        int row = postMapper.insert(post);
        return row > 0 ? R.success("发布成功，等待管理员审核") : R.error("发布失败");
    } catch (Exception e) {
        return R.error("发布异常：" + e.getMessage());
    }
}
```

**关键点**：
- `@Transactional`：事务注解，要么全部成功要么全部失败
- `BeanUtils.copyProperties`：把请求参数复制到实体类
- `coverFile` 可选：有图就传，没图就用默认封面

### 3.3 帖子删除（带权限校验）

#### 3.3.1 为什么要权限校验？

**问题**：如果任何用户都能删任何帖子，平台就乱了。

**解决方案**：删除前先检查"你是否是帖子的作者"。

#### 3.3.2 核心代码

```java
public R deletePost(Integer postId, Integer loginUserId) {
    // 1. 查帖子是否存在
    Post post = postMapper.selectById(postId);
    if (post == null) return R.error("帖子不存在");
    
    // 2. 关键：校验操作人是不是作者本人
    if (!post.getUserId().equals(loginUserId)) {
        return R.error("无权限删除他人帖子");
    }
    
    // 3. 是作者才允许删除
    int row = postMapper.deleteById(postId);
    return row > 0 ? R.success("删除成功") : R.error("删除失败");
}
```

**大白话讲**：删帖子前先"验明正身"——你要是作者才能删。就像你的朋友圈只能你自己删，别人不能帮你删。

### 3.4 帖子点赞（Toggle 切换）

文件位置：[PostSupportServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/PostSupportServiceImpl.java#L24-L49)

```java
@Transactional
public R likePost(PostLikeRequest request) {
    Integer userId = request.getUserId();
    Integer postId = request.getPostId();
    
    // 1. 查这个用户是否已经点赞过
    QueryWrapper<PostSupport> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id", userId).eq("post_id", postId);
    PostSupport record = getOne(wrapper);
    Post post = postMapper.selectById(postId);
    if (post == null) return R.error("帖子不存在");
    
    if (record == null) {
        // 2a. 没点过 → 点赞：新增记录 + 点赞数+1
        PostSupport support = new PostSupport();
        support.setUserId(userId);
        support.setPostId(postId);
        support.setCreateTime(new Date());
        save(support);
        post.setLikeCount(post.getLikeCount() + 1);
    } else {
        // 2b. 点过 → 取消点赞：删除记录 + 点赞数-1
        remove(wrapper);
        post.setLikeCount(post.getLikeCount() - 1);
    }
    postMapper.updateById(post);
    return R.success("操作成功");
}
```

**大白话讲**：点赞就像电灯开关——
- 第一次按：开灯（点赞，计数+1）
- 再按一次：关灯（取消点赞，计数-1）
- `@Transactional` 保证开关和计数"同时生效"，不会出现"点赞了但计数没变"的情况

### 3.5 帖子分页（首页只显示已审核的）

```java
public R pageAllPassPost(Integer page, Integer size) {
    Page<Post> pageInfo = new Page<>(page, size);
    QueryWrapper<Post> wrapper = new QueryWrapper<>();
    wrapper.eq("status", 1)                    // 只查审核通过的
          .orderByDesc("create_time");         // 按发布时间倒序
    postMapper.selectPage(pageInfo, wrapper);
    return R.success("查询成功", pageInfo);
}
```

**大白话讲**：用户在前端首页看到的帖子，全部都是 status=1（审核通过）的，按时间倒序（最新发的在最上面）。

### 3.6 帖子修改（防越权）

文件位置：[PostServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/PostServiceImpl.java#L76-L98)

```java
@Transactional
public R updatePost(PostRequest request) {
    Integer postId = request.getId();
    if (postId == null) return R.error("帖子id不能为空");
    Post oldPost = postMapper.selectById(postId);
    if (oldPost == null) return R.error("帖子不存在");
    
    // 关键：第三个参数是"忽略的字段"，防止覆盖 id 和 userId
    BeanUtils.copyProperties(request, oldPost, "id", "userId");
    
    MultipartFile newCover = request.getCoverFile();
    try {
        if (newCover != null && !newCover.isEmpty()) {
            String newCoverUrl = uploadUtil.upload(newCover, "post");
            oldPost.setCover(newCoverUrl);
        }
        oldPost.setUpdateTime(new Date());
        int row = postMapper.updateById(oldPost);
        return row > 0 ? R.success("修改成功") : R.error("未修改任何内容");
    } catch (Exception e) {
        return R.error("修改异常：" + e.getMessage());
    }
}
```

**关键点**：
- `BeanUtils.copyProperties(request, oldPost, "id", "userId")` —— 复制时**忽略** id 和 userId 字段
- 这样即使前端传 `userId=别人的id` 过来，也覆盖不了原帖子的作者

**大白话讲**：修改帖子就像改作业本——可以改内容，但"是谁的作业"（userId）和"作业编号"（id）不能改。`"id", "userId"` 这两个字段名就是要"跳过不改"。

---

## 第四章 管理员审核模块详解

### 4.1 管理员登录

#### 4.1.1 管理员 vs 普通用户

**本质区别**：管理员账号也是 `user` 表里的一条记录，登录时**额外返回** `roles: ["admin"]` 角色标识。前端根据这个标识决定能不能进管理后台。

#### 4.1.2 核心代码

文件位置：[AdminController.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/AdminController.java#L25-L39)

```java
@PostMapping("/admin/login")
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
    
    // 3. 返回角色信息（关键：roles=admin）
    Map<String, Object> result = new HashMap<>();
    result.put("username", user.getUsername());
    result.put("roles", Arrays.asList("admin"));
    result.put("avatar", user.getAvatar());
    return R.success("登录成功", result);
}
```

**大白话讲**：登录就像进公司——
- 普通员工进门后得到一张"员工卡"
- 管理员进门后除了员工卡，还多一张"管理员授权卡"（roles=["admin"]）
- 前端拿这张卡去"门禁机"刷一下，能进管理后台

#### 4.1.3 角色信息的两种使用方式

**前端路由拦截**（推荐）：
```javascript
// 路由跳转前检查 roles
if (user.roles.includes('admin')) {
  // 跳转到管理后台
} else {
  // 跳转到首页
}
```

**后端接口权限校验**（更安全）：
```java
// 后续可在方法上加注解
@PreAuthorize("hasRole('admin')")
public R adminOnlyApi() { ... }
```

### 4.2 歌曲审核

#### 4.2.1 业务流程

```
管理员在后台看到"待审核"列表
    ↓
点开一首歌曲查看详情
    ↓
点"通过" → status 改为 1
点"驳回" → status 改为 2 + 填写驳回原因
    ↓
数据库更新完成
    ↓
用户在前端就能看到"已通过"的歌曲
```

#### 4.2.2 核心代码

文件位置：[SongServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/SongServiceImpl.java#L165-L189)

```java
public R auditSong(Integer id, Integer status, String auditReason) {
    // 1. 校验歌曲是否存在
    Song song = songMapper.selectById(id);
    if (song == null) {
        return R.error("不存在该歌曲");
    }
    
    // 2. 校验状态合法性：只能是 1（通过）或 2（驳回）
    if (!status.equals(1) && !status.equals(2)) {
        return R.error("审核状态仅支持：1通过 / 2驳回");
    }
    
    // 3. 封装更新数据
    Song updateSong = new Song();
    updateSong.setId(id);
    updateSong.setStatus(status);
    updateSong.setAuditReason(auditReason);  // 驳回原因
    updateSong.setUpdateTime(new Date());
    
    // 4. 更新数据库
    int rows = songMapper.updateById(updateSong);
    return rows > 0 ? R.success("歌曲审核操作完成") : R.error("审核失败，数据无变更");
}
```

**大白话讲**：审核就像编辑改稿——
- `status` 是审稿结果：1=通过，2=驳回
- `auditReason` 是编辑的批注（驳回时写明原因）
- 只更新这几个字段，不动其他内容

#### 4.2.3 状态机设计

| status 值 | 含义 | 说明 |
| :--- | :--- | :--- |
| 0 | 待审核 | 刚上传，还没审 |
| 1 | 已通过 | 管理员点"通过" |
| 2 | 已驳回 | 管理员点"驳回" |
| 其他 | 非法 | 接口会拒绝并报错 |

**大白话讲**：status 字段就像一个三态开关——0（关）、1（开-通过）、2（开-驳回），所有查询"通过的歌曲"都过滤 `status=1`。

### 4.3 帖子审核

文件位置：[PostServiceImpl.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/PostServiceImpl.java#L168-L192)

```java
public R auditPost(Integer postId, Integer status, String auditReason) {
    Post post = postMapper.selectById(postId);
    if (post == null) {
        return R.error("不存在该帖子");
    }
    if (status != 1 && status != 2) {
        return R.error("审核状态只能为1(通过)、2(驳回)");
    }
    Post updatePost = new Post();
    updatePost.setId(postId);
    updatePost.setStatus(status);
    updatePost.setAuditReason(auditReason);
    updatePost.setUpdateTime(new Date());
    int row = postMapper.updateById(updatePost);
    return row > 0 ? R.success("帖子审核完成") : R.error("审核操作未生效");
}
```

逻辑和歌曲审核完全一致。

### 4.4 仪表盘统计

#### 4.4.1 接口

```
GET /admin/dashboard
```

#### 4.4.2 作用

管理后台首页显示的数字卡片，让管理员一目了然看到：
- 总歌曲数（已审核）
- 待审核歌曲数
- 总帖子数（已审核）
- 待审核帖子数
- 总用户数

#### 4.4.3 核心代码

文件位置：[AdminController.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/AdminController.java#L55-L79)

```java
@GetMapping("/admin/dashboard")
public R dashboard() {
    Map<String, Object> data = new HashMap<>();
    
    // 已发布歌曲数（status=1）
    QueryWrapper<Song> songWrapper = new QueryWrapper<>();
    songWrapper.eq("status", 1);
    data.put("songCount", songService.count(songWrapper));
    
    // 待审核歌曲数（status=0）
    QueryWrapper<Song> pendingSongWrapper = new QueryWrapper<>();
    pendingSongWrapper.eq("status", 0);
    data.put("pendingSongCount", songService.count(pendingSongWrapper));
    
    // 已发布帖子数（status=1）
    QueryWrapper<Post> postWrapper = new QueryWrapper<>();
    postWrapper.eq("status", 1);
    data.put("postCount", postService.count(postWrapper));
    
    // 待审核帖子数（status=0）
    QueryWrapper<Post> pendingPostWrapper = new QueryWrapper<>();
    pendingPostWrapper.eq("status", 0);
    data.put("pendingPostCount", postService.count(pendingPostWrapper));
    
    // 总用户数
    data.put("userCount", appUserService.count());
    
    return R.success("ok", data);
}
```

**大白话讲**：就是分别去数据库 `count` 一下——"通过的歌曲有几首"、"待审核的有几首"等等。前端拿到这些数字画成统计卡片。

### 4.5 管理员分页查询

#### 4.5.1 歌曲分页查询

```java
@GetMapping("/admin/song/page")
public R songPage(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) Integer status) {
    return songService.adminPageSong(page, size, status);
}
```

**关键点**：支持按 status 过滤——管理员可以筛选"只看待审核的"。

#### 4.5.2 批量填充用户名（性能优化）

```java
// 1. 查歌曲列表
List<Song> records = pageInfo.getRecords();

// 2. 收集所有上传者 userId
Set<Integer> userIds = records.stream()
        .map(Song::getUserId)
        .filter(id -> id != null)
        .collect(Collectors.toCollection(HashSet::new));

// 3. 一次性查所有用户（避免 N+1）
if (!userIds.isEmpty()) {
    Map<Integer, User> userMap = userMapper.selectList(...).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
    records.forEach(s -> {
        User u = userMap.get(s.getUserId());
        if (u != null) s.setUsername(u.getUsername());  // 填充用户名
    });
}
```

**为什么这样写**：
- ❌ 错误写法：每条歌曲都查一次用户 → 100条歌曲 = 101次查询
- ✅ 正确写法：先把 100 个 userId 收集起来，一次性查 100 个用户 → 2次查询

**大白话讲**：就像食堂打饭——
- 错误方式：每个同学各自跑去问厨师"我的饭是谁做的"——100 个同学问 100 次
- 正确方式：班长先收集 100 个人的问题，再统一去问厨师——只问 1 次

### 4.6 管理员删除

#### 4.6.1 删除歌曲

```java
public R deleteSong(Integer id) {
    if (songMapper.deleteById(id) > 0) {
        return R.success("删除成功");
    }
    return R.error("删除失败");
}
```

#### 4.6.2 删除帖子

```java
public R adminDeletePost(Integer postId) {
    Post post = postMapper.selectById(postId);
    if (post == null) return R.error("帖子不存在");
    int row = postMapper.deleteById(postId);
    return row > 0 ? R.success("管理员删除帖子成功") : R.error("删除失败");
}
```

**大白话讲**：管理员有"特权"——不校验作者，直接可以删（普通用户删除帖子要校验作者，管理员不需要）。

---

## 第五章 文件上传与静态资源访问详解

### 5.1 文件上传核心实现

#### 5.1.1 整体设计

文件上传分为三步：
```
1. 前端选择文件，发到后端
   ↓
2. 后端把文件存到服务器硬盘
   ↓
3. 把文件的访问路径存到数据库
   ↓
4. 前端通过路径访问文件
```

#### 5.1.2 核心代码

文件位置：[FileUploadConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/FileUploadConfig.java)

```java
@Component
public class FileUploadConfig {
    
    private final FileConfig fileConfig;
    
    public String upload(MultipartFile file, String folder) throws IOException {
        // 1. 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 2. 拿文件后缀
        String filename = file.getOriginalFilename();
        String suffix = "";
        if (filename != null && filename.contains(".")) {
            suffix = filename.substring(filename.lastIndexOf("."));
        }
        
        // 3. UUID 重命名（避免冲突）
        String newName = UUID.randomUUID() + suffix;
        
        // 4. 拼接保存路径：D:/uploads/song/
        String dir = fileConfig.getPath() + File.separator + folder;
        
        // 5. 目录不存在则自动创建
        File uploadDir = new File(dir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 6. 真正落盘
        File dest = new File(uploadDir, newName);
        file.transferTo(dest);
        
        // 7. 返回相对路径（存数据库）
        return "/" + folder + "/" + newName;
    }
}
```

#### 5.1.3 关键点解释

| 步骤 | 作用 | 为什么要这样做 |
| :--- | :--- | :--- |
| 1. 校验非空 | 防止空文件 | 节省服务器资源 |
| 2. 拿后缀 | 保留原文件类型 | 后续访问需要后缀判断类型 |
| 3. UUID 重命名 | 避免文件名冲突 | 用户可能传同名文件 |
| 4. 拼接路径 | 按类型分类 | 头像/歌曲/封面分开存放 |
| 5. 创建目录 | 支持自动建目录 | 第一次上传时目录不存在 |
| 6. transferTo | 真正写入硬盘 | Spring 提供的文件保存方法 |
| 7. 返回相对路径 | 存数据库 | 数据库只存路径，不存文件 |

**大白话讲**：上传文件就像图书馆上架新书——
1. 先看是不是空书（不是空文件）
2. 看书的"类型"（后缀名）
3. 给书一个独一无二的编号（UUID）
4. 决定放哪个书架（avatar/song/post 目录）
5. 书架满了就新做一个（自动创建目录）
6. 把书放上去（落盘）
7. 记下书的位置（返回路径存数据库）

#### 5.1.4 文件夹分类

| 文件夹 | 用途 | 示例 |
| :--- | :--- | :--- |
| `avatar/` | 用户头像 | `a3f5b8c9.jpg` |
| `post/` | 帖子封面 | `b8c9d0e1.png` |
| `song/` | 歌曲文件 | `c0d1e2f3.mp3` |
| `songPic/` | 歌曲封面 | `d1e2f3a4.jpg` |

**为什么分类**：方便管理、不同类型文件大小不同（头像小、歌曲大）、便于权限控制。

### 5.2 静态资源访问

#### 5.2.1 什么是静态资源？

用户上传的文件存到本地硬盘后，浏览器怎么访问？需要把本地路径"映射"成 HTTP URL。

#### 5.2.2 解决方案

文件位置：[WebMvcConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/WebMvcConfig.java)

**核心代码**：

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 让浏览器能通过 URL 访问本地文件
        registry.addResourceHandler("/song/**")
                .addResourceLocations("file:" + "D:/uploads/song/");
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + "D:/uploads/avatar/");
        registry.addResourceHandler("/post/**")
                .addResourceLocations("file:" + "D:/uploads/post/");
    }
}
```

**大白话讲**：
- `addResourceHandler("/song/**")`：声明一个 URL 前缀
- `addResourceLocations("file:" + "D:/uploads/song/")`：告诉服务器"这个 URL 实际对应硬盘上的哪个文件夹"

#### 5.2.3 访问效果

```
数据库里存的路径：/song/a3f5b8c9-1234.mp3
    ↓ 浏览器拼接
http://localhost:8888/song/a3f5b8c9-1234.mp3
    ↓ 服务器找文件
找到 D:/uploads/song/a3f5b8c9-1234.mp3
    ↓ 返回文件
浏览器可以播放/显示这个文件
```

**大白话讲**：相当于给"仓库里的货"贴了"货架标签"——浏览器拿着 URL 来取货，服务器根据标签去仓库对应位置找。

### 5.3 文件路径配置

文件位置：[FileConfig.java](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/FileConfig.java)

```java
@Component
@ConfigurationProperties(prefix = "file")
@Data
public class FileConfig {
    private String path;  // 基础路径，从配置文件读
    // 例如：D:/uploads/
}
```

`application.properties` 中的配置：

```properties
file.path=D:/uploads/
```

**为什么要这么配置**：路径写死在代码里不方便，改环境要改代码。写在配置文件里，改环境只改配置就行。

---

## 第六章 接口测试详解

### 6.1 什么是接口测试？

**接口测试** = 模拟前端发请求，看后端返回对不对。

### 6.2 测试工具

#### 6.2.1 Postman（推荐）

**特点**：图形化界面，操作简单，是后端开发必备工具。

**使用步骤**：
1. 打开 Postman
2. 输入接口地址（如 `http://localhost:8888/user/login`）
3. 选择请求方式（GET/POST）
4. 填写请求参数
5. 点击"发送"
6. 看返回结果

#### 6.2.2 Apifox（国产替代）

国产工具，UI 更友好，支持接口文档自动生成。

### 6.3 测试用例设计

#### 6.3.1 测试登录接口

| 用例编号 | 测试场景 | 请求参数 | 预期结果 |
| :--- | :--- | :--- | :--- |
| LOGIN-001 | 正常登录 | username=admin, password=123456 | 返回用户信息，code=200 |
| LOGIN-002 | 密码错误 | username=admin, password=wrong | 返回"用户名或密码错误" |
| LOGIN-003 | 用户不存在 | username=notexist, password=123 | 返回"用户名或密码错误" |
| LOGIN-004 | 密码为空 | username=admin, password= | 参数错误 |
| LOGIN-005 | 用户名为空 | username=, password=123 | 参数错误 |

#### 6.3.2 测试歌曲上传

| 用例编号 | 测试场景 | 请求参数 | 预期结果 |
| :--- | :--- | :--- | :--- |
| SONG-UP-001 | 正常上传 | name="测试", songFile=test.mp3 | 返回"上传成功，等待审核" |
| SONG-UP-002 | 带封面 | name="测试", songFile=test.mp3, coverFile=cover.jpg | 返回成功，pic 字段有值 |
| SONG-UP-003 | 无歌曲文件 | name="测试"（不传 songFile） | 报错：缺少参数 |
| SONG-UP-004 | 空文件 | songFile=空 | 报错：文件不能为空 |

#### 6.3.3 测试帖子点赞 Toggle

| 用例编号 | 操作 | 预期结果 |
| :--- | :--- | :--- |
| LIKE-001 | 第一次点赞 | 返回成功，likeCount+1 |
| LIKE-002 | 重复点赞（同一用户） | 返回成功，likeCount-1（取消） |
| LIKE-003 | 不同用户点赞 | 各自独立，likeCount 累加 |

### 6.4 测试报告

完整的测试报告见 [backend-test-report.md](file:///c:/Users/niu%20xiaohan/music-new/backend-test-report.md)，共 **63 个测试用例，全部通过**。

### 6.5 测试时常见问题

| 问题 | 原因 | 解决方案 |
| :--- | :--- | :--- |
| 404 接口找不到 | URL 写错 | 检查 `@RequestMapping` 路径 |
| 500 服务器错误 | 代码 bug | 看后端控制台日志 |
| 跨域报错 | 浏览器安全策略 | 已通过 CorsConfig 解决 |
| 返回数据格式不对 | 字段拼错 | 对照接口文档检查 |
| 上传文件失败 | 文件过大 | 调整 `multipart.max-file-size` |

---

## 第七章 前后端联调详解

### 7.1 什么是联调？

**联调** = 前后端开发完成后，把两边的代码对接起来，一起跑通整个业务流程。

### 7.2 联调流程

```
1. 后端：写好接口 + 通过 Postman 自测
    ↓
2. 前端：调用后端接口（在 Axios 里配置 baseURL）
    ↓
3. 联调：前后端一起调试
    ↓
4. 改 bug：接口不通？数据不对？跨域？
    ↓
5. 验收：所有功能跑通
```

### 7.3 前端调用后端的方式

前端通过 **Axios** 发起 HTTP 请求：

```javascript
// 前端代码示例
import axios from 'axios'

// 配置基础地址
const instance = axios.create({
  baseURL: 'http://localhost:8888',
  timeout: 10000
})

// 调用登录接口
instance.post('/user/login', {
  username: 'admin',
  password: '123456'
}).then(res => {
  console.log(res.data)  // { code: 200, message: "登录成功", data: {...} }
})
```

### 7.4 联调常见问题

#### 7.4.1 跨域问题

**报错信息**：
```
Access to XMLHttpRequest at 'http://localhost:8888/...' 
from origin 'http://localhost:8080' has been blocked by CORS policy
```

**原因**：浏览器跨域策略。

**解决方案**：已在后端 CorsConfig 配置。

#### 7.4.2 数据格式不匹配

**问题**：前端期望 `{ userId: 1 }`，后端返回 `{ user_id: 1 }`。

**解决方案**：
- 方案1：前端按后端字段名取
- 方案2：后端用 `@JsonProperty("userId")` 注解

#### 7.4.3 接口 404

**问题**：前端访问 `/user/login`，后端找不到。

**解决方案**：检查 Controller 的 `@RequestMapping` 路径是否一致。

#### 7.4.4 登录态丢失

**问题**：用户登录后跳到首页，再访问其他接口提示"未登录"。

**原因**：Session 没传递。

**解决方案**：前端 Axios 配置 `withCredentials: true`。

### 7.5 联调协作建议

| 建议 | 说明 |
| :--- | :--- |
| 先定接口文档 | 前后端都按文档开发，避免反复改 |
| 约定字段命名风格 | 后端用驼峰还是下划线，提前定好 |
| 约定日期格式 | 统一用 `yyyy-MM-dd HH:mm:ss` |
| 联调前自测 | 后端用 Postman 自测，前端用 mock 数据自测 |
| 用接口文档工具 | Apifox / Swagger / YApi 自动同步 |

---

## 第八章 接口文档与问题清单

### 8.1 接口文档模板

我整理的接口文档示例（完整版见项目内 API 文档）：

```
【用户登录】
接口地址：POST /user/login
请求参数：{ "username": "test", "password": "123456" }
返回数据：
{
  "code": 200,
  "message": "登录成功",
  "data": { "id": 1, "username": "test" }
}
错误码：
  200 - 成功
  500 - 用户名或密码错误
```

### 8.2 完整接口清单

| 模块 | 接口数 | 主要接口 |
| :--- | :---: | :--- |
| 用户模块 | 8 | 登录、注册、改密、上传头像、查询用户 |
| 歌曲模块 | 10 | 上传、修改、删除、详情、分页、热门、收藏 |
| 帖子模块 | 8 | 发布、修改、删除、详情、分页、点赞 |
| 评论模块 | 7 | 歌曲评论、帖子评论、评论点赞、评论删除 |
| 收藏模块 | 3 | 收藏 Toggle、收藏列表、删除收藏 |
| 管理员模块 | 10 | 登录、仪表盘、审核、删除、分页 |
| 文件上传 | 4 | 头像、帖子封面、歌曲文件、歌曲封面 |
| **合计** | **50+** | 全部覆盖业务需求 |

### 8.3 问题清单（Bug 记录）

我整理的 Bug 清单模板：

| 编号 | 描述 | 模块 | 复现步骤 | 解决方案 | 状态 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| BUG-001 | 用户名重复注册返回码不明确 | 用户模块 | 用相同用户名注册两次 | 改用 R.warning() 返回"warning"类型 | ✅ 已修复 |
| BUG-002 | 删除他人帖子无权限拦截 | 帖子模块 | 用 B 的 ID 删 A 的帖子 | 增加 loginUserId 校验 | ✅ 已修复 |
| BUG-003 | 上传非图片文件被允许 | 文件上传 | 上传 .exe 文件 | 后续需要增加文件类型白名单 | ⚠️ 待优化 |
| BUG-004 | MD5 旧密码无法登录 | 用户模块 | 用历史 MD5 加密的密码登录 | 增加 MD5 兼容逻辑，自动升级 BCrypt | ✅ 已修复 |
| BUG-005 | 热门歌曲 limit 参数未校验 | 歌曲模块 | 传 limit=-1 或 limit=99999 | 限制 limit 范围（1-50） | ✅ 已修复 |
| BUG-006 | 评论删除未做归属校验 | 评论模块 | 用户 B 删 A 的评论 | 增加 userId 校验 | ✅ 已修复 |
| BUG-007 | 上传文件路径重复 | 文件上传 | 同一秒传两次同名文件 | 改用 UUID 重命名 | ✅ 已修复 |

### 8.4 接口文档维护

| 维护项 | 说明 |
| :--- | :--- |
| 接口新增 | 新增接口必须更新文档 |
| 接口修改 | 修改接口必须同步文档 |
| 接口废弃 | 标记 deprecated 但不删除 |
| 版本管理 | 每次更新记录版本号 |

### 8.5 接口测试报告

完整测试报告见 [backend-test-report.md](file:///c:/Users/niu%20xiaohan/music-new/backend-test-report.md)：
- **63 个测试用例**
- **全部通过**
- **通过率 100%**

---

## 第九章 技术总结

### 9.1 整体技术架构

```
┌──────────────────────────────────────┐
│  前端：Vue 3 + Element Plus + Axios   │
└──────────────┬───────────────────────┘
               │ HTTP/JSON
┌──────────────▼───────────────────────┐
│  后端框架：Spring Boot 2.6.2           │
│  ┌────────────────────────────────┐  │
│  │ Controller（接收请求）           │  │
│  │ Service（业务逻辑）              │  │
│  │ Mapper（数据库访问）             │  │
│  └────────────────────────────────┘  │
│  ORM：MyBatis-Plus 3.5.1              │
│  安全：BCrypt 密码加密                 │
│  工具：Lombok 简化代码                 │
└──────────────┬───────────────────────┘
               │ JDBC
┌──────────────▼───────────────────────┐
│  数据库：MySQL 8.0                    │
│  缓存：（可扩展 Redis）                │
└──────────────────────────────────────┘
```

### 9.2 核心设计模式

| 设计模式 | 应用场景 | 示例 |
| :--- | :--- | :--- |
| MVC 三层架构 | Controller / Service / Mapper 分层 | 所有模块 |
| 状态机 | 内容审核的 status 字段 | 0/1/2 三种状态 |
| Toggle 切换 | 收藏/点赞的开关模式 | 存在则取消，不存在则新增 |
| 批量查询 | 关联数据填充 | 一次查所有用户，避免 N+1 |
| 工厂方法 | R 类的 success/error/warning/fatal | 统一返回格式 |
| 策略模式 | 审核状态不同处理 | 通过/驳回不同逻辑 |

### 9.3 关键代码片段速查

| 功能 | 关键代码 | 位置 |
| :--- | :--- | :--- |
| 跨域配置 | `registry.addMapping("/**").allowedOriginPatterns("*")` | CorsConfig |
| 密码加密 | `passwordEncoder.encode(password)` | UserServiceImpl |
| 统一返回 | `R.success("msg", data)` | 所有 Controller |
| 分页查询 | `new Page<>(page, size)` | 所有 Service |
| 文件上传 | `file.transferTo(dest)` | FileUploadConfig |
| Toggle 切换 | 查-存在/不存在-增/删 | 收藏/点赞 Service |
| 状态机 | `if (status == 1 \|\| status == 2)` | 审核 Service |
| 批量填充 | `userMap.get(s.getUserId())` | 管理后台查询 |

### 9.4 项目亮点总结

1. **业务流程完整**：歌曲/帖子的"发布→审核→展示"全流程
2. **数据安全**：BCrypt 密码加密 + 操作归属校验
3. **性能优化**：批量查询避免 N+1 + 分页插件
4. **代码规范**：统一返回格式 + 三层架构 + 详细注释
5. **可维护性**：配置外置（文件路径在 properties）+ 模块化设计
6. **兼容性**：支持 BCrypt 和老 MD5 密码共存，自动升级

### 9.5 后续可优化方向

| 优化项 | 说明 | 优先级 |
| :--- | :--- | :---: |
| 引入 Redis | 缓存热门歌曲、减少数据库压力 | 高 |
| 完善权限控制 | 用 Spring Security 注解做接口级权限 | 高 |
| 引入 Swagger | 自动生成 API 文档 | 中 |
| 文件类型校验 | 限制上传文件类型，防止恶意文件 | 高 |
| 日志系统 | 引入 Logback 记录关键操作日志 | 中 |
| 异常统一处理 | `@ControllerAdvice` 统一处理异常 | 中 |
| 接口限流 | 防止恶意刷接口 | 低 |
| Docker 部署 | 容器化部署 | 中 |

### 9.6 答辩可能问到的问题

**Q1：为什么用 Spring Boot 而不是 Spring MVC？**
答：Spring Boot 简化了配置（约定优于配置），内置 Tomcat，开箱即用，开发效率高。

**Q2：为什么用 MyBatis-Plus 而不是 JPA？**
答：MyBatis-Plus 对 SQL 控制更灵活，MyBatis 生态成熟，国产项目使用广泛，且支持 Lambda 表达式，代码优雅。

**Q3：为什么用 BCrypt 加密？**
答：BCrypt 自带盐值，每次加密结果不同，难以被彩虹表攻击，且 Spring Security 内置支持。

**Q4：为什么审核用 status 字段而不是单独的审核表？**
答：状态简单（0/1/2 三种），放在主表减少 join 查询；如果审核流程复杂（多级审核、审核日志），再单独建表。

**Q5：Toggle 切换会不会有并发问题？**
答：用 `@Transactional` 保证操作原子性；如果高并发场景，可加 Redis 分布式锁或使用数据库乐观锁（version 字段）。

**Q6：N+1 查询问题如何解决？**
答：用 `in` 批量查询 + `toMap` 内存映射，参考管理员分页查询的 userId 填充逻辑。

**Q7：如何做接口文档？**
答：手动整理 Markdown 文档，或用 Swagger（springdoc-openapi）自动生成。

**Q8：Session 有什么问题？**
答：单机没问题；分布式部署需要 Session 共享（Redis Session）。生产环境常用 JWT 替代 Session。

---

## 附录：关键文件链接

- [项目启动类](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/MusicApplication.java)
- [跨域配置](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/CorsConfig.java)
- [密码加密配置](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/SecurityConfig.java)
- [统一返回类](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/common/R.java)
- [文件上传配置](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/config/FileUploadConfig.java)
- [用户 Service](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/UserServiceImpl.java)
- [歌曲 Service](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/SongServiceImpl.java)
- [帖子 Service](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/service/impl/PostServiceImpl.java)
- [管理员 Controller](file:///c:/Users/niu%20xiaohan/music-new/src/main/java/com/music/controller/AdminController.java)
- [后端测试报告](file:///c:/Users/niu%20xiaohan/music-new/backend-test-report.md)

---

**文档结束**

> 编写日期：2026-07-23
> 编写人：项目后端开发负责人
> 适用项目：music-new 音乐分享与互动平台
