package jiheon.closetservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ClosetDTO(
        String id, // MongoDB 오브젝트 ID
        String userId, // 회원 ID
        String parts, // 부위
        long photoSeq, // 이미지 번호
        String photoUrl // 이미지가 저장된 url
) {
}
