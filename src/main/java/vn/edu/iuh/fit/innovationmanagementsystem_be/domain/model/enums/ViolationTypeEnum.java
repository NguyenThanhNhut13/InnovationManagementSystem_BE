package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum ViolationTypeEnum {
    // 1. Vi phạm tính mới (Điều 4.1.a & Điều 6.1)
    // Dùng khi: Đã có người làm rồi, hoặc copy từ người khác, hoặc nộp sau người khác.
    DUPLICATE,

    // 2. Không khả thi / Sai phạm vi (Điều 4.1.b)
    // Dùng khi: Giải pháp viển vông, không áp dụng được tại trường ĐH CN TP.HCM.
    FEASIBILITY, 

    // 3. Vi phạm quy định cấm (Điều 4.2) - RẤT QUAN TRỌNG
    // Dùng cho 3 trường hợp trong văn bản:
    // - Trái đạo đức xã hội/trật tự công cộng 
    // - Đang tranh chấp/bảo hộ quyền sở hữu trí tuệ 
    // - Lấy từ luận văn/đồ án sinh viên mà chưa xin phép 
    POLICY_VIOLATION, 

    // 4. Khác (Dành cho các lý do đặc thù mà giám khảo muốn ghi chú thêm)
    OTHER
}

