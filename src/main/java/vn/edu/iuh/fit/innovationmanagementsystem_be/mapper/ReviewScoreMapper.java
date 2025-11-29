package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ScoreCriteriaDetail;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoreResponse;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewScoreMapper {

    Logger logger = LoggerFactory.getLogger(ReviewScoreMapper.class);
    ObjectMapper staticObjectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Mapping(source = "id", target = "reviewScoreId")
    @Mapping(source = "innovation.id", target = "innovationId")
    @Mapping(source = "innovation.innovationName", target = "innovationName")
    @Mapping(source = "reviewer.fullName", target = "reviewerName")
    @Mapping(source = "reviewer.email", target = "reviewerEmail")
    @Mapping(target = "scoringDetails", ignore = true)
    @Mapping(target = "maxTotalScore", expression = "java(100)")
    InnovationScoreResponse toInnovationScoreResponse(ReviewScore reviewScore);

    @AfterMapping
    default void convertScoringDetails(@MappingTarget InnovationScoreResponse response, ReviewScore reviewScore) {
        logger.info("convertScoringDetails called for reviewScore: {}", reviewScore.getId());

        JsonNode scoringDetailsNode = reviewScore.getScoringDetails();
        logger.info("scoringDetailsNode is null: {}, isArray: {}",
                scoringDetailsNode == null,
                scoringDetailsNode != null ? scoringDetailsNode.isArray() : false);

        if (scoringDetailsNode != null && scoringDetailsNode.isArray()) {
            try {
                logger.info("Array size: {}", scoringDetailsNode.size());

                List<ScoreCriteriaDetail> details = new ArrayList<>();
                for (JsonNode node : scoringDetailsNode) {
                    logger.info("Parsing node: {}", node.toString());
                    ScoreCriteriaDetail detail = staticObjectMapper.treeToValue(node, ScoreCriteriaDetail.class);
                    details.add(detail);
                }
                logger.info("Successfully converted {} details", details.size());
                response.setScoringDetails(details);
            } catch (Exception e) {
                logger.error("Error converting scoringDetails from JsonNode for reviewScore: {}",
                        reviewScore.getId(), e);
                response.setScoringDetails(null);
            }
        } else {
            logger.warn("scoringDetailsNode is null or not an array");
            response.setScoringDetails(null);
        }
    }
}