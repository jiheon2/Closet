package jiheon.communityservice.repository.entity;

import lombok.Builder;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@Document(collection = "post") // MongoDB 컬렉션 이름 지정
public class PostEntity {

    @Id // MongoDB primary key 지정
    private String id; // MongoDB ObjectId (String 형식으로 저장)
    @Field
    private String userId; // 회원아이디
    @Field
    private String nickName; // 닉네임
    @Field
    private int postSeq; // 게시글 번호
    @Field
    private String title; // 게시글 제목
    @Field
    private String contents; // 게시글 내용
    @Field
    private String regDt; // 등록시간
    @Field
    private String chgDt; // 수정시간
    @Field
    private String imageName; // 이미지 경로

    @Field
    private Binary image; // 이미지
}
