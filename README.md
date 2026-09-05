# 易栈·二手交易平台 — 完整代码使用说明

本目录是可直接运行的完整项目，已修复《项目1.md》中的全部问题。

## 目录结构

```
yizhan-platform/
├── docker-compose.yml      # Docker 版编排（有 Docker 用这个）
├── podman-compose.yml      # Podman 版编排（RHEL 10 原生，带 :Z 标签）
├── backend/                # Spring Boot 3.2 后端（全部源码）
│   ├── Dockerfile          # 多阶段构建
│   ├── pom.xml
│   └── src/main/...
├── frontend/               # 6 个前端页面 + common.js + 内置商品占位图
│   ├── common.js           # 修复重写版（原项目1.md 中的已损坏）
│   ├── login.html / index.html / detail.html
│   ├── publish.html / orders.html / admin.html
│   └── static/img/         # 12 张内置渐变商品图（离线可用，不依赖外网）
├── nginx/conf.d/default.conf   # 修复了 try_files 与图片代理
└── mysql/
    ├── init/01-init.sql    # 建表（去掉了无效的 BCrypt 哈希）
    ├── master.cnf / slave.cnf  # 进阶：主从复制
```

## 已修复的问题（对照项目1.md）

| 问题 | 修复方式 |
|---|---|
| 后端 6 个 Controller 无代码 | 全部补齐：实体、Mapper、Service、Controller、JWT、MinIO、异常处理 |
| 前端 5 个页面未写 | 补齐 6 个页面（新增 orders.html 补齐订单闭环） |
| common.js 模板字符串损坏 | 整体重写，并统一 Result 字段（message） |
| BCrypt 哈希无效导致 admin 无法登录 | 改为后端启动时 DataInitializer 自动创建，**admin / root123456** |
| jjwt 缺 impl/jackson 依赖 | pom.xml 已补齐三件套 |
| MySQL 8.4 已移除的参数 | 去掉 `--default-authentication-plugin`，改用字符集参数 |
| MinIO bucket 无人创建 | MinioConfig 启动时自动创建 `yizhan-goods` |
| 图片访问需暴露 9000 端口 | Nginx 增加 `/minio-files/` 同源代理 |
| MySQL 未就绪后端先启动 | docker-compose 给 MySQL 加 healthcheck，backend 等待就绪 |

## 部署步骤（RHEL 10.2 虚拟机）

```bash
# 1. 把整个目录拷到虚拟机（如 /root/yizhan-platform）
# 2. 安装容器环境（二选一）
sudo dnf install -y podman podman-compose        # Podman（RHEL 原生）
# 或按项目1.md 第二节安装 Docker

# 3. 构建后端镜像（镜像内编译，宿主机不需要 JDK/Maven）
cd yizhan-platform/backend
podman build -t yizhan-backend:1.0 .             # Docker 则: docker build -t yizhan-backend:1.0 .

# 4. 启动整套服务
cd ..
podman-compose up -d                              # Docker 则: docker compose up -d

# 5. 放行端口
sudo firewall-cmd --permanent --add-port=8080/tcp && sudo firewall-cmd --reload
# Docker 版映射的是 80 端口: --add-service=http
```

浏览器访问 `http://<虚拟机IP>:8080`（Docker 版为 80 端口）。

## 验证流程

首次启动后端会自动写入演示数据（幂等，只在商品表为空时执行）：
**3 个普通用户 / 12 件已上架商品 / 8 条评论 / 2 笔订单**。

可用账号（密码统一 `root123456`）：

| 账号 | 角色 |
|---|---|
| admin | 管理员 |
| zhangsan / lisi / wangwu | 普通用户（带在售商品） |

演示步骤：

1. 用 **admin / root123456** 登录，进管理后台看统计数据（此时已有种子数据）
2. 用 **zhangsan / root123456** 登录，浏览首页的 12 件演示商品
3. 自己发布一件商品（带图片上传）→ 切回 admin 审核上架
4. 下单购买 → 我的订单 → 确认收货
5. 商品详情页发表评论

> 注意：本次更新新增了后端代码（SeedDataInitializer），
> 虚拟机上需要重新构建后端镜像并重建容器才会生效：
> `cd backend && docker build -t yizhan-backend:1.0 . && cd .. && docker compose up -d --build`
> （前端改动只需覆盖 frontend 目录后浏览器强刷，无需重建）

## 接口清单（与文档一致）

- `POST /api/user/register`、`POST /api/user/login`、`GET /api/user/me`
- `POST /api/goods/add`、`GET /api/goods/list?keyword=`、`GET /api/goods/{id}`
- `POST /api/order/create`、`GET /api/order/list`、`POST /api/order/confirm`
- `POST /api/comment/add`、`GET /api/comment/list?goodsId=`
- `POST /api/upload`（multipart/form-data）
- `GET /api/admin/goods`、`POST /api/admin/goods/status`、`GET /api/admin/users`、`GET /api/admin/stats`

统一返回：`{code: 200, message: "success", data: ...}`；未登录返回 401。
