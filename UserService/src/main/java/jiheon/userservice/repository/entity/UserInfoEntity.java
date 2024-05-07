package jiheon.userservice.repository.entity;

import jakarta.persistence.*;
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
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "USER_SEQ")
        private int userSeq; // 회원번호

        @Column(name = "USER_ID")
        private String userId; // 회원 아이디

        @Column(name = "PASSWORD")
        private String password; // 비밀번호

        @Column(name = "NICKNAME")
        private String nickName; // 닉네임

        @Column(name = "AGE")
        private String age; // 연령

        @Column(name = "GENDER")
        private String gender; // 성별

        @Column(name = "EMAIL")
        private String email; // 이메일

        @Column(name = "ISKAKAO")
        private String isKakao; // 카카오회원인지 여부 / normal = 일반 회원, kakao = 카카오 회원

        @Column(name = "ROLES")
        private String roles; // 회원 권한
}