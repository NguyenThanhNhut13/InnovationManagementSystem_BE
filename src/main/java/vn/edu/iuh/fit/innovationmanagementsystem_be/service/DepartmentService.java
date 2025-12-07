package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentImportResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentInnovationStatisticsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

        private final DepartmentRepository departmentRepository;
        private final DepartmentMapper departmentMapper;

        public DepartmentService(DepartmentRepository departmentRepository,
                        DepartmentMapper departmentMapper) {
                this.departmentRepository = departmentRepository;
                this.departmentMapper = departmentMapper;
        }

        // 1. Lấy tất cả Departments với Pagination and Filtering
        public ResultPaginationDTO getAllDepartments(@NonNull Specification<Department> specification,
                        @NonNull Pageable pageable) {

                if (pageable.getSort().isUnsorted()) {
                        pageable = org.springframework.data.domain.PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        org.springframework.data.domain.Sort.by("createdAt").descending());
                }

                Page<Department> departments = departmentRepository.findAll(specification, pageable);

                Page<DepartmentResponse> departmentResponses = departments.map(departmentMapper::toDepartmentResponse);
                return Utils.toResultPaginationDTO(departmentResponses, pageable);
        }

        // 2. Lấy thống kê số lượng Innovation của tất cả các Department
        public List<DepartmentInnovationStatisticsResponse> getDepartmentInnovationStatistics() {
                List<Department> departments = departmentRepository.findAll();

                return departments.stream()
                                .map(department -> {
                                        DepartmentInnovationStatisticsResponse stats = new DepartmentInnovationStatisticsResponse();
                                        stats.setDepartmentId(department.getId());
                                        stats.setDepartmentName(department.getDepartmentName());
                                        stats.setDepartmentCode(department.getDepartmentCode());
                                        stats.setTotalInnovations(departmentRepository
                                                        .countInnovationsByDepartmentId(department.getId()));
                                        stats.setDraftInnovations(
                                                        departmentRepository.countDraftInnovationsByDepartmentId(
                                                                        department.getId()));
                                        stats.setSubmittedInnovations(
                                                        departmentRepository.countSubmittedInnovationsByDepartmentId(
                                                                        department.getId()));
                                        stats.setApprovedInnovations(
                                                        departmentRepository.countApprovedInnovationsByDepartmentId(
                                                                        department.getId()));
                                        stats.setRejectedInnovations(
                                                        departmentRepository.countRejectedInnovationsByDepartmentId(
                                                                        department.getId()));
                                        return stats;
                                })
                                .collect(Collectors.toList());
        }

        // 3. Import departments từ file Excel
        public DepartmentImportResponse importDepartmentsFromExcel(MultipartFile file) {
                validateExcelFile(file);

                List<DepartmentImportResponse.SkippedDepartment> skippedDepartments = new ArrayList<>();
                List<Department> departmentsToSave = new ArrayList<>();
                int totalRecords = 0;

                try (InputStream inputStream = file.getInputStream();
                                Workbook workbook = new XSSFWorkbook(inputStream)) {

                        Sheet sheet = workbook.getSheetAt(0);
                        boolean isFirstRow = true;

                        for (Row row : sheet) {
                                if (isFirstRow) {
                                        isFirstRow = false;
                                        continue;
                                }

                                Cell codeCell = row.getCell(0);
                                Cell nameCell = row.getCell(1);

                                if (codeCell == null || nameCell == null) {
                                        continue;
                                }

                                String departmentCode = getCellValueAsString(codeCell).trim();
                                String departmentName = getCellValueAsString(nameCell).trim();

                                if (departmentCode.isEmpty() || departmentName.isEmpty()) {
                                        continue;
                                }

                                totalRecords++;

                                if (departmentRepository.existsByDepartmentCode(departmentCode)) {
                                        skippedDepartments.add(new DepartmentImportResponse.SkippedDepartment(
                                                        departmentCode, departmentName, "Mã phòng ban đã tồn tại"));
                                        continue;
                                }

                                if (departmentRepository.existsByDepartmentName(departmentName)) {
                                        skippedDepartments.add(new DepartmentImportResponse.SkippedDepartment(
                                                        departmentCode, departmentName, "Tên phòng ban đã tồn tại"));
                                        continue;
                                }

                                Department department = new Department();
                                department.setDepartmentCode(departmentCode);
                                department.setDepartmentName(departmentName);
                                department.setIsActive(true);
                                departmentsToSave.add(department);
                        }

                        departmentRepository.saveAll(departmentsToSave);

                } catch (Exception e) {
                        throw new IdInvalidException("Lỗi khi đọc file Excel: " + e.getMessage());
                }

                int importedCount = departmentsToSave.size();
                int skippedCount = skippedDepartments.size();

                String message = String.format("Import thành công %d/%d phòng ban. Bỏ qua %d phòng ban đã tồn tại.",
                                importedCount, totalRecords, skippedCount);

                return new DepartmentImportResponse(totalRecords, importedCount, skippedCount, skippedDepartments,
                                message);
        }

        private String getCellValueAsString(Cell cell) {
                if (cell == null) {
                        return "";
                }
                switch (cell.getCellType()) {
                        case STRING:
                                return cell.getStringCellValue();
                        case NUMERIC:
                                return String.valueOf((long) cell.getNumericCellValue());
                        case BOOLEAN:
                                return String.valueOf(cell.getBooleanCellValue());
                        default:
                                return "";
                }
        }

        private void validateExcelFile(MultipartFile file) {
                if (file == null || file.isEmpty()) {
                        throw new IdInvalidException("File không được để trống");
                }

                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null ||
                                (!originalFilename.toLowerCase().endsWith(".xlsx")
                                                && !originalFilename.toLowerCase().endsWith(".xls"))) {
                        throw new IdInvalidException("Chỉ chấp nhận file Excel (.xlsx hoặc .xls)");
                }
        }
}
