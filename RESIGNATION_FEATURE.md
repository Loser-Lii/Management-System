# 销售员离职管理功能更新说明

## 更新日期
2025年12月7日

## 功能概述
实现了销售员离职的软删除管理方案，当销售员离职时，系统会：
1. 将销售员状态标记为"离职"
2. 自动清空该销售员负责的所有客户的负责人
3. 提醒管理员及时为这些客户重新分配销售员
4. 提供一键跳转到客户管理页面的功能

## 主要变更

### 后端变更

#### 1. 实体类修改
**文件**: `backend/src/main/java/com/example/backend/entity/Salesman.java`
- 新增字段:
  - `status`: String类型，状态标识（active-在职/resigned-离职）
  - `resignationDate`: LocalDate类型，离职日期
- 默认状态: 新创建的销售员默认为"active"状态

#### 2. Service层修改
**文件**: `backend/src/main/java/com/example/backend/service/SalesmanService.java`
- 修改`findAll()`方法: 只返回在职（active）状态的销售员
- 新增`findAllIncludingResigned()`方法: 返回包括离职员工在内的所有销售员
- **核心功能** - 新增`resign(Long id)`方法:
  ```java
  // 1. 标记销售员为离职状态
  // 2. 设置离职日期为当前日期
  // 3. 查找该销售员负责的所有客户
  // 4. 将这些客户的salesmanId设置为null
  // 5. 返回受影响的客户数量
  ```
- 保留`delete()`方法供管理员强制删除使用

#### 3. Controller层修改
**文件**: `backend/src/main/java/com/example/backend/controller/SalesmanController.java`
- 修改`findAll()`方法: 支持`includeResigned`参数来控制是否包含离职员工
- 新增`resign()`接口:
  - 路径: `POST /api/salesmen/{id}/resign`
  - 返回: 受影响的客户数量

#### 4. Customer相关修改
**文件**: 
- `backend/src/main/java/com/example/backend/repository/CustomerRepository.java`
  - 新增`findBySalesmanIdIsNull()`方法：查询无负责人的客户
- `backend/src/main/java/com/example/backend/service/CustomerService.java`
  - 新增`findWithoutSalesman()`方法：业务层查询无负责人客户
- `backend/src/main/java/com/example/backend/controller/CustomerController.java`
  - 新增`findWithoutSalesman()`接口
  - 路径: `GET /api/customers/without-salesman`

### 前端变更

#### 1. API接口更新
**文件**: `front/src/services/api.ts`
- 修改`salesmanApi.getAll()`: 支持`includeResigned`参数
- 新增`salesmanApi.resign(id)`: 调用离职接口
- 新增`customerApi.findWithoutSalesman()`: 查询无负责人客户

#### 2. 销售员管理页面
**文件**: `front/src/components/SalesmanList.vue`
- **表格展示**:
  - 新增"状态"列，显示在职/离职状态
  - 在职状态: 绿色徽章
  - 离职状态: 橙色徽章，并显示离职日期
- **操作按钮**:
  - 将"删除"按钮改为"办理离职"
  - 离职员工不显示升职/降职/离职按钮，只显示"已离职"文本
- **离职流程**:
  1. 点击"办理离职"弹出确认对话框
  2. 确认后调用resign接口
  3. 如果该销售员有负责的客户，再次弹出提示
  4. 显示受影响的客户数量
  5. 询问是否跳转到客户管理页面
  6. 确认后自动跳转到客户列表

#### 3. 客户管理页面
**文件**: `front/src/components/CustomerList.vue`
- **智能排序**:
  - 无负责人的客户自动排在列表最前面
- **视觉高亮**:
  - 无负责人客户的行背景色为浅橙色(#fff3e0)
  - 左侧有橙色边框标识
  - 客户名称旁显示红色"待分配"徽章（带脉冲动画）
  - 负责销售员列显示"⚠️ 未分配"（红色加粗）
- **样式细节**:
  - hover时背景色加深
  - 待分配徽章有呼吸效果
  - 视觉上非常醒目，便于管理员快速识别

### 数据库变更

#### SQL迁移脚本
**文件**: `backend/src/main/resources/db-migration/add_salesman_status.sql`

```sql
-- 添加状态字段
ALTER TABLE salesman ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'active';

-- 添加离职日期字段
ALTER TABLE salesman ADD COLUMN IF NOT EXISTS resignation_date DATE;

-- 更新现有数据
UPDATE salesman SET status = 'active' WHERE status IS NULL;
```

## 使用说明

### 管理员操作流程

1. **办理销售员离职**:
   - 进入"销售员管理"页面
   - 找到需要离职的销售员
   - 点击"办理离职"按钮
   - 确认离职操作

2. **处理无负责人客户**:
   - 离职成功后，系统会提示该销售员有X个客户需要重新分配
   - 点击"确认"跳转到客户管理页面
   - 或手动进入"客户管理"页面
   - 无负责人的客户会自动排在最前面，背景为橙色高亮
   - 点击"编辑"为客户重新分配销售员

3. **查看离职员工**:
   - 在销售员列表中可以看到离职员工
   - 状态列显示"离职"和离职日期
   - 离职员工不能进行升职/降职操作

### 数据说明

- **历史数据保留**: 离职员工的所有历史记录（销售记录、服务记录等）都会保留
- **客户关系**: 离职后客户的salesmanId变为null，但历史交易记录中仍保留原销售员信息
- **统计报表**: 历史业绩统计不受影响，仍可查询离职员工的历史业绩

## 技术亮点

1. **软删除设计**: 不真正删除数据，只标记状态，保护历史数据完整性
2. **事务保证**: 使用@Transactional确保离职操作和客户转移的原子性
3. **用户体验**: 
   - 清晰的视觉反馈（颜色、徽章、动画）
   - 智能排序（待分配客户置顶）
   - 流畅的操作流程（确认→执行→提示→跳转）
4. **数据一致性**: 自动处理关联数据，避免遗留问题

## 注意事项

1. **首次部署**: 需要执行SQL迁移脚本添加新字段
2. **现有数据**: 所有现有销售员会自动设置为"active"状态
3. **权限控制**: 只有管理员可以办理离职和分配客户
4. **数据备份**: 建议在执行迁移前备份数据库

## 后续优化建议

1. 可以添加"重新入职"功能，将resigned状态改回active
2. 可以添加离职原因字段记录离职详情
3. 可以添加批量分配客户功能
4. 可以添加离职员工的业绩归档报表

## 测试建议

1. 测试离职无客户的销售员
2. 测试离职有客户的销售员
3. 测试跨页面跳转
4. 测试客户列表的排序和高亮
5. 测试客户重新分配后的状态变化
