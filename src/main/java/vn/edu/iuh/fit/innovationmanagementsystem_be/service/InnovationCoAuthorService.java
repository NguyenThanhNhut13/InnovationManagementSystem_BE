package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TemplateDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CoInnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class InnovationCoAuthorService {

    private static final Logger logger = LoggerFactory.getLogger(InnovationCoAuthorService.class);

    private final CoInnovationRepository coInnovationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public InnovationCoAuthorService(CoInnovationRepository coInnovationRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.coInnovationRepository = coInnovationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public void processCoInnovations(Innovation innovation, List<TemplateDataRequest> templates) {
        for (TemplateDataRequest templateRequest : templates) {
            if (templateRequest.getFormData() == null || templateRequest.getFormData().isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Object> entry : templateRequest.getFormData().entrySet()) {
                String fieldKey = entry.getKey();
                Object fieldValue = entry.getValue();

                if (fieldValue != null) {
                    try {
                        JsonNode valueNode = objectMapper.valueToTree(fieldValue);

                        if (valueNode.isArray() && valueNode.size() > 0) {
                            JsonNode firstItem = valueNode.get(0);
                            if (firstItem.isObject() && firstItem.has("ma_nhan_su")) {
                                for (JsonNode itemNode : valueNode) {
                                    processCoInnovationItem(innovation, itemNode);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Lỗi khi kiểm tra đồng sáng kiến cho field {}: {}",
                                fieldKey, e.getMessage());
                    }
                }
            }
        }
    }

    private void processCoInnovationItem(Innovation innovation, JsonNode itemNode) {
        try {
            JsonNode hoVaTenNode = itemNode.get("ho_va_ten");
            JsonNode maNhanSuNode = itemNode.get("ma_nhan_su");
            JsonNode noiCongTacNode = itemNode.get("noi_cong_tac_hoac_noi_thuong_tru");

            String hoVaTen = hoVaTenNode != null && !hoVaTenNode.isNull()
                    ? hoVaTenNode.asText().trim()
                    : "";
            String noiCongTac = noiCongTacNode != null && !noiCongTacNode.isNull()
                    ? noiCongTacNode.asText().trim()
                    : "";

            boolean hasMaNhanSu = maNhanSuNode != null && !maNhanSuNode.isNull()
                    && !maNhanSuNode.asText().trim().isEmpty();

            User user = null;
            if (hasMaNhanSu && maNhanSuNode != null) {
                String maNhanSu = maNhanSuNode.asText().trim();
                Optional<User> userOpt = userRepository.findByPersonnelId(maNhanSu);

                if (userOpt.isEmpty()) {
                    logger.warn("Không tìm thấy User với mã nhân sự: {}. Sẽ lưu như đồng tác giả bên ngoài.",
                            maNhanSu);
                } else {
                    user = userOpt.get();
                    final User finalUser = user;

                    boolean exists = coInnovationRepository.findByInnovationId(innovation.getId())
                            .stream()
                            .anyMatch(co -> co.getUser() != null
                                    && co.getUser().getId()
                                            .equals(finalUser.getId()));

                    if (exists) {
                        logger.info("Đồng tác giả với user_id {} đã tồn tại, bỏ qua",
                                finalUser.getId());
                        return;
                    }
                }
            }

            if (hoVaTen.isEmpty()) {
                if (user != null) {
                    hoVaTen = user.getFullName();
                } else {
                    throw new IdInvalidException(
                            "Đồng tác giả phải có họ tên (ho_va_ten) nếu không có mã nhân sự");
                }
            }

            CoInnovation coInnovation = new CoInnovation();
            coInnovation.setInnovation(innovation);
            coInnovation.setUser(user);
            coInnovation.setCoInnovatorFullName(hoVaTen);

            if (!noiCongTac.isEmpty()) {
                coInnovation.setCoInnovatorDepartmentName(noiCongTac);
            } else if (user != null && user.getDepartment() != null) {
                coInnovation.setCoInnovatorDepartmentName(user.getDepartment().getDepartmentName());
            } else {
                coInnovation.setCoInnovatorDepartmentName("Bên ngoài");
            }

            StringBuilder contactInfo = new StringBuilder();
            if (user != null) {
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    contactInfo.append("Email: ").append(user.getEmail());
                }
                if (user.getPersonnelId() != null && !user.getPersonnelId().isEmpty()) {
                    if (contactInfo.length() > 0) {
                        contactInfo.append("; ");
                    }
                    contactInfo.append("Mã NV: ").append(user.getPersonnelId());
                }
            }
            coInnovation.setContactInfo(
                    contactInfo.length() > 0 ? contactInfo.toString()
                            : "Chưa có thông tin liên hệ");

            coInnovationRepository.save(coInnovation);
            logger.info("Đã lưu đồng tác giả: {} (user_id: {})", hoVaTen,
                    user != null ? user.getId() : "null");
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý item đồng sáng kiến: {}", e.getMessage(), e);
            throw new IdInvalidException(
                    "Lỗi khi xử lý item đồng sáng kiến: " + e.getMessage());
        }
    }
}
