package jiheon.closetservice.dto;

import lombok.Builder;

@Builder
public record KafkaDTO(
        String userId
) {
}
