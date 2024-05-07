package jiheon.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record KakaoDTO(
        String access_token, // 액세스토큰
        String token_type, // 토큰 종류
        String refresh_token, // 리프레시 토큰
        int expires_in,
        String scope,
        int refresh_token_expires_in
) {
}
