# Tự động cập nhật trạng thái DepartmentPhase

## Tổng quan
Hệ thống đã được cập nhật để tự động chuyển đổi trạng thái của DepartmentPhase theo thời gian:
- **SCHEDULED** → **ACTIVE** → **COMPLETED**

## Cách hoạt động

### 1. Scheduled Task (Tự động)
- **DepartmentPhaseStatusSchedulerService** chạy tự động mỗi ngày lúc **00:02** (12:02 AM)
- Chạy sau InnovationPhase (00:01) để đảm bảo thứ tự logic
- Kiểm tra tất cả các department phase và cập nhật trạng thái dựa trên:
  - `phaseStartDate`: Ngày bắt đầu phase
  - `phaseEndDate`: Ngày kết thúc phase
  - Ngày hiện tại

### 2. Quy tắc chuyển đổi trạng thái

#### SCHEDULED → ACTIVE
- Khi ngày hiện tại >= `phaseStartDate`
- Department phase tự động chuyển sang trạng thái ACTIVE

#### ACTIVE → COMPLETED
- Khi ngày hiện tại > `phaseEndDate`
- Department phase tự động chuyển sang trạng thái COMPLETED

#### Lưu ý đặc biệt:
- **DRAFT**: Không tự động chuyển đổi (cần publish thủ công)
- **COMPLETED**: Không thay đổi sau khi đã hoàn thành

### 3. Cấu hình Cron Expression
```java
@Scheduled(cron = "0 2 0 * * ?")
```
- Chạy vào lúc 00:02:00 mỗi ngày
- Chạy sau InnovationPhase để đảm bảo consistency
- Format: `giây phút giờ ngày tháng thứ`

