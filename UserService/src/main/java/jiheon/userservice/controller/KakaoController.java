package jiheon.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletResponse;
import jiheon.userservice.auth.JwtTokenProvider;
import jiheon.userservice.auth.JwtTokenType;
import jiheon.userservice.auth.UserRole;
import jiheon.userservice.dto.KakaoDTO;
import jiheon.userservice.dto.KakaoUserDTO;
import jiheon.userservice.dto.UserInfoDTO;
import jiheon.userservice.service.IKakaoService;
import jiheon.userservice.service.IRedisService;
import jiheon.userservice.service.ISecurityService;
import jiheon.userservice.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/kakao")
@Controller
public class KakaoController {

    private final IKakaoService kakaoService;
    private final IUserInfoService userInfoService;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final ISecurityService securityService;
    private final JwtTokenProvider jwtTokenProvider;
    private final IRedisService redisService;

    @Value("${jwt.token.access.valid.time}")
    private long accessTokenValidTime;

    @Value("${jwt.token.access.name}")
    private String accessTokenName;

    @GetMapping(value = "kakaoLogin")
    public String kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) throws Exception {

        log.info(this.getClass().getName() + ".kakaoLogin");

        String url = "";

        try {
            // 토큰 가져오기
            KakaoDTO tokenRes = kakaoService.getKakaoToken(code);
            String token = tokenRes.access_token();

            log.info("token : " + token);

            // 사용자 정보 가져오기
            KakaoUserDTO infoRes = kakaoService.getKakaoInfo(token);
            log.info("카카오 회원정보");
            log.info("카카오 ID : " + infoRes.id());
            log.info("카카오 이메일 : " + infoRes.kakao_account().getEmail());
            log.info("카카오 나이 : " + infoRes.kakao_account().getAge_range());
            log.info("카카오 성별 : " + infoRes.kakao_account().getGender());
            log.info("카카오 닉네임 : " + infoRes.kakao_account().getProfile().getNickname());

            UserInfoDTO dto = userInfoService.getUserInfo(infoRes.id());

            if (dto == null) {
                UserInfoDTO pDTO = UserInfoDTO.builder()
                        .userId(infoRes.id())
                        .age(infoRes.kakao_account().getAge_range())
                        .nickName(infoRes.kakao_account().getProfile().getNickname())
                        .email(infoRes.kakao_account().getEmail())
                        .gender(infoRes.kakao_account().getGender())
                        .isKakao("kakao")
                        .roles(UserRole.USER.getValue())
                        .password(bCryptPasswordEncoder.encode("0000"))
                        .build();

                int res = securityService.insertUserInfo(pDTO);

                log.info("회원가입 결과(res) : " + res);

                if (res == 1) {
                    log.info("회원가입되었습니다.");
                    url = "redirect:http://localhost:12000/security/login.html";
                } else if (res == 2) {
                    log.info("이미 가입된 아이디입니다");
                }
            } else {

                String accessToken = jwtTokenProvider.createToken(infoRes.id(), UserRole.USER.getValue(), dto.userSeq(), JwtTokenType.ACCESS_TOKEN);
                log.info("accessToken : " + accessToken);

                ResponseCookie cookie = ResponseCookie.from(accessTokenName, accessToken)
                        .domain("localhost")
                        .path("/")
                        .maxAge(accessTokenValidTime)
                        .httpOnly(true)
                        .build();

                // 기존쿠키 모두 삭제하고, Cookie에 Access Token 저장하기
                response.setHeader("Set-Cookie", cookie.toString());

                cookie = null;

                // Refresh Token 생성
                // Refresh Token은 보안상 노출되면, 위험하기에 Refresh Token은 DB에 저장하고,
                // DB를 조회하기 위한 값만 Refresh Token으로 생성함
                // Refresh Token은 Access Token에 비해 만료시간을 길게 설정함
                String refreshToken = jwtTokenProvider.createToken(infoRes.id(), UserRole.USER.getValue(), dto.userSeq(), JwtTokenType.REFRESH_TOKEN);

                log.info("refreshToken : " + refreshToken);

                // 레디스에 리프레시 토큰 저장
                redisService.setValues(refreshToken, infoRes.id());

                log.info("로그인 되었습니다.");
                url = "redirect:http://localhost:12000/security/afterLogin.html";
            }
        } catch (Exception e) {
            log.info("에러 : " + e);
        }

        return url;
    }
}
