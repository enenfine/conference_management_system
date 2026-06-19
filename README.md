# 会议管理系统

## 1. 项目简介

会议管理系统是一个基于 Spring Boot + Vue 的前后端分离项目，主要用于会议创建、会议室查看、参会人员管理、会议文件上传下载以及后台基础数据管理。

本项目已对原有代码进行了精简整理，只保留当前项目中已经形成完整业务闭环、可以实际运行的功能，删除了未完整实现或没有真实入口的功能模块，例如会议提醒、通知、会议记录、统计分析、反馈评价等内容。

## 2. 技术栈

### 后端

- Java 8
- Spring Boot
- Spring MVC
- MyBatis-Plus
- MySQL
- Lombok
- Knife4j
- Hutool

### 前端

- Vue 3
- Vite
- TypeScript
- Vue Router
- Axios
- Element Plus

### 数据库

- MySQL

## 3. 项目结构

```text
conference_management_system_cleaned
├── cms_backend              后端 Spring Boot 项目
│   ├── src/main/java        Java 源代码
│   ├── src/main/resources   配置文件与 Mapper XML
│   ├── sql                  数据库脚本
│   └── pom.xml              Maven 配置文件
│
├── admin_frontend           管理端 Vue 项目
│   ├── src/api              接口请求文件
│   ├── src/views            页面文件
│   ├── src/router           路由配置
│   └── package.json         前端依赖配置
│
├── user_frontend            用户端 Vue 项目
│   ├── src/api              接口请求文件
│   ├── src/views            页面文件
│   ├── src/router           路由配置
│   └── package.json         前端依赖配置
│
└── init.sql                 数据库初始化脚本
```

## 4. 已保留功能

### 4.1 登录注册功能

- 用户登录
- 用户注册
- 获取当前登录用户
- 退出登录
- 管理员与普通用户角色区分

### 4.2 管理端功能

管理端主要用于系统基础数据维护，保留功能如下：

- 用户管理
    - 用户查询
    - 新增用户
    - 修改用户
    - 删除用户
    - 用户角色管理

- 会议管理
    - 会议列表查询
    - 新增会议
    - 修改会议
    - 删除会议
    - 查看会议基础信息

- 会议类型管理
    - 会议类型查询
    - 新增会议类型
    - 修改会议类型
    - 删除会议类型

- 会议室管理
    - 会议室查询
    - 新增会议室
    - 修改会议室
    - 删除会议室
    - 查看会议室基础信息

- 会议文件管理
    - 文件列表查看
    - 文件上传
    - 文件下载或打开
    - 文件删除

### 4.3 用户端功能

用户端主要面向普通用户使用，保留功能如下：

- 会议管理
    - 查看自己创建的会议
    - 查看自己参与的会议
    - 新增会议
    - 编辑会议
    - 取消会议
    - 查看会议详情
    - 选择会议室
    - 选择参会人员

- 会议室查看
    - 查看会议室列表
    - 查看会议室详情
    - 根据会议室情况辅助选择会议场地

- 会议文件
    - 上传会议附件
    - 查看会议文件
    - 下载或打开会议文件

## 5. 已删除功能

本项目已删除以下未真正实现、未形成完整业务闭环或当前页面不再使用的功能：

- 会议提醒功能
- 提醒类型功能
- 通知功能
- 会议记录 / 会议纪要功能
- 独立会议日程模块
- 统计分析功能
- 反馈评价功能
- 设备类型功能
- 会议室设备明细功能
- 博客 / 帖子演示功能
- 访客演示功能
- 二维码签到相关残留代码
- 没有前端入口或后端不完整的接口
- 数据库中未被当前代码真实使用的表

## 6. 数据库说明

### 6.1 数据库名称

默认数据库名称为：

```text
z_cms_system
```

### 6.2 保留的数据表

```text
user
meeting_type
meeting_room
meeting
meeting_participant
file_type
meeting_file
```

### 6.3 初始化数据库

在 MySQL 中执行项目根目录下的 `init.sql` 文件：

```bash
mysql -uroot -p < init.sql
```

也可以使用 Navicat、DataGrip、DBeaver 等数据库工具打开 `init.sql` 并执行。

## 7. 后端启动说明

### 7.1 修改数据库配置

打开后端配置文件：

```text
cms_backend/src/main/resources/application.yml
```

根据本地 MySQL 环境修改数据库账号、密码和数据库地址：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/z_cms_system
    username: root
    password: 123456
```

### 7.2 启动后端项目

进入后端目录：

```bash
cd cms_backend
```

安装依赖并启动：

```bash
mvn clean package
mvn spring-boot:run
```

后端默认启动地址：

```text
http://localhost:8080/api
```

Knife4j 接口文档地址：

```text
http://localhost:8080/api/doc.html
```

## 8. 前端启动说明

### 8.1 启动管理端

进入管理端目录：

```bash
cd admin_frontend
```

安装依赖：

```bash
npm install
```

启动项目：

```bash
npm run dev
```

### 8.2 启动用户端

进入用户端目录：

```bash
cd user_frontend
```

安装依赖：

```bash
npm install
```

启动项目：

```bash
npm run dev
```

前端启动后，根据终端输出的地址在浏览器中访问即可。

## 9. 默认账号

如果已执行项目提供的初始化 SQL，可以使用以下测试账号登录。

### 管理员账号

```text
账号：admin
密码：12345678
```

### 普通用户账号

```text
账号：user
密码：12345678
```

其他测试用户：

```text
zhangsan / 12345678
lisi / 12345678
wangwu / 12345678
```

## 10. 常见问题

### 10.1 Java 版本问题

本项目使用 Java 8，请确保 `pom.xml` 中配置为：

```xml
<java.version>1.8</java.version>
```

如果代码中出现 `Map.of()`、`List.of()`、`Set.of()` 等写法，需要改为 Java 8 支持的写法。

### 10.2 数据库连接失败

请检查：

- MySQL 是否已启动
- 数据库 `z_cms_system` 是否已创建
- `init.sql` 是否已执行
- `application.yml` 中的用户名和密码是否正确
- 后端端口 `8080` 是否被占用

### 10.3 前端接口请求失败

请检查：

- 后端是否已经启动
- 后端访问地址是否为 `http://localhost:8080/api`
- 前端请求代理配置是否正确
- 浏览器控制台是否存在跨域或接口地址错误

### 10.4 文件上传失败

请检查：

- 后端上传接口是否正常
- 文件大小是否超过限制
- `application.yml` 中是否配置了上传大小限制
- 上传目录是否存在访问权限问题

## 11. 构建命令

### 后端构建

```bash
cd cms_backend
mvn clean package
```

### 管理端构建

```bash
cd admin_frontend
npm install
npm run build
```

### 用户端构建

```bash
cd user_frontend
npm install
npm run build
```

