package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentInnovationStatisticsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentMapper;

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
                    stats.setTotalInnovations(departmentRepository.countInnovationsByDepartmentId(department.getId()));
                    stats.setDraftInnovations(
                            departmentRepository.countDraftInnovationsByDepartmentId(department.getId()));
                    stats.setSubmittedInnovations(
                            departmentRepository.countSubmittedInnovationsByDepartmentId(department.getId()));
                    stats.setApprovedInnovations(
                            departmentRepository.countApprovedInnovationsByDepartmentId(department.getId()));
                    stats.setRejectedInnovations(
                            departmentRepository.countRejectedInnovationsByDepartmentId(department.getId()));
                    return stats;
                })
                .collect(Collectors.toList());
    }
}
