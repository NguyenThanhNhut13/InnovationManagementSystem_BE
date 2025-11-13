# Tự động cập nhật trạng thái InnovationPhase

## Tổng quan
Hệ thống đã được cập nhật để tự động chuyển đổi trạng thái của InnovationPhase theo thời gian:
- **SCHEDULED** → **ACTIVE** → **COMPLETED**

## Cách hoạt động

### 1. Scheduled Task (Tự động)
- **PhaseStatusSchedulerService** chạy tự động mỗi ngày lúc **00:01** (12:01 AM)
- Kiểm tra tất cả các phase và cập nhật trạng thái dựa trên:
  - `phaseStartDate`: Ngày bắt đầu phase
  - `phaseEndDate`: Ngày kết thúc phase
  - Ngày hiện tại

### 2. Quy tắc chuyển đổi trạng thái

#### SCHEDULED → ACTIVE
- Khi ngày hiện tại >= `phaseStartDate`
- Phase tự động chuyển sang trạng thái ACTIVE

#### ACTIVE → COMPLETED
- Khi ngày hiện tại > `phaseEndDate`
- Phase tự động chuyển sang trạng thái COMPLETED

#### Lưu ý đặc biệt:
- **DRAFT**: Không tự động chuyển đổi (cần cập nhật thủ công)
- **COMPLETED**: Không thay đổi sau khi đã hoàn thành

### 3. Cấu hình Cron Expression
```java
@Scheduled(cron = "0 1 0 * * ?")
```
- Chạy vào lúc 00:01:00 mỗi ngày
- Format: `giây phút giờ ngày tháng thứ`

Có thể thay đổi thời gian chạy bằng cách sửa cron expression:
- Mỗi giờ: `0 0 * * * ?`
- Mỗi 30 phút: `0 */30 * * * ?`
- Mỗi 5 phút: `0 */5 * * * ?`

