package jiheon.communityservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.bson.types.Binary;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record PostDTO(
        String _id, // mongoDB ObjectId
        String userId, // 회원 아이디
        String nickName, // 닉네임
        int postSeq, // 게시글 번호
        String title, // 제목
        String contents, // 내용
        String regDt, // 등록일
        String chgDt, // 수정일
        String imageName, // 이미지 경로
        Binary image // 이미지
) {
}
