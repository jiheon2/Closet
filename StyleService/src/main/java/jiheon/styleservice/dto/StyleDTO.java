package jiheon.styleservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record StyleDTO(
        String mainStyle,
        String subStyle,
        String mainCategory,
        String subCategory,
        String size,
        String gender,
        String age,
        String color
) {
}
