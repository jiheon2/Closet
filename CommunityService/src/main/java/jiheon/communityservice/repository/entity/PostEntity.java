package jiheon.communityservice.repository.entity;

import jiheon.communityservice.dto.PostDTO;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@Getter
@Document(collection = "post") // MongoDB 컬렉션 이름 지정
public class PostEntity {

    @Id // MongoDB primary key 지정
    private String id; // MongoDB ObjectId (String 형식으로 저장)
    @Field
    private String userId; // 회원아이디
    @Field
    private String nickName; // 닉네임
    @Field
    private long postSeq; // 게시글 번호
    @Field
    private String title; // 게시글 제목
    @Field
    private String contents; // 게시글 내용
    @Field
    private String regDt; // 등록시간
    @Field
    private String imagePath; // 이미지 경로

    @Override
    public String toString() {
        return "PostEntity {" +
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
