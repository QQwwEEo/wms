# 🚀 Premium WMS 智能仓库管理系统 (v1.0.1)

这是一个基于 **Spring Boot 3.x** + **Vue 3** + **Element Plus** + **ECharts** 开发的高颜值、响应式智能仓库管理系统 (WMS)。本系统提供了完备的物资流通管理、工作流自动审批、物资状态可视化、用户多角色权限划分，以及极具现代感的**多主题/暗黑模式**切换功能。

---

## 📸 核心功能特色

### 1. 🎨 全局多主题与沉浸式暗黑模式
* **五套精选主题**：默认靛蓝 (Default Indigo)、翡翠绿色 (Emerald Green)、活力橙色 (Amber Orange)、深邃蓝色 (Ocean Blue) 和暗黑夜色 (Midnight Dark)。
* **全局变量覆盖**：侧边栏、顶部栏、卡片容器、字体及 Element Plus 核心组件（输入框、选择器、表格、分页、弹窗等）全部采用原生 CSS 变量设计，主题切换无缝响应。
* **无感持久化**：使用 `localStorage` 持久化保存主题偏好，并在 DOM 挂载的最早期将偏好渲染至 `html[data-theme]`，完全杜绝了页面刷新时的“白光闪烁”。

### 2. 📊 ECharts 动态数据可视化看板
* **物资分类统计（环形饼图）**：实时加载系统内各品类物资的数量分布，并支持鼠标悬停聚焦动效。
* **出入库近7日流量趋势（渐变折线图）**：自适应生成近 7 天的时间轴，通过 Smooth 平滑曲线展示出入库流量，并使用 `LinearGradient` 线性渐变色彩，分别以绿（入库）、红（出库）双色填充阴影区域。
* **视口自适应 (ResizeObserver)**：监听整个页面及侧边栏的展开/折叠，实时重绘 ECharts 图表尺寸，保证多端排版不乱。

### 3. ⚙️ 工作流自动化与自动库存扣减
* **物资申请自动出库**：普通用户提交申请，管理员审批通过（`approved`）时，系统**自动扣减库存**并**自动生成一条出库流水日志**。审批时会严格校验当前库存是否充足。
* **物资归还自动入库**：用户提交归还申请，管理员审批通过时，系统**自动增加库存**并**自动生成一条入库流水日志**。

### 4. ⚠️ 低库存智能预警
* 支持对每种物资独立配置最低安全库存量 (`min_stock`)。
* 当库存数量低于或等于最低预警值时，表格中的库存数值将自动渲染为**高亮警示红色**，便于管理员及时采购补仓。

### 5. 🔒 企业级安全加固 & JWT 动态无感刷新
* **高强度密码校验**：注册及更新用户时，密码长度必须不小于 6 位，且必须同时包含字母和数字。
* **JWT 动态无感刷新**：每次请求时，过滤器会自动检测 Token 剩余寿命，若**小于 30 分钟**，则在 HTTP 响应头中注入 `Authorization-Refresh` 进行新 Token 的静默下发，前端自动更新，实现用户登录“无感续期”。

---

## 📂 项目文件结构

```
nifty-chandrasekhar/
├── README.md                          # 项目中文文档与搭建指南
├── .gitignore                         # Git 忽略文件规则
├── backend/                           # Spring Boot 3.x 后端项目
│   ├── pom.xml                        # Maven 依赖管理
│   └── src/main/
│       ├── java/com/wms/
│       │   ├── WmsApplication.java    # 后端启动入口
│       │   ├── common/                # 统一返回结果封装 (Result, PageResult)
│       │   ├── config/                # 安全与系统配置 (SecurityConfig, CorsConfig 等)
│       │   ├── controller/            # REST APIs 控制层
│       │   ├── entity/                # MyBatis-Plus 数据库实体映射
│       │   ├── mapper/                # MyBatis-Plus Mapper 接口
│       │   ├── service/               # 业务逻辑服务层 (及 impl 实现)
│       │   ├── utils/                 # 工具类 (JwtUtils)
│       │   └── vo/                    # 响应视图对象 (LoginVO)
│       └── resources/
│           ├── application.yml        # 系统核心配置文件
│           ├── wms.sql                # 数据库核心初始化脚本
│           └── wms_fix.sql            # 数据库种子和乱码修复脚本
│
└── frontend/                          # Vue 3 前端项目
    ├── index.html                     # SPA 入口 HTML
    ├── package.json                   # 前端依赖配置
    ├── vite.config.js                 # Vite 配置文件
    └── src/
        ├── main.js                    # 前端初始化主入口
        ├── App.vue                    # 根组件
        ├── api/                       # API 统一管理层 (index.js)
        ├── assets/                    # 样式资源目录 (index.css 设计系统)
        ├── router/                    # 路由守卫与角色动态加载 (index.js)
        ├── store/                     # Pinia 状态管理 (user.js)
        ├── utils/                     # 辅助工具类 (Axios 拦截器 request.js)
        └── views/                     # 前端功能页面
            ├── Login.vue              # 渐变毛玻璃登录页
            ├── Layout.vue             # 主体框架 (含菜单栏、顶部更换主题)
            ├── Dashboard.vue          # 数据可视化工作台
            ├── Profile.vue            # 个人信息与安全中心
            ├── user/                  # 用户及仓管管理
            ├── supplier/              # 供应商管理
            ├── warehouse/             # 仓库管理
            ├── material/              # 物资信息及分类
            └── apply/                 # 物资领用与归还申请
```

