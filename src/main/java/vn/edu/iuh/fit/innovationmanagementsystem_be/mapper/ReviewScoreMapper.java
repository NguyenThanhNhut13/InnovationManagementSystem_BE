package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
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

    private final Logger logger = LoggerFactory.getLogger(ReviewScoreMapper.class);
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
        logger.info("Converting scoringDetails for reviewScore: {}", reviewScore.getId());

        JsonNode scoringDetailsNode = reviewScore.getScoringDetails();

        logger.info("scoringDetailsNode - isNull: {}, type: {}",
                scoringDetailsNode == null,
                scoringDetailsNode != null ? scoringDetailsNode.getNodeType() : "null");

        if (scoringDetailsNode != null && !scoringDetailsNode.isNull()) {
            try {
                if (scoringDetailsNode.isArray()) {
                    logger.info("Converting array with {} elements", scoringDetailsNode.size());

                    List<ScoreCriteriaDetail> details = objectMapper.convertValue(
                            scoringDetailsNode,
                            new TypeReference<List<ScoreCriteriaDetail>>() {
                            });

                    logger.info("Successfully converted {} scoring details", details.size());
                    response.setScoringDetails(details);
                } else {
                    logger.warn("scoringDetailsNode is not an array for reviewScore: {}, type: {}",
                            reviewScore.getId(), scoringDetailsNode.getNodeType());
                    response.setScoringDetails(new ArrayList<>());
                }
            } catch (Exception e) {
                logger.error("Error converting scoringDetails from JsonNode for reviewScore: {}",
                        reviewScore.getId(), e);
                logger.error("Raw JSON: {}", scoringDetailsNode.toString());
                response.setScoringDetails(null);
            }
        } else {
            logger.info("scoringDetailsNode is null or isNull() for reviewScore: {}", reviewScore.getId());
            response.setScoringDetails(null);
        }
    }
}