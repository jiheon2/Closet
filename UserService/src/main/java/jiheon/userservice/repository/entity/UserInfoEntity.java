package jiheon.userservice.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_INFO")
@DynamicUpdate
@DynamicInsert
@Entity
public class UserInfoEntity {
        @Id
        @Column(name = "USER_SEQ")
        private int userSeq; // 회원번호

        @NonNull
        @Column(name = "USER_ID")
        private String userId; // 회원 아이디

        @NonNull
        @Column(name = "PASSWORD")
        private String password; // 비밀번호

        @NonNull
        @Column(name = "NAME")
        private String name; // 이름

        @NonNull
        @Column(name = "NICKNAME")
        private String nickName; // 닉네임

        @NonNull
        @Column(name = "AGE")
        private String age; // 연령

        @NonNull
        @Column(name = "GENDER")
        private String gender; // 성별

        @NonNull
        @Column(name = "EMAIL")
        private String email; // 이메일

        @NonNull
        @Column(name = "ISKAKAO")
        private String isKakao; // 카카오회원인지 여부 / normal = 일반 회원, kakao = 카카오 회원

        @NonNull
        @Column(name = "ROLES")
        private String roles; // 회원 권한
}