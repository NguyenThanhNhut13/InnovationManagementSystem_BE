package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ScoreCriteriaDetail;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoreResponse;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ReviewScoreMapper {

    private static final Logger logger = LoggerFactory.getLogger(ReviewScoreMapper.class);

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(source = "id", target = "reviewScoreId")
    @Mapping(source = "innovation.id", target = "innovationId")
    @Mapping(source = "innovation.innovationName", target = "innovationName")
    @Mapping(source = "reviewer.fullName", target = "reviewerName")
    @Mapping(source = "reviewer.email", target = "reviewerEmail")
    @Mapping(target = "scoringDetails", ignore = true)
    @Mapping(target = "maxTotalScore", expression = "java(100)")
    public abstract InnovationScoreResponse toInnovationScoreResponse(ReviewScore reviewScore);

    @AfterMapping
    protected void convertScoringDetails(@MappingTarget InnovationScoreResponse response, ReviewScore reviewScore) {
        logger.info("convertScoringDetails called for reviewScore: {}", reviewScore.getId());
        JsonNode scoringDetailsNode = reviewScore.getScoringDetails();
        
        logger.info("scoringDetailsNode isNull: {}, isArray: {}", 
                scoringDetailsNode == null, 
                scoringDetailsNode != null ? scoringDetailsNode.isArray() : false);
        
        if (scoringDetailsNode != null && scoringDetailsNode.isArray()) {
            try {
                logger.info("Parsing array with size: {}", scoringDetailsNode.size());
                List<ScoreCriteriaDetail> details = new ArrayList<>();
                for (JsonNode node : scoringDetailsNode) {
                    logger.info("Parsing node: {}", node.toString());
                    ScoreCriteriaDetail detail = objectMapper.treeToValue(node, ScoreCriteriaDetail.class);
                    logger.info("Successfully parsed detail: criteriaId={}, selectedSubCriteriaId={}, score={}", 
                            detail.getCriteriaId(), detail.getSelectedSubCriteriaId(), detail.getScore());
                    details.add(detail);
                }
                logger.info("Successfully converted {} scoringDetails for reviewScore: {}", 
                        details.size(), reviewScore.getId());
                response.setScoringDetails(details);
            } catch (Exception e) {
                logger.error("Error converting scoringDetails from JsonNode for reviewScore: {}. Error: {}", 
                        reviewScore.getId(), e.getMessage(), e);
                response.setScoringDetails(null);
            }
        } else {
            logger.warn("scoringDetailsNode is null or not an array for reviewScore: {}", reviewScore.getId());
            response.setScoringDetails(null);
        }
    }
}