package jiheon.communityservice.dto;

import lombok.Builder;

@Builder
public record KafkaDTO(
        String userId
) {
}
