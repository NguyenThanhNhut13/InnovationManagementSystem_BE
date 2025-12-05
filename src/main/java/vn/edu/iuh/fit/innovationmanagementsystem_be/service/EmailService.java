package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 1. Gửi email OTP reset password
    public void sendOtpEmail(String toEmail, String otp, Long expiresInMinutes) {
        sendOtpEmail(toEmail, otp, expiresInMinutes, toEmail);
    }

    // 2. Gửi email OTP reset password với user name
    public void sendOtpEmail(String toEmail, String otp, Long expiresInMinutes, String userName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Hệ Thống Quản Lý Sáng Kiến - IUH"); // Thêm tên hiển thị
            helper.setTo(toEmail);
            helper.setSubject("🔒 Mã OTP đặt lại mật khẩu - Hệ Thống Quản Lý Sáng Kiến");

            String emailContent = String.format(
                    """
                            <html>
                            <head>
                                <style>
                                    body {
                                        font-family: Arial, sans-serif;
                                        background-color: #f4f6f9;
                                        padding: 20px;
                                    }
                                    .container {
                                        background: #ffffff;
                                        border-radius: 12px;
                                        padding: 30px;
                                        max-width: 620px;
                                        margin: auto;
                                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                                        border-top: 5px solid #d62828;
                                    }
                                    .header {
                                        text-align: center;
                                        margin-bottom: 20px;
                                    }
                                    .header img {
                                        width: 100px;
                                    }
                                    .title {
                                        font-size: 22px;
                                        font-weight: bold;
                                        color: #d62828;
                                        margin: 20px 0 10px;
                                        text-align: center;
                                        text-transform: uppercase;
                                    }
                                    .otp {
                                        font-size: 30px;
                                        font-weight: bold;
                                        color: #1d3557;
                                        background: #f1faee;
                                        padding: 12px 24px;
                                        border-radius: 8px;
                                        display: inline-block;
                                        margin: 25px auto;
                                        letter-spacing: 4px;
                                    }
                                    p {
                                        font-size: 15px;
                                        color: #333;
                                        line-height: 1.6;
                                    }
                                    .footer {
                                        font-size: 12px;
                                        color: #777;
                                        text-align: center;
                                        margin-top: 30px;
                                    }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <img src="https://i.ibb.co/1GLQ9gM9/Logo-ch-nh-th-c.png" alt="IUH Logo"/>
                                    </div>
                                    <div class="title">Mã OTP xác thực đặt lại mật khẩu</div>
                                    <p>Xin chào <b>%s</b>,</p>
                                    <p>Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản của mình trên <b>Hệ Thống Quản Lý Sáng Kiến</b>.</p>

                                    <div style="text-align: center;">
                                        <div class="otp">%s</div>
                                    </div>

                                    <p>Mã OTP này sẽ hết hạn sau <b>%d phút</b>. Vui lòng nhập mã OTP vào biểu mẫu đặt lại mật khẩu.</p>
                                    <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>

                                    <div class="footer">
                                        <p>Trân trọng,<br/>Đội ngũ Hệ Thống Quản Lý Sáng Kiến - IUH</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                            """,
                    userName, otp, expiresInMinutes);

            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể gửi email OTP: " + e.getMessage());
        }
    }

    // 3. Gửi email thông báo mật khẩu đã được thay đổi
    public void sendPasswordChangedEmail(String toEmail, String personnelId) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom(fromEmail, "Hệ Thống Quản Lý Sáng Kiến - IUH");
            helper.setTo(toEmail);
            helper.setSubject("🔔 Thông báo: Mật khẩu đã được thay đổi - Hệ Thống Quản Lý Sáng Kiến");

            String emailContent = String.format(
                    """
                            <!DOCTYPE html>
                            <html lang="vi">
                            <head>
                                <meta charset="UTF-8">
                                <style>
                                    body {
                                        font-family: Arial, sans-serif;
                                        background-color: #f4f6f9;
                                        padding: 20px;
                                        margin: 0;
                                    }
                                    .container {
                                        background: #ffffff;
                                        border-radius: 12px;
                                        padding: 30px;
                                        max-width: 620px;
                                        margin: auto;
                                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                                        border-top: 5px solid #1d3557;
                                    }
                                    .header {
                                        text-align: center;
                                        margin-bottom: 20px;
                                    }
                                    .header img {
                                        width: 120px;
                                    }
                                    .title {
                                        font-size: 22px;
                                        font-weight: bold;
                                        color: #1d3557;
                                        margin: 20px 0 15px;
                                        text-align: center;
                                        text-transform: uppercase;
                                    }
                                    p {
                                        font-size: 15px;
                                        color: #333;
                                        line-height: 1.6;
                                    }
                                    .account-box {
                                        background: #f1faee;
                                        padding: 12px 18px;
                                        border-radius: 8px;
                                        font-weight: bold;
                                        font-size: 16px;
                                        color: #d62828;
                                        text-align: center;
                                        margin: 20px auto;
                                        display: inline-block;
                                    }
                                    .footer {
                                        font-size: 12px;
                                        color: #777;
                                        text-align: center;
                                        margin-top: 30px;
                                    }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <img src="https://i.ibb.co/1GLQ9gM9/Logo-ch-nh-th-c.png" alt="IUH Logo"/>
                                    </div>
                                    <div class="title">Thông báo thay đổi mật khẩu</div>

                                    <p>Xin chào <b>%s</b>,</p>
                                    <p>Mật khẩu cho tài khoản của bạn trên <b>Hệ Thống Quản Lý Sáng Kiến</b> đã được thay đổi thành công.</p>

                                    <div style="text-align:center;">
                                        <div class="account-box">Mã nhân sự: %s</div>
                                    </div>

                                    <p>Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ ngay với <b>Quản trị hệ thống (Admin)</b> để được hỗ trợ kịp thời.</p>

                                    <div class="footer">
                                        <p>Trân trọng,<br/>Đội ngũ Hệ Thống Quản Lý Sáng Kiến - IUH</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                            """,
                    toEmail, personnelId);

            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            // Không throw exception vì đây chỉ là notification
        }
    }

    // 4. Gửi email backup với attachment
    public void sendBackupEmail(String toEmail, String backupFileName, byte[] backupData,
            vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType backupType) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom(fromEmail, "Hệ Thống Quản Lý Sáng Kiến - IUH");
            helper.setTo(toEmail);
            helper.setSubject("📦 Backup dữ liệu hệ thống - " + backupType.name() + " - " + java.time.LocalDate.now());

            String fileSizeFormatted = formatFileSize(backupData.length);

            String emailContent = String.format(
                    """
                            <!DOCTYPE html>
                            <html lang="vi">
                            <head>
                                <meta charset="UTF-8">
                                <style>
                                    body {
                                        font-family: Arial, sans-serif;
                                        background-color: #f4f6f9;
                                        padding: 20px;
                                        margin: 0;
                                    }
                                    .container {
                                        background: #ffffff;
                                        border-radius: 12px;
                                        padding: 30px;
                                        max-width: 620px;
                                        margin: auto;
                                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                                        border-top: 5px solid #28a745;
                                    }
                                    .header {
                                        text-align: center;
                                        margin-bottom: 20px;
                                    }
                                    .header img {
                                        width: 120px;
                                    }
                                    .title {
                                        font-size: 22px;
                                        font-weight: bold;
                                        color: #28a745;
                                        margin: 20px 0 15px;
                                        text-align: center;
                                        text-transform: uppercase;
                                    }
                                    p {
                                        font-size: 15px;
                                        color: #333;
                                        line-height: 1.6;
                                    }
                                    .info-box {
                                        background: #e8f5e9;
                                        padding: 15px 18px;
                                        border-radius: 8px;
                                        margin: 20px 0;
                                    }
                                    .info-box p {
                                        margin: 5px 0;
                                    }
                                    .footer {
                                        font-size: 12px;
                                        color: #777;
                                        text-align: center;
                                        margin-top: 30px;
                                    }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <img src="https://i.ibb.co/1GLQ9gM9/Logo-ch-nh-th-c.png" alt="IUH Logo"/>
                                    </div>
                                    <div class="title">Backup dữ liệu thành công</div>

                                    <p>Xin chào <b>Quản trị viên</b>,</p>
                                    <p>Hệ thống đã hoàn tất backup dữ liệu thành công. Thông tin chi tiết:</p>

                                    <div class="info-box">
                                        <p><strong>📁 Tên file:</strong> %s</p>
                                        <p><strong>📊 Loại backup:</strong> %s</p>
                                        <p><strong>💾 Kích thước:</strong> %s</p>
                                        <p><strong>🕐 Thời gian:</strong> %s</p>
                                    </div>

                                    <p>File backup đã được đính kèm trong email này. Vui lòng lưu trữ cẩn thận.</p>

                                    <div class="footer">
                                        <p>Trân trọng,<br/>Đội ngũ Hệ Thống Quản Lý Sáng Kiến - IUH</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                            """,
                    backupFileName,
                    backupType.name(),
                    fileSizeFormatted,
                    java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            helper.setText(emailContent, true);

            // Đính kèm file backup
            helper.addAttachment(extractFileName(backupFileName),
                    new jakarta.mail.util.ByteArrayDataSource(backupData, "application/octet-stream"));

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new IdInvalidException("Không thể gửi email backup: " + e.getMessage());
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String extractFileName(String path) {
        if (path == null)
            return "backup";
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

}
