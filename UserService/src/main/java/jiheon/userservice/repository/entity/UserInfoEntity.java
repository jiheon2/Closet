package jiheon.userservice.repository.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@Document(collection = "userInfo")
public record UserInfoEntity(
        @Id
        String id, // 오브젝트 ID
        @Field
        int userSeq, // 회원번호
        @Field
        String userId, // 유저 아이디
        @Field
        String password, // 비밀번호
        @Field
        String name, // 회원 이름
        @Field
        String nickName, // 닉네임
        @Field
        String age, // 연령
        @Field
        String gender, // 성별
        @Field
        String email, // 이메일
        @Field
        String isKakao, // 회원 종류
        @Field
        String roles // 역할
) {}