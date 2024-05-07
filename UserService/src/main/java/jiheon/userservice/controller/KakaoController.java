package jiheon.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import jiheon.userservice.dto.KakaoDTO;
import jiheon.userservice.service.IKakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/kakao")
@Controller
public class KakaoController {

    private final IKakaoService iKakaoService;

    @GetMapping(value = "kakaoLogin")
    public String kakaoLogin(@RequestParam("code") String code) throws Exception {

        log.info(this.getClass().getName() + ".kakaoLogin");

        // 토큰 가져오기
        ResponseEntity<String> response = iKakaoService.getKakaoToken(code);

        // 토큰값 dto에 저장
        KakaoDTO pDTO = new ObjectMapper().readValue(response.getBody(), KakaoDTO.class);

        String token = pDTO.access_token();
        log.info("accessToken : " + token);

        // 사용자 정보 받아오기


        return "redirect:http://localhost:12000/security/login.html";
    }
}