---

## 🔑 系统内置演示账号

系统启动时，后端 `DataInitializer` 将会自动初始化并加密以下三类角色的默认账号：

| 登录用户名 | 登录密码 | 角色类型 | 可操作的核心权限 |
| :--- | :--- | :--- | :--- |
| **admin** | `123456` | **系统管理员** | 拥有对所有模块、用户、设置及日志的最高权限 |
| **manager** | `123456` | **仓库管理员** | 物资数据管理、入库管理、出库管理、仓库和供应商维护 |
| **user1** | `123456` | **普通员工** | 查看物资列表、提交物资借用申请、提交归还申请 |

---

## 🛠️ 全栈环境搭建教程

本教程适用于在本地 Windows 环境下快速配置及部署本系统。

### 📦 前置环境依赖
请确保你的机器上安装了以下基础组件：
* **JDK 17** 及以上
* **Node.js 18.x** 及以上 (推荐 v20+)
* **Maven 3.8.x** 及以上
* **MySQL 8.0** 及以上

---

### 第一步：数据库创建与数据导入

1. 打开 MySQL 客户端或可视化管理工具 (如 Navicat、DBeaver)。
2. 创建名为 `wms_db` 的数据库，并指定字符集为 `utf8mb4`：
   ```sql
   CREATE DATABASE wms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 执行导入脚本。在项目根目录下以字节流重定向形式导入 `wms.sql` 和 `wms_fix.sql`（可防止中文乱码）：
   ```powershell
   # 导入核心表结构
   Start-Process -FilePath "mysql.exe" -ArgumentList "-u root -p wms_db" -RedirectStandardInput "backend/src/main/resources/wms.sql" -Wait
   
   # 导入中文字符集修正及种子数据
   Start-Process -FilePath "mysql.exe" -ArgumentList "-u root -p --default-character-set=utf8 wms_db" -RedirectStandardInput "backend/src/main/resources/wms_fix.sql" -Wait
   ```

---

### 第二步：后端服务配置与启动

1. **修改数据库连接**：
   打开 `backend/src/main/resources/application.yml`，修改如下连接地址中的数据库账号与密码：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/wms_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
       username: root       # 替换为你的 MySQL 用户名
       password:            # 替换为你的 MySQL 密码
   ```

2. **构建后端工程**：
   在控制台中切入 `backend` 目录，执行 maven 构建：
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

3. **运行服务**：
   使用 maven 插件快速拉起后端服务：
   ```bash
   mvn spring-boot:run
   ```
   * 后端服务将默认绑定并运行在：`http://localhost:8080/api`。

---

### 第三步：前端工程配置与启动

1. 打开控制台切入到 `frontend` 目录：
   ```bash
   cd frontend
   ```

2. **安装 Node 依赖**：
   ```bash
   npm install
   ```

3. **运行本地开发环境服务器**：
   ```bash
   npm run dev
   ```
   * 启动成功后，根据终端打印的连接，在浏览器中输入访问地址：`http://localhost:5173`。

4. **生产环境静态编译（可选）**：
   ```bash
   npm run build
   ```
   * 编译生成的网页资源将会打包至 `frontend/dist` 文件夹下。

---

## 🖥️ 上传到 GitHub 的操作说明

如果你想将本项目上传到你自己的 GitHub 仓库中，请执行以下命令序列：

```bash
# 1. 在本地初始化 Git 仓库（如果尚未初始化）
git init

# 2. 将所有文件添加至暂存区 (会自动根据根目录的 .gitignore 过滤 build 及 class 等冗余文件)
git add .

# 3. 提交至本地分支
git commit -m "feat: complete WMS upgrade with multi-theme support, dashboard visualizations, password security, dynamic jwt silent refresh, and bug fixes"

# 4. 创建并绑定远程 GitHub 仓库 (请将 URL 替换为你在 GitHub 上创建的空仓库地址)
git remote add origin https://github.com/你的用户名/你的仓库名.git

# 5. 推送至远程仓库的主分支
git branch -M main
git push -u origin main
```
