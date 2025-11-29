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
        JsonNode scoringDetailsNode = reviewScore.getScoringDetails();
        
        if (scoringDetailsNode != null && scoringDetailsNode.isArray()) {
            try {
                List<ScoreCriteriaDetail> details = new ArrayList<>();
                for (JsonNode node : scoringDetailsNode) {
                    ScoreCriteriaDetail detail = objectMapper.treeToValue(node, ScoreCriteriaDetail.class);
                    details.add(detail);
                }
                response.setScoringDetails(details);
            } catch (Exception e) {
                logger.error("Error converting scoringDetails from JsonNode for reviewScore: {}", 
                        reviewScore.getId(), e);
                response.setScoringDetails(null);
            }
        } else {
            response.setScoringDetails(null);
        }
    }
}