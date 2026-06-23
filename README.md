# 人生大事服务管理系统

课程设计级 Vue 3 + Spring Boot 3 全栈项目，严格围绕 `life_event_service` 数据库脚本中的 8 张核心表实现。

## 数据库初始化

1. 安装并启动 MySQL 8.0。
2. 执行当前目录下的 `人生大事服务管理系统数据库脚本.sql`。
3. 后端默认连接：
   - 数据库：`life_event_service`
   - 用户名：`root`
   - 密码：`123456`

如本机密码不同，可启动前设置环境变量 `DB_USERNAME`、`DB_PASSWORD`，或修改 `backend/src/main/resources/application.yml`。

## 后端启动

```bash
cd backend
mvn spring-boot:run
```

后端端口：`http://localhost:8080`。

未登录访问 `http://localhost:8080/api/dashboard/summary` 应返回 `401`。

## 前端启动

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

前端端口：`http://localhost:5173`。

## 演示账号

- 管理员：`admin01` / `hash_admin01`
- 接待员：`recv01` / `hash_recv01`
- 财务：`fin01` / `hash_fin01`

## 核心演示闭环

1. 登录系统进入统计看板。
2. 新增家属、逝者档案。
3. 在租赁办理中选择家属、逝者、空置墓穴和员工，创建租赁订单。
4. 创建成功后，墓穴状态会从 `empty` 更新为 `rented`。
5. 在缴费管理中为租赁订单登记缴费，月度营收图表会体现新增收入。
6. 在祭扫预约中创建预约，并可执行核销或取消。

