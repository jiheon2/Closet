package jiheon.communityservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record CommentDTO(
        String id, // MongoDB ObjectId
        String commentSeq, // 댓글 번호
        String postSeq, // 게시글 번호
        String userId, // 회원아이디
        String nickName, // 닉네임
        String comment // 댓글 내용
) {
}
