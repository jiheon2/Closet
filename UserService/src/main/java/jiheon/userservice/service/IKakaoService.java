package jiheon.userservice.service;

import org.springframework.http.ResponseEntity;

public interface IKakaoService {

    // 토큰 값 가져오기
    ResponseEntity<String> getKakaoToken(String code);

    // 사용자 정보 가져오기
    ResponseEntity<String> getKakaoInfo(String token);
}
