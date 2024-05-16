package jiheon.communityservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record PostDTO(
        String id, // mongoDB ObjectId
        String userId, // 회원 아이디
        String nickName, // 닉네임
        long postSeq, // 게시글 번호
        String title, // 제목
        String contents, // 내용
        String regDt, // 등록일
        String imagePath // 이미지 경로
) {
    @Override
    public String toString() {
        return "PostDTO {" +
                "id : " + id +
                ", userId : " + userId +
                ", nickName : " + nickName +
                ", postSeq : " + postSeq +
                ", title : " + title +
                ", contents : " + contents +
                ", regDt : " + regDt +
                ", imagePath : " + imagePath +
                "}";
    }
}
