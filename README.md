# 青年品质租房管理平台 (Youth Rental Management System)

基于 Spring Boot + Vue3 前后端分离的租房管理系统，实现房源发布、租客签约、订单支付、后台管理等核心功能。

## 技术栈
- **后端**：Java 17 + Spring Boot + MyBatis-Plus + MySQL
- **前端**：Vue 3 + Element Plus + Axios
- **工具**：Maven + Git + Redis（缓存）

## 核心功能
- 用户注册/登录（JWT 鉴权）、角色权限控制
- 房源信息管理、多条件筛选与分页查询
- 租房订单创建、支付回调、订单状态流转
- 管理员后台数据统计与用户管理

## 快速启动
1. 导入 `sql/` 下的数据库脚本到 MySQL
2. 修改 `application.yml` 中数据库连接信息
3. 后端：`mvn spring-boot:run`
4. 前端：`npm install && npm run dev`

## 项目亮点
- 支付回调采用状态机控制订单流转，保证数据一致性
- 高频查询字段建立联合索引，优化接口响应
- 统一全局异常处理与结果封装，代码规范清晰
