# ContactHub

多用户在线通讯录管理系统 —— 基于 Spring Boot 3.x + Spring Data JPA + Bootstrap 5 构建，支持多用户数据隔离、分组管理、软删除回收站、Excel 导入导出、头像自动缩放上传、IP 暴力破解防护、国际化中英文切换。

## 功能特性

- **用户认证** — 注册/登录/退出，BCrypt 密码加密，Session 30 分钟超时
- **IP 暴力破解防护** — 同一 IP 连续登录失败 3 次自动锁定 5 分钟
- **联系人管理** — 增删改查，姓/名拆分（CardDAV 兼容），三类电话，省/市/区三级地域
- **分组管理** — 自定义分组，删除分组时联系人自动迁移到默认分组（事务保证）
- **软删除回收站** — 删除不丢数据，支持恢复、彻底删除、清空
- **多条件组合查询** — JPA Specification 动态查询，姓名/电话/地域/公司/职位/分组任意组合
- **头像上传** — Thumbnailator 自动缩放 200×200，中心裁剪，旧文件自动清理
- **Excel 导入导出** — Apache POI，17 字段，支持按筛选/全部/选中三种导出模式
- **管理员后台** — 用户管理（增删改禁用/启用/重置密码），级联删除
- **国际化** — 中英文切换，200+ 翻译条目，localStorage 持久化语言偏好
- **Swagger API 文档** — springdoc-openapi 自动生成，访问 `/swagger-ui.html`

## 技术栈

| 层次 | 技术 | 说明 |
|:---|:---|:---|
| 后端框架 | Spring Boot 3.2.5 | 内嵌 Tomcat，约定优于配置 |
| 安全框架 | Spring Security | BCrypt 加密、CSRF 防护、方法级权限控制 |
| ORM | Spring Data JPA | JPA Specification 动态查询 |
| 数据库 | MySQL 8.0+ | 生产环境；H2 用于测试 |
| 前端页面 | Static HTML + jQuery AJAX | 纯静态页面，AJAX 异步交互 |
| 前端 UI | Bootstrap 5 + jQuery | 响应式栅格、模态框、AJAX |
| Excel 处理 | Apache POI 5.2.5 | 导入导出 `.xlsx` |
| 图片处理 | Thumbnailator 0.4.20 | 头像缩放压缩 |
| 验证码 | easy-captcha 1.6.2 | 图形验证码 |
| API 文档 | springdoc-openapi 2.5.0 | Swagger UI |
| 构建工具 | Maven | 依赖管理与打包 |
| 容器化 | Docker | 多阶段构建 |
| CI/CD | GitHub Actions | 自动构建推送镜像到阿里云 ACR |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+（或使用 H2 内存数据库）

### 本地运行

```bash
# 1. 克隆项目
git clone <repo-url>
cd ContactHub

# 2. 创建数据库（可选，JPA 会自动建表）
mysql -u root -p -e "CREATE DATABASE contacthub DEFAULT CHARACTER SET utf8mb4;"

# 3. 配置数据库连接（编辑已有的配置文件）
# application-local.yml 已在项目中（gitignored），直接编辑数据库连接信息

# 4. 构建并运行
mvn clean package -DskipTests
java -jar target/addressbook.jar
```

启动后访问 http://localhost:8087

### 默认管理员账号

在 `application.yml` 中配置（默认值）：

| 配置项 | 默认值 |
|:---|:---|
| `admin.username` | admin |
| `admin.password` | admin123 |

应用启动时自动创建管理员账号，无需手动初始化。

### 使用 Maven 直接运行

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Docker 部署

```bash
# 构建镜像
docker build -f docker/Dockerfile -t contacthub:latest .

# 运行容器
docker run -d \
  -p 8087:8087 \
  -e MYSQL_HOST=your-mysql-host \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DB=contacthub \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=your-password \
  --name contacthub \
  contacthub:latest
```

### GitHub Actions CI/CD

推送到 `main`/`master` 分支时自动触发：

1. 运行单元测试
2. 构建 Docker 镜像（标签：`latest` + Git 短哈希）
3. 推送到阿里云容器镜像服务（ACR）

需要在 GitHub Secrets 中配置：

| Secret | 说明 |
|:---|:---|
| `ACR_KEY` | 阿里云 AccessKey ID |
| `ACR_SECRET` | 阿里云 AccessKey Secret |
| `ACR_NAMESPACE` | ACR 命名空间 |

## 项目结构

```
src/main/java/com/yabo/addressbook/
├── config/             # Security、WebMvc、Admin 初始化等配置
├── controller/         # REST API 控制器（11 个）
├── dto/                # 请求/响应 DTO（ContactDTO、ApiResult、PageDTO、GroupRequest）
├── entity/             # JPA 实体（User、Contact、ContactGroup）
├── exception/          # 自定义异常 + 全局异常处理器
├── repository/         # Spring Data JPA Repository（3 个）
├── security/           # IP 锁定过滤器、认证成功/失败处理器
├── service/            # 业务逻辑层（7 个）
└── util/               # 工具类（Excel、图片、校验、IP）

src/main/resources/
├── static/             # 前端页面（login、register、contacts、recycle 等 HTML）及静态资源
├── messages.properties # 国际化默认（中文）
├── messages_en.properties
├── messages_zh.properties
├── application.yml     # 主配置
├── application-dev.yml # 开发环境
├── application-prod.yml# 生产环境
└── logback-spring.xml  # 日志配置
```

## 数据库设计

三张核心表，通过外键关联：

```
users (用户表)
  ├── contact_groups (分组表)     1:N  一个用户拥有多个分组
  └── contacts (联系人表)         1:N  一个用户拥有多个联系人

contact_groups (分组表)
  └── contacts (联系人表)         1:N  一个分组拥有多个联系人
```

| 表名 | 说明 | 核心字段 |
|:---|:---|:---|
| `users` | 用户表 | id, username(UNIQUE), password(BCrypt), role(USER/ADMIN), enabled |
| `contact_groups` | 分组表 | id, name, user_id(FK), is_default, sort_order |
| `contacts` | 联系人表 | id, user_id(FK), group_id(FK), name, family_name, given_name, uid(UUID), gender, phone_*, email, company, job_title, province/city/district, birthday, is_deleted, deleted_at |

索引：`contacts` 表对 `user_id`、`group_id`、`is_deleted`、`name`、`phone_mobile` 建立索引，组合索引 `(province, city, district)` 加速地域查询。

## 配置说明

关键配置项（`application.yml`）：

```yaml
server:
  port: 8087
  servlet:
    session:
      timeout: 30m          # Session 超时时间

spring:
  jpa:
    hibernate:
      ddl-auto: update      # 自动建表（JPA 根据实体自动创建/更新表结构）
  servlet:
    multipart:
      max-file-size: 10MB   # 文件上传大小限制

admin:
  username: admin           # 管理员用户名
  password: admin123        # 管理员密码

upload:
  avatar:
    max-size: 2097152       # 头像最大 2MB
    allowed-types: image/jpeg,image/png
  import:
    max-size: 10485760      # 导入文件最大 10MB
    max-rows: 5000          # 导入最大行数

captcha:
  enabled: false            # 验证码开关
```

## API 接口

| 端点 | 方法 | 功能 | 权限 |
|:---|:---|:---|:---|
| `/login.html` | GET | 登录页面 | 匿名 |
| `/login` | POST | 登录认证 | 匿名 |
| `/logout` | POST | 退出登录 | 登录用户 |
| `/api/v1/register/check-username` | GET | 校验用户名唯一性 | 匿名 |
| `/api/v1/register/random-nickname` | GET | 获取随机昵称 | 匿名 |
| `/api/v1/register` | POST | 用户注册 | 匿名 |
| `/api/v1/contacts/data` | GET | 分页查询联系人 | 登录用户 |
| `/api/v1/contacts` | POST | 新增联系人 | 登录用户 |
| `/api/v1/contacts/{id}` | PUT | 编辑联系人 | 归属人/管理员 |
| `/api/v1/contacts/{id}` | DELETE | 软删除联系人 | 归属人/管理员 |
| `/api/v1/contacts/{id}/avatar` | POST | 上传联系人头像 | 归属人/管理员 |
| `/api/v1/contacts/recycle/data` | GET | 回收站列表 | 登录用户 |
| `/api/v1/contacts/{id}/restore` | PUT | 恢复联系人 | 归属人/管理员 |
| `/api/v1/contacts/{id}/permanent` | DELETE | 彻底删除 | 归属人/管理员 |
| `/api/v1/contacts/recycle` | DELETE | 清空回收站 | 登录用户 |
| `/api/v1/contacts/export` | GET | 导出 Excel（按筛选） | 登录用户 |
| `/api/v1/contacts/export/all` | GET | 导出全部 | 登录用户 |
| `/api/v1/contacts/export/selected` | POST | 选中导出 | 登录用户 |
| `/api/v1/groups` | GET | 分组列表 | 登录用户 |
| `/api/v1/groups/check-name` | GET | 校验分组名唯一性 | 登录用户 |
| `/api/v1/groups` | POST | 新建分组 | 登录用户 |
| `/api/v1/groups/{id}` | PUT | 编辑分组 | 归属人/管理员 |
| `/api/v1/groups/{id}` | DELETE | 删除分组 | 归属人/管理员 |
| `/api/v1/admin/users/data` | GET | 用户列表 | 管理员 |
| `/api/v1/admin/users` | POST | 创建用户 | 管理员 |
| `/api/v1/admin/users/{id}` | PUT | 更新用户 | 管理员 |
| `/api/v1/admin/users/{id}` | DELETE | 删除用户 | 管理员 |
| `/api/v1/admin/users/{id}/reset-password` | PUT | 重置密码 | 管理员 |
| `/api/v1/user/current` | GET | 获取当前用户信息 | 登录用户 |
| `/api/v1/user/avatar` | POST/GET | 上传/查看用户头像 | 登录用户 |
| `/api/v1/user/{id}/avatar` | GET | 查看指定用户头像 | 登录用户 |
| `/api/v1/user/profile/update` | PUT | 更新个人信息 | 登录用户 |
| `/api/v1/user/profile/password` | POST | 修改密码 | 登录用户 |
| `/api/v1/i18n` | GET | 获取翻译文本 | 匿名 |
| `/captcha` | GET | 生成验证码图片 | 匿名 |
| `/captcha/status` | GET | 验证码开关状态 | 匿名 |
| `/csrf` | GET | 获取 CSRF Token | 匿名 |
| `/swagger-ui.html` | GET | API 文档 | 匿名 |

## 测试

```bash
# 运行全部测试
mvn test

# 运行指定测试类
mvn test -Dtest=ContactServiceTest
mvn test -Dtest=ContactControllerTest
```

测试覆盖：20 个测试类，覆盖 Controller、Service、Repository 各层，使用 H2 内存数据库 + MockMvc。

## License

MIT
