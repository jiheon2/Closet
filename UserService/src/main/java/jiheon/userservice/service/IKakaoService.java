package jiheon.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jiheon.userservice.dto.KakaoDTO;
import jiheon.userservice.dto.KakaoUserDTO;
import org.springframework.http.ResponseEntity;

public interface IKakaoService {

    // 토큰 값 가져오기
    KakaoDTO getKakaoToken(String code) throws JsonProcessingException;

    // 사용자 정보 가져오기
    KakaoUserDTO getKakaoInfo(String token) throws JsonProcessingException;
}
