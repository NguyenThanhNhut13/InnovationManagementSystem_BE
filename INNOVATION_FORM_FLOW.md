# Innovation Form Data Flow - Hệ thống đăng ký sáng kiến

## 📋 Tổng quan Flow

Hệ thống cho phép người dùng đăng ký sáng kiến thông qua việc điền 2 mẫu form với các trạng thái DRAFT và SUBMITTED.

## 🔄 Flow thực tế

### 1. **Điền Mẫu 1 (Lần đầu)**
- **API**: `POST /api/v1/innovations/form-data`
- **Action**: `DRAFT` hoặc `SUBMITTED`
- **Logic**: Tự động tạo Innovation mới với status DRAFT (dù action là SUBMITTED)

### 2. **Chỉnh sửa Mẫu 1 (Lưu nháp)**
- **API**: `PUT /api/v1/innovations/{innovationId}/form-data`
- **Action**: `DRAFT`
- **Logic**: Cập nhật form data đã tồn tại, giữ nguyên trạng thái DRAFT

### 3. **Hoàn thành Mẫu 1 (Nộp)**
- **API**: `PUT /api/v1/innovations/{innovationId}/form-data`
- **Action**: `SUBMITTED`
- **Logic**: ❌ **TỪ CHỐI** - Chỉ có thể SUBMITTED khi đã điền xong cả 2 mẫu form

### 4. **Điền Mẫu 2 (Tiếp tục)**
- **API**: `PUT /api/v1/innovations/{innovationId}/form-data`
- **Action**: `DRAFT`
- **Logic**: Thêm form data mới cho mẫu 2, giữ nguyên trạng thái DRAFT

### 5. **Hoàn thành Mẫu 2 (Nộp cuối)**
- **API**: `PUT /api/v1/innovations/{innovationId}/form-data`
- **Action**: `SUBMITTED`
- **Logic**: Cập nhật form data và chuyển trạng thái Innovation thành SUBMITTED

## 🚫 Validation Rules

### ActionType Validation
- **Chỉ chấp nhận**: `DRAFT` hoặc `SUBMITTED`
- **Từ chối**: Tất cả các trạng thái khác (PENDING_KHOA_REVIEW, KHOA_APPROVED, etc.)
- **Lý do**: Các trạng thái khác được xử lý bởi hội đồng chấm điểm

### Permission Validation
- **Chỉ owner** của innovation mới có thể chỉnh sửa
- **Chỉ cho phép chỉnh sửa** khi innovation ở trạng thái `DRAFT`
- **Từ chối chỉnh sửa** khi innovation đã `SUBMITTED` hoặc các trạng thái khác

### Template Completion Validation
- **Chỉ cho phép SUBMITTED** khi đã điền xong cả 2 mẫu form
- **Từ chối SUBMITTED** khi chỉ có 1 mẫu hoặc chưa có mẫu nào
- **Logic kiểm tra**: Đếm số lượng template ID khác nhau trong form data

## 📊 API Endpoints

### 1. Tạo Innovation & Form Data
```http
POST /api/v1/innovations/form-data
Content-Type: application/json
Authorization: Bearer {token}

{
  "templateId": "template_1",
  "actionType": "DRAFT", // hoặc "SUBMITTED"
  "innovationName": "Tên sáng kiến",
  "innovationRoundId": "round_id",
  "isScore": true,
  "formDataItems": [
    {
      "fieldValue": "Giá trị field",
      "formFieldId": "field_id"
    }
  ]
}
```

### 2. Cập nhật Innovation Form Data
```http
PUT /api/v1/innovations/{innovationId}/form-data
Content-Type: application/json
Authorization: Bearer {token}

{
  "templateId": "template_2",
  "actionType": "DRAFT", // hoặc "SUBMITTED"
  "formDataItems": [
    {
      "fieldValue": "Giá trị mới",
      "formFieldId": "field_id",
      "dataId": "existing_data_id" // Cho update
    },
    {
      "fieldValue": "Giá trị mới",
      "formFieldId": "new_field_id"
      // Không có dataId = tạo mới
    }
  ]
}
```

### 3. Lấy Innovation Form Data
```http
GET /api/v1/innovations/{innovationId}/form-data?templateId={templateId}
Authorization: Bearer {token}
```

### 4. Lấy danh sách Innovation theo trạng thái
```http
GET /api/v1/innovations/my-innovations?status=DRAFT
GET /api/v1/innovations/my-innovations?status=SUBMITTED
GET /api/v1/innovations/my-innovations?status=PENDING_KHOA_REVIEW
Authorization: Bearer {token}
```

## 🔒 Business Rules

### Trạng thái Innovation
- **DRAFT**: Người dùng có thể chỉnh sửa tự do
- **SUBMITTED**: Không thể chỉnh sửa, chờ hội đồng chấm điểm
- **Các trạng thái khác**: Được quản lý bởi hội đồng chấm điểm

### Form Data Management
- **Tạo mới**: Khi `dataId` không có hoặc rỗng
- **Cập nhật**: Khi `dataId` có giá trị
- **Template**: Mỗi form data thuộc về một template cụ thể

### User Experience
- **Lưu nháp**: Cho phép user lưu tạm khi chưa hoàn thành
- **Nộp chính thức**: Chuyển sang trạng thái SUBMITTED
- **Chỉnh sửa**: Chỉ được phép khi ở trạng thái DRAFT

## ⚠️ Error Handling

### Validation Errors
- `Action type chỉ được là DRAFT hoặc SUBMITTED`
- `Chỉ có thể chỉnh sửa sáng kiến ở trạng thái DRAFT`
- `Bạn không có quyền chỉnh sửa sáng kiến này`
- `Chỉ có thể SUBMITTED khi đã điền xong cả 2 mẫu form. Vui lòng hoàn thành mẫu còn lại trước khi nộp.`

### Business Logic Errors
- `Không tìm thấy đợt sáng kiến với ID: {id}`
- `Không tìm thấy sáng kiến với ID: {id}`
- `Sáng kiến hiện tại đang ở trạng thái: {status}`

## 🎯 Use Cases

### Case 1: User điền mẫu 1 lần đầu
1. Gọi `POST /api/v1/innovations/form-data` với `actionType: "DRAFT"`
2. Hệ thống tạo Innovation mới với status DRAFT
3. Lưu form data cho mẫu 1

### Case 2: User chỉnh sửa mẫu 1
1. Gọi `PUT /api/v1/innovations/{id}/form-data` với `actionType: "DRAFT"`
2. Hệ thống cập nhật form data hiện có
3. Giữ nguyên status DRAFT

### Case 3: User cố gắng nộp mẫu 1
1. Gọi `PUT /api/v1/innovations/{id}/form-data` với `actionType: "SUBMITTED"`
2. Hệ thống từ chối với lỗi: "Chỉ có thể SUBMITTED khi đã điền xong cả 2 mẫu form"
3. Status vẫn giữ nguyên DRAFT

### Case 4: User điền mẫu 2
1. Gọi `PUT /api/v1/innovations/{id}/form-data` với `actionType: "DRAFT"`
2. Hệ thống thêm form data mới cho mẫu 2
3. Chuyển status về DRAFT (cho phép chỉnh sửa tiếp)

### Case 5: User hoàn thành mẫu 2
1. Gọi `PUT /api/v1/innovations/{id}/form-data` với `actionType: "SUBMITTED"`
2. Hệ thống cập nhật form data
3. Chuyển status thành SUBMITTED (hoàn thành đăng ký)
