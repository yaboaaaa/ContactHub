# 检查清单

## 项目基础设施
- [x] pom.xml 包含所有必需依赖（Spring Boot 3.x, JPA, Security, Thymeleaf, POI, Thumbnailator, H2/MySQL）
- [x] application.yml 正确配置数据库、管理员账号、文件上传限制
- [x] 包目录结构完整（config/controller/dto/entity/exception/repository/service/util）
- [x] 启动类正确配置

## JPA 实体与 Repository
- [x] User 实体包含所有字段（id, username, password, email, role, enabled, avatar_data, avatar_url, avatar_content_type, created_at, updated_at）
- [x] ContactGroup 实体包含所有字段（id, name, user_id, is_default, sort_order, created_at）
- [x] Contact 实体包含所有字段（id, user_id, group_id, name, gender, phones, email, company, job_title, address fields, birthday, notes, is_deleted, deleted_at, timestamps）
- [x] 每个实体有对应的 Repository 接口

## Spring Security
- [x] SecurityConfig 配置表单登录、CSRF、Session 管理
- [x] UserDetailsServiceImpl 正确加载用户信息
- [x] 登录页/注册页/静态资源允许匿名访问，其他需认证
- [x] BCrypt 密码加密

## 用户注册与管理员初始化
- [x] 注册接口校验用户名唯一性
- [x] 注册时自动创建默认分组
- [x] 启动时自动初始化管理员账号
- [x] 注册页面模板正常工作

## 分组管理
- [x] 分组 CRUD 接口正确实现
- [x] 默认分组不可删除（前后端双重校验）
- [x] 删除分组时联系人自动迁移到默认分组
- [x] 新增分组校验同名冲突

## 联系人管理
- [x] 联系人 CRUD 接口正确实现
- [x] 新增联系人校验姓名必填和至少一个联系电话
- [x] 软删除正确标记 is_deleted 和 deleted_at
- [x] JPA Specification 动态组合多条件查询
- [x] 回收站页面展示已删除联系人
- [x] 恢复联系人取消软删除标记
- [x] 彻底删除物理删除记录
- [x] 清空回收站物理删除所有已删除联系人

## 头像上传
- [x] 仅允许 jpeg/png，大小不超过 2MB
- [x] Thumbnailator 缩放为 200x200
- [x] 头像查看接口正确返回二进制数据

## Excel 导入导出
- [x] 导出生成正确格式的 XLSX 文件
- [x] 导出表头包含所有联系人字段
- [x] 导入逐行解析，姓名必填校验
- [x] 导入时自动创建不存在的分组
- [x] 返回导入结果（成功条数 + 失败详情）

## 管理员功能
- [x] 管理员可查看所有用户列表
- [x] 管理员可禁用/启用用户
- [x] 管理员可删除用户（级联删除所有数据）
- [x] 管理员不可创建/修改 ADMIN 角色

## 全局异常与校验
- [x] GlobalExceptionHandler 处理各类异常
- [x] DTO 使用 Bean Validation 注解
- [x] 自定义校验注解"至少一个联系电话"
- [x] 多表操作使用 @Transactional

## 前端页面
- [x] 登录页面响应式（Bootstrap 5）
- [x] 注册页面响应式
- [x] 联系人主页包含分组树、搜索栏、联系人表格
- [x] 模态框实现新增/编辑联系人
- [x] 回收站页面
- [x] 管理员用户管理页面
- [x] 移动端适配（分组折叠、卡片视图）