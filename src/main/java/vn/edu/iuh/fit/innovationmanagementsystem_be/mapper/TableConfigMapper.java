package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.TableConfig;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.TableColumn;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TableConfigRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TableColumnRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TableConfigResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TableColumnResponse;

@Mapper(componentModel = "spring")
public interface TableConfigMapper {

    TableConfigResponse toTableConfigResponse(TableConfig tableConfig);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "formField", ignore = true)
    TableConfig toTableConfig(TableConfigRequest tableConfigRequest);

    TableColumnResponse toTableColumnResponse(TableColumn tableColumn);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tableConfig", ignore = true)
    TableColumn toTableColumn(TableColumnRequest tableColumnRequest);
}
