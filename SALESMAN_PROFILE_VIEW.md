# Salesman Profile View Refactoring - Read from View, Write to Entity Pattern

## Overview
Implemented a "Read from View, Write to Entity" pattern for the Salesman Profile feature:
- **READ operations**: Query from `view_salesman_profile` (read-only view)
- **WRITE operations**: Update `salesman` table entity directly

## Database Changes

### View Definition
Location: `backend/src/main/resources/db-migration/create_salesman_profile_view.sql`

```sql
CREATE OR REPLACE VIEW view_salesman_profile AS
SELECT 
    s.id,
    s.employee_no,
    s.name,
    s.avatar,
    s.level,
    s.commission_rate,
    s.hire_date,
    s.status,
    s.phone,
    s.email,
    s.wechat,
    s.qq,
    s.remark,
    u.username AS login_username
FROM salesman s
LEFT JOIN users u ON u.salesman_id = s.id;
```

**To apply:** Run this SQL script in your MySQL database.

## Architecture Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                    READ PATH (Query)                        │
├─────────────────────────────────────────────────────────────┤
│  Controller → Service.getProfileById()                      │
│      ↓                                                       │
│  Repository.findProfileById() [JPQL Query]                  │
│      ↓                                                       │
│  view_salesman_profile (Database View)                      │
│      ↓                                                       │
│  Return SalesmanProfileDTO                                  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    WRITE PATH (Update)                      │
├─────────────────────────────────────────────────────────────┤
│  Controller → Service.updateProfile(id, form)               │
│      ↓                                                       │
│  Repository.findById(id) [Find Entity]                      │
│      ↓                                                       │
│  Update Salesman Entity fields                              │
│      ↓                                                       │
│  Repository.save(entity) [Write to salesman table]          │
│      ↓                                                       │
│  Re-query from view and return SalesmanProfileDTO           │
└─────────────────────────────────────────────────────────────┘
```

## Code Changes

### 1. DTOs Created

**File:** `com.example.backend.dto.SalesmanProfileDTO`
- Maps all columns from `view_salesman_profile` (READ)
- Uses Lombok `@Data` annotation
- Proper Java types (BigDecimal for commission_rate, LocalDate for hire_date)

**File:** `com.example.backend.dto.SalesmanUpdateForm`
- Contains only editable fields (WRITE)
- Fields: phone, email, wechat, qq, avatar, remark
- Does NOT include read-only fields (id, employeeNo, level, etc.)

### 2. Repository Update
**File:** `com.example.backend.repository.SalesmanRepository`
- Added method: `Optional<SalesmanProfileDTO> findProfileById(Long id)`
- Uses JPQL constructor expression to map query results to DTO
- Includes subquery to fetch login username from User entity

### 3. Service Update
**File:** `com.example.backend.service.SalesmanService`

**READ Method:**
- `SalesmanProfileDTO getProfileById(Long id)`
- Queries from `view_salesman_profile` via Repository
- Returns DTO with aggregated data

**WRITE Method:**
- `SalesmanProfileDTO updateProfile(Long id, SalesmanUpdateForm form)`
- Finds Salesman entity from `salesman` table (NOT from view)
- Updates only the fields provided in the form
- Saves entity using `repository.save()`
- Re-queries from view and returns updated DTO

### 4. Controller Update
**File:** `com.example.backend.controller.SalesmanController`

**READ Endpoint:**
- `GET /api/salesmen/{id}/profile`
- Returns `Result<SalesmanProfileDTO>`
- Queries from view

**WRITE Endpoint:**
- `PUT /api/salesmen/{id}/profile`
- Accepts `SalesmanUpdateForm` in request body
- Updates entity in `salesman` table
- Returns updated `Result<SalesmanProfileDTO>`

**Note:** Existing `GET /api/salesmen/{id}` endpoint unchanged (still returns basic Salesman entity)

## API Usage

### READ Endpoint

**GET /api/salesmen/{id}/profile**

Queries from `view_salesman_profile` and returns complete profile including login username.

**Response Example:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "employeeNo": "EMP001",
    "name": "张三",
    "avatar": "/uploads/avatars/xxx.jpg",
    "level": "senior",
    "commissionRate": 0.06,
    "hireDate": "2023-01-15",
    "status": "active",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "wechat": "zhangsan_wx",
    "qq": "123456789",
    "remark": "优秀销售员",
    "loginUsername": "zhangsan"
  }
}
```

### WRITE Endpoint

**PUT /api/salesmen/{id}/profile**

Updates the `salesman` entity (NOT the view) and returns updated profile.

**Request Body Example:**
```json
{
  "phone": "13900139000",
  "email": "newemail@example.com",
  "wechat": "new_wechat",
  "qq": "987654321",
  "avatar": "/uploads/avatars/new.jpg",
  "remark": "Updated remark"
}
```

**Response Example:**
```json
{
  "code": 200,
  "message": "档案更新成功",
  "data": {
    "id": 1,
    "employeeNo": "EMP001",
    "name": "张三",
    "avatar": "/uploads/avatars/new.jpg",
    "level": "senior",
    "commissionRate": 0.06,
    "hireDate": "2023-01-15",
    "status": "active",
    "phone": "13900139000",
    "email": "newemail@example.com",
    "wechat": "new_wechat",
    "qq": "987654321",
    "remark": "Updated remark",
    "loginUsername": "zhangsan"
  }
}
```

**Note:** Only the fields in the request body will be updated. Other fields remain unchanged.

### Existing Endpoint (Unchanged)

**GET /api/salesmen/{id}**

Still returns basic Salesman entity without login username.

## Benefits

1. **Clear Separation**: Read from View, Write to Entity - each has its own path
2. **Single Source of Truth**: View definition centralized in database
3. **Performance**: Single query for reads instead of multiple joins
4. **Safety**: View is read-only, prevents accidental updates
5. **Maintainability**: Schema changes only require updating the View
6. **Flexibility**: Can update individual fields without affecting the whole entity
7. **Consistency**: After update, always return fresh data from view

## Migration Notes

1. Run the SQL script to create the view
2. Restart Spring Boot application
3. Update frontend to use new `/api/salesmen/{id}/profile` endpoint if needed
4. Old endpoint still works for backward compatibility

## Testing

### Test READ endpoint:
```bash
curl http://localhost:8080/api/salesmen/1/profile
```

### Test WRITE endpoint:
```bash
curl -X PUT http://localhost:8080/api/salesmen/1/profile \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13900139000",
    "email": "newemail@example.com",
    "wechat": "new_wechat",
    "qq": "987654321"
  }'
```

## Key Points

1. **View is READ-ONLY**: Never try to INSERT/UPDATE/DELETE on `view_salesman_profile`
2. **Entity for WRITE**: All updates go through the `Salesman` entity and `salesman` table
3. **Partial Updates**: Only fields provided in `SalesmanUpdateForm` are updated
4. **Null Safety**: Check for null before updating entity fields
5. **Return Fresh Data**: After update, re-query from view to ensure consistency
6. **Transaction**: Update method is `@Transactional` to ensure atomicity
