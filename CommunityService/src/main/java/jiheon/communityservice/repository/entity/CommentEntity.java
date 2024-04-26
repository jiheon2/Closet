package jiheon.communityservice.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "comment") // MongoDB 컬렉션 이름 지정
public class CommentEntity {
    @Id // MongoDB primary key 지정
    private String id; // MongoDB ObjectId (String 형식으로 저장)
    @Field
    private int postSeq; // 게시글 번호
    @Field
    private String userId; // 회원아이디
    @Field
    private String nickName; // 닉네임
    @Field
    private String comment; // 댓글 내용
    @Field
    private LocalDateTime regDt; // 등록시간
    @Field
    private LocalDateTime chgDt; // 수정시간
}
