package jiheon.userservice.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public record KafkaDTO(
        String userId
) {
}
