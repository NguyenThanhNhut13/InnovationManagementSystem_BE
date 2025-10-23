package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ActivityLog;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ActivityResponse;

import java.time.Duration;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ActivityResponseMapper {

    @Mapping(target = "timeAgo", source = "createdAt", qualifiedByName = "calculateTimeAgo")
    @Mapping(target = "iconType", source = "activityType", qualifiedByName = "getIconType")
    @Mapping(target = "iconColor", source = "activityType", qualifiedByName = "getIconColor")
    ActivityResponse toActivityResponse(ActivityLog activityLog);

    @Named("calculateTimeAgo")
    default String calculateTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();

        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    @Named("getIconType")
    default String getIconType(InnovationStatusEnum activityType) {
        return ActivityIconMapper.getIconType(activityType);
    }

    @Named("getIconColor")
    default String getIconColor(InnovationStatusEnum activityType) {
        return ActivityColorMapper.getIconColor(activityType);
    }
}
