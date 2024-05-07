package jiheon.userservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Builder
public record KakaoUserDTO(
        String id,
        String connected_at,
        Properties properties,
        KakaoAccount kakao_account,
        LinkedHashMap<String, Object> additional_properties
) {
    @Data
    public static class Properties {
        private String nickname;
        private Map<String, Object> additional_properties = new LinkedHashMap<>();
    }

    @Data
    public static class KakaoAccount {
        private Boolean profile_nickname_needs_agreement;
        private Profile profile;
        private Boolean email_needs_agreement;
        private Boolean is_email_valid;
        private Boolean is_email_verified;
        private Boolean has_email;
        private String email;
        private Boolean has_age_range;
        private Boolean age_range_needs_agreement;
        private String age_range;
        private Boolean has_gender;
        private Boolean gender_needs_agreement;
        private String gender;
        private Map<String, Object> additional_properties = new LinkedHashMap<>();

        @Data
        public static class Profile {
            private String nickname;
            private Boolean is_default_nickname;
            private Map<String, Object> additional_properties = new LinkedHashMap<>();
        }
    }
}
