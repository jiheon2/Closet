package jiheon.userservice.dto;

import lombok.Builder;

@Builder
public record UserInfoDTO(
        // 자동으로 직렬화를 해주므로 Serializable 인터페이스를 구현 안해도 된다!

        int userSeq, // 회원번호
        String userId, // 회원 아이디
        String password, // 비밀번호
        String name, // 이름
        String nickName, // 닉네임
        String age, // 연령
        String gender, // 성별
        String email, // 이메일
        String isKakao, // 카카오회원인지 여부 / normal = 일반 회원, kakao = 카카오 회원
        String roles // 회원 권한
) {
}

