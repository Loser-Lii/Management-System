# 项目初始化指南

## 项目简介
销售员绩效管理系统 - 大作业项目，前后端分离架构。

## 快速开始

### 前置要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.8+

### 数据库初始化

#### 方法一：直接导入演示数据库（推荐）

1. 创建数据库：
```sql
CREATE DATABASE salesman_performance_management_system;
```

2. 导入演示数据：
```powershell
# Windows PowerShell
mysql -u root -p < backend/src/main/resources/db-init/database_init.sql

# 或 Linux/Mac
mysql -u root -p salesman_performance_management_system < backend/src/main/resources/db-init/database_init.sql
```

3. 配置数据库连接（在项目根目录创建 `.env` 文件，参考 `backend/.env.example`）：
```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/salesman_performance_management_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password
```

#### 方法二：首次自动初始化（如果你修改了演示数据）

如果需要重新生成演示数据，修改 `backend/src/main/resources/application.properties`：
```properties
spring.jpa.hibernate.ddl-auto=create
```
运行应用后，会自动执行 `DataInitializer` 初始化数据。完成后改回 `update`。

### 后端启动

```powershell
cd backend
mvn clean spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 前端启动

```powershell
cd front
npm install
npm run dev
```

前端默认运行在 `http://localhost:5174`

## 项目结构

```
Management-System/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/         # 源代码
│   ├── src/main/resources/
│   │   ├── db-init/           # 数据库初始化脚本
│   │   │   └── database_init.sql  # 演示数据库导出
│   │   └── application.properties
│   └── pom.xml
├── front/                      # Vue 3 前端
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
├── .gitignore
├── README.md
└── database_init.sql          # 备份的演示数据库导出
```

## 数据库架构说明

### 核心表

- **user**: 系统用户（支持角色权限）
- **salesman**: 销售员（包含等级、佣金率等）
- **customer**: 客户（包含级别 A/B/C、负责销售员等）
- **product**: 产品（演示数据包含医疗产品）
- **sales_record**: 销售记录（记录销售人、产品、数量、价格等）
- **collection_record**: 回款记录
- **complaint_record**: 投诉记录
- **contact_record**: 联系记录
- **service_record**: 服务记录

### 关键字段

- `salesman.level`: 销售员等级（junior、intermediate、senior、expert）
- `salesman.commission_rate`: 佣金率（根据等级自动调整）
- `customer.level`: 客户等级（A、B、C）
- `customer.salesman_id`: 客户负责销售员

## 主要功能

- ✅ 用户认证与权限管理
- ✅ 销售员管理（升职/降职）
- ✅ 客户管理与分配
- ✅ 销售记录统计
- ✅ 绩效分析与建议
- ✅ 文件上传

## 演示数据说明

`database_init.sql` 包含完整的演示数据：
- 20 个医疗产品（Arknights 主题）
- 20 个销售员（包含各个等级）
- 5 个企业客户 + 5 个个人客户
- 完整的销售、回款、投诉等记录

## 常见问题

### Q: 如何重置数据库为演示数据？
A: 删除数据库，重新运行导入命令即可。

### Q: 能否修改演示数据？
A: 可以。修改后，若需保存，运行：
```powershell
mysqldump -u root -p --routines --triggers salesman_performance_management_system > backend/src/main/resources/db-init/database_init.sql
```

### Q: 前端无法连接后端？
A: 检查后端是否运行在 `8080` 端口，检查 CORS 配置（`backend/src/main/java/com/example/backend/config/WebConfig.java`）。

### Q: 如何生成新的数据库导出？
A: 在项目根运行：
```powershell
mysqldump -u root -p --routines --triggers salesman_performance_management_system > database_init.sql
```

## 合作开发建议

1. **数据同步**：每个开发者在本地导入 `database_init.sql` 以保持演示数据一致。
2. **Schema 变更**：在 entity 中修改，利用 Hibernate 的 `ddl-auto=update` 自动迁移。
3. **敏感配置**：使用环境变量或 `.env` 文件（参考 `.env.example`），**不要提交真实凭据**。
4. **分支管理**：建议基于功能或前后端分支，定期合并到 `main`。

## 技术栈

- **后端**：Spring Boot 3.5.8, JPA/Hibernate, MySQL 8.0
- **前端**：Vue 3, TypeScript, Vite, Bulma CSS
- **工具**：Maven, npm, Git

## 联系方式

如有问题，请在项目中提交 Issue 或联系开发团队。

---

**最后更新**：2025年12月7日
