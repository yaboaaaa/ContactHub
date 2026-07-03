# 多用户通讯录系统 Spec

## Why

根据 `设计方案.md` 构建一个完整的在线通讯录服务，支持多用户、内置管理员、导入/导出/回收站等功能。系统采用 Spring Boot 3.x + Spring Data JPA + Thymeleaf + Bootstrap 5 技术栈。

## What Changes

- 创建完整的 Maven 项目结构（pom.xml、目录结构）
- 实现 JPA 实体类（User、ContactGroup、Contact）
- 实现 Spring Security 配置与认证
- 实现用户注册、登录、管理员初始化
- 实现分组 CRUD（含默认分组保护）
- 实现联系人 CRUD、软删除、回收站
- 实现多条件组合查询（JPA Specification）
- 实现头像上传与缩放（Thumbnailator）
- 实现 Excel 导入导出（Apache POI）
- 实现全局异常处理与校验
- 实现 Thymeleaf 前端页面（响应式 Bootstrap 5）
- 实现管理员用户管理

## Impact

- Affected specs: 全部
- Affected code: 整个项目从零创建

## ADDED Requirements

### 1. 项目基础设施

系统 SHALL 使用 Maven 构建，Spring Boot 3.x 作为基础框架。

- **WHEN** 运行 `mvn clean package`
- **THEN** 生成可执行 JAR 包 `addressbook.jar`

### 2. 用户认证与注册

#### Scenario: 用户注册
- **WHEN** 匿名用户访问 `/register` 并提交合法表单
- **THEN** 系统创建用户（BCrypt 加密密码），角色为 USER
- **AND** 自动创建该用户的默认分组
- **AND** 重定向到登录页

#### Scenario: 用户登录
- **WHEN** 用户提交正确凭证到 `/login`
- **THEN** 系统创建 Session，重定向到联系人主页

#### Scenario: 管理员自动初始化
- **WHEN** 应用启动
- **THEN** 系统检查配置的管理员账号是否存在，不存在则自动创建

### 3. 联系人管理

#### Scenario: 新增联系人
- **WHEN** 用户提交联系人表单
- **THEN** 系统校验姓名必填且至少一个联系电话
- **AND** 若未指定分组则归入默认分组
- **AND** 保存联系人，返回成功

#### Scenario: 编辑联系人
- **WHEN** 归属用户或管理员提交编辑
- **THEN** 系统更新联系人信息

#### Scenario: 软删除联系人
- **WHEN** 归属用户或管理员执行删除
- **THEN** 系统将 `is_deleted` 置为 true，记录 `deleted_at`

#### Scenario: 多条件查询
- **WHEN** 用户提交查询参数（keyword/phone/province/city/district/company/jobTitle/groupId）
- **THEN** 系统使用 JPA Specification 动态组合查询条件
- **AND** 默认附加 `user_id = currentUser AND is_deleted = false`

### 4. 分组管理

#### Scenario: 新增分组
- **WHEN** 用户提交新分组名称
- **THEN** 系统校验该用户下无同名分组
- **AND** 创建分组

#### Scenario: 删除分组（非默认）
- **WHEN** 用户删除非默认分组
- **THEN** 系统将该分组下未删除联系人移入默认分组
- **AND** 物理删除分组

#### Scenario: 保护默认分组
- **WHEN** 用户尝试删除默认分组
- **THEN** 系统抛出业务异常，拒绝操作

### 5. 回收站

#### Scenario: 查看回收站
- **WHEN** 用户访问回收站页面
- **THEN** 系统展示 `is_deleted = true` 的联系人列表

#### Scenario: 恢复联系人
- **WHEN** 用户执行恢复操作
- **THEN** 系统将 `is_deleted` 置为 false，清除 `deleted_at`

#### Scenario: 彻底删除/清空
- **WHEN** 用户执行彻底删除或清空回收站
- **THEN** 系统物理删除相关联系人记录

### 6. 头像上传

#### Scenario: 上传头像
- **WHEN** 用户上传图片文件（jpeg/png，≤2MB）
- **THEN** 系统缩放为 200×200 像素
- **AND** 存入数据库 `avatar_data` 字段

#### Scenario: 查看头像
- **WHEN** 请求头像
- **THEN** 系统优先返回 CDN URL，否则返回数据库存储的二进制数据

### 7. Excel 导入导出

#### Scenario: 导出联系人
- **WHEN** 用户请求导出
- **THEN** 系统生成 XLSX 文件，包含全部联系人字段
- **AND** 响应头设置 `Content-Disposition: attachment`

#### Scenario: 导入联系人
- **WHEN** 用户上传 Excel 文件
- **THEN** 系统逐行解析，姓名必填
- **AND** 分组名不存在则自动创建
- **AND** 返回导入结果（成功条数 + 失败详情）

### 8. 管理员功能

#### Scenario: 管理员查看用户列表
- **WHEN** 管理员访问 `/admin/users`
- **THEN** 系统展示所有用户列表

#### Scenario: 管理员操作用户
- **WHEN** 管理员执行增/删/改/禁用用户
- **THEN** 系统执行对应操作（删除用户时级联删除其所有数据）

### 9. 全局异常与校验

- 系统 SHALL 使用 `@ControllerAdvice` 统一处理异常
- 系统 SHALL 使用 Bean Validation 校验 DTO
- 系统 SHALL 自定义校验器确保"至少一个联系电话"
- 系统 SHALL 使用 `@Transactional` 保障多表操作原子性

### 10. 前端页面

- 系统 SHALL 提供响应式页面（Bootstrap 5）
- 系统 SHALL 包含：登录/注册页、联系人主页、回收站页、管理员用户管理页
- 系统 SHALL 使用模态框实现新增/编辑操作
- 系统 SHALL 移动端适配（分组树折叠为下拉，联系人表格变为卡片）