# Tasks

- [x] Task 1: 创建 Maven 项目骨架与基础配置
  - [x] 创建 pom.xml（Spring Boot 3.x、JPA、Security、Thymeleaf、Bootstrap5、POI、Thumbnailator）
  - [x] 创建 application.yml（数据库、管理员账号、文件上传配置）
  - [x] 创建包目录结构（config/controller/dto/entity/exception/repository/service/util）
  - [x] 创建 AddressbookApplication.java 启动类

- [x] Task 2: 实现 JPA 实体类与 Repository
  - [x] 创建 User 实体（users 表）
  - [x] 创建 ContactGroup 实体（contact_groups 表）
  - [x] 创建 Contact 实体（contacts 表）
  - [x] 创建对应的 JPA Repository 接口

- [x] Task 3: 实现 Spring Security 安全配置
  - [x] 创建 SecurityConfig（HttpSecurity 配置，CSRF，表单登录，Session 管理）
  - [x] 创建 UserDetailsServiceImpl 加载用户
  - [x] 创建登录/注册页面匿名访问配置

- [x] Task 4: 实现用户注册与管理员初始化
  - [x] 创建 UserService（注册、管理员初始化 CommandLineRunner）
  - [x] 创建 RegisterController（GET/POST /register）
  - [x] 创建注册页面模板 register.html

- [x] Task 5: 实现分组管理
  - [x] 创建 GroupService（CRUD，默认分组保护，联系人迁移）
  - [x] 创建 GroupController（REST 接口）
  - [x] 实现分组管理相关模态框

- [x] Task 6: 实现联系人管理与多条件查询
  - [x] 创建 ContactService（CRUD，JPA Specification 动态查询，软删除/恢复）
  - [x] 创建 ContactController（页面与 REST 接口）
  - [x] 创建联系人主页模板 contacts.html
  - [x] 实现回收站页面模板 recycle.html

- [x] Task 7: 实现头像上传
  - [x] 创建 AvatarService（Thumbnailator 缩放 200x200）
  - [x] 创建 AvatarController（上传/查看接口）
  - [x] 实现头像上传模态框

- [x] Task 8: 实现 Excel 导入导出
  - [x] 创建 ExcelUtil（POI 读写工具类）
  - [x] 在 ContactController 中添加导入/导出接口
  - [x] 实现导入模态框和导出按钮

- [x] Task 9: 实现管理员用户管理
  - [x] 创建 AdminService（用户管理）
  - [x] 创建 AdminController
  - [x] 创建管理员页面模板 admin-users.html

- [x] Task 10: 实现全局异常处理与 DTO 校验
  - [x] 创建 BusinessException 和 GlobalExceptionHandler
  - [x] 创建联系人 DTO 及自定义校验注解（至少一个联系电话）
  - [x] 实现统一 JSON 响应格式

- [x] Task 11: 实现登录页面模板
  - [x] 创建 login.html（Bootstrap 5 响应式）

# Task Dependencies
- [Task 1] 无依赖
- [Task 2] 依赖 [Task 1]
- [Task 3] 依赖 [Task 1, Task 2]
- [Task 4] 依赖 [Task 3]
- [Task 5] 依赖 [Task 2, Task 3]
- [Task 6] 依赖 [Task 2, Task 3, Task 5]
- [Task 7] 依赖 [Task 2, Task 3]
- [Task 8] 依赖 [Task 2, Task 3, Task 6]
- [Task 9] 依赖 [Task 2, Task 3]
- [Task 10] 依赖 [Task 1]
- [Task 11] 依赖 [Task 1]