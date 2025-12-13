package vn.edu.iuh.fit.innovationmanagementsystem_be.repository.projection;

/**
 * Interface projection cho kết quả similarity search
 * Spring Data JPA sẽ tự động map kết quả query vào interface này
 */
public interface SimilarInnovationProjection {
    // Innovation fields
    String getId();
    String getInnovationName();
    String getStatus();
    
    // User fields (JOIN)
    String getAuthorName();
    
    // Department fields (JOIN)
    String getDepartmentName();
    
    // Similarity score (calculated field)
    Double getSimilarity();
}

