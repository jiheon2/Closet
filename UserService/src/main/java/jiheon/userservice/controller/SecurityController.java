package jiheon.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jiheon.userservice.auth.AuthInfo;
import jiheon.userservice.auth.JwtTokenProvider;
import jiheon.userservice.auth.JwtTokenType;
import jiheon.userservice.auth.UserRole;
import jiheon.userservice.controller.response.CommonResponse;
import jiheon.userservice.dto.MsgDTO;
import jiheon.userservice.dto.UserInfoDTO;
import jiheon.userservice.service.IRedisService;
import jiheon.userservice.service.ISecurityService;
import jiheon.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = {"http://gateway:11000", "http://front:12000", "http://user:13000"},
        allowedHeaders = {"*"},
        allowCredentials = "true",
        methods = {RequestMethod.POST, RequestMethod.GET},
        originPatterns = {"security/**"})
@Tag(name = "Security Service API", description = "회원가입 및 로그인을 위한 API 설명입니다.")
@Slf4j
@RequestMapping(value = "/security/v1")
@RequiredArgsConstructor
@RestController
public class SecurityController {

    @Value("${jwt.token.access.valid.time}")
    private long accessTokenValidTime;

    @Value("${jwt.token.access.name}")
    private String accessTokenName;

    private final JwtTokenProvider jwtTokenProvider;

    private final IRedisService redisService;

    private final ISecurityService securityService;

    // Spring Security에서 제공하는 비밀번호 암호화 객체(해시 함수)
    private final PasswordEncoder bCryptPasswordEncoder;

    @Operation(summary = "로그인 성공 API", description = "로그인 성공시 실행되는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "loginSuccess")
    public ResponseEntity<CommonResponse> loginSuccess(
            @AuthenticationPrincipal AuthInfo authInfo, HttpServletResponse response) throws Exception {

        log.info(this.getClass().getName() + ".loginSuccess 실행");

        // Spring Security에 저장된 정보 가져오기
        UserInfoDTO rDTO = Optional.ofNullable(authInfo.userInfoDTO())
                .orElseGet(() -> UserInfoDTO.builder().build());

        String userId = CmmUtil.nvl(rDTO.userId());
        String userRoles = CmmUtil.nvl(rDTO.roles());
        String userName = CmmUtil.nvl(rDTO.nickName());
        int userSeq = rDTO.userSeq();

        log.info("userId : " + userId);
        log.info("userRoles : " + userRoles);
        log.info("userSeq : " + userSeq);

        // Access Token 생성
        String accessToken = jwtTokenProvider.createToken(userId, userRoles, userSeq, JwtTokenType.ACCESS_TOKEN);
        log.info("accessToken : " + accessToken);

        ResponseCookie cookie = ResponseCookie.from(accessTokenName, accessToken)
                //.domain("localhost")
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
        String refreshToken = jwtTokenProvider.createToken(userId, userRoles, userSeq, JwtTokenType.REFRESH_TOKEN);

        log.info("refreshToken : " + refreshToken);

        // 레디스에 리프레시 토큰 저장
        redisService.setValues(refreshToken, userId);

        // 결과 메시지 전달하기
        MsgDTO dto = MsgDTO.builder().result(1).msg(userName + "님 로그인이 성공하였습니다.").build();

        log.info(this.getClass().getName() + ".loginSuccess End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

    @Operation(summary = "로그인 실패 API", description = "로그인 실패 시 MSG 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "loginFail")
    public ResponseEntity<CommonResponse> loginFail() {

        log.info(this.getClass().getName() + ".loginFail 실행");

        MsgDTO dto = MsgDTO.builder().result(0).msg("로그인 실패").build();

        log.info(this.getClass().getName() + ".loginFail 종료");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }


    @Operation(summary = "회원가입  API", description = "회원가입 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "signUp")
    public ResponseEntity<CommonResponse> singUp(HttpServletRequest request) {

        log.info(this.getClass().getName() + ".signUp 실행");

        int res = 0; // 회원가입 결과
        String msg = ""; // 결과에 대한 메세지를 전달할 변수
        MsgDTO dto; // 결과 메세지 구조

        UserInfoDTO pDTO = null; // 웹에서 받는 정보를 저장할 변수

        try {

            String userId = CmmUtil.nvl(request.getParameter("userId"));
            String nickName = CmmUtil.nvl(request.getParameter("nickName"));
            String password = CmmUtil.nvl(request.getParameter("password"));
            String email = CmmUtil.nvl(request.getParameter("email"));
            String age = CmmUtil.nvl(request.getParameter("age"));
            String gender = CmmUtil.nvl(request.getParameter("gender"));
            String isKakao = CmmUtil.nvl(request.getParameter("isKakao"));
            String roles = CmmUtil.nvl(request.getParameter("roles"));

            log.info("userId : " + userId);
            log.info("nickName : " + nickName);
            log.info("password : " + password);
            log.info("email : " + email);
            log.info("age : " + age);
            log.info("gender : " + gender);
            log.info("isKakao : " + isKakao);
            log.info("roles : " + roles);

            pDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .nickName(nickName)
                    .password(bCryptPasswordEncoder.encode(password))
                    .email(email) // 암호화 해야하나?
                    .age(age)
                    .gender(gender)
                    .isKakao(isKakao)
                    .roles(UserRole.USER.getValue())
                    .build();

            res = securityService.insertUserInfo(pDTO); // 회원가입

            log.info("회원가입 결과(res) : " + res);

            if (res == 1) {
                msg = "회원가입되었습니다.";
            } else if (res == 2) {
                msg = "이미 가입된 아이디 입니다";
            } else {
                msg = "오류로 인해 회원가입이 실패하였습니다.";
            }
        } catch (Exception e) {
            msg = "실패하였습니다 : " + e;
            res = 2;
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();

            log.info(this.getClass().getName() + ".insertUserInfo 종료");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @Operation(summary = "아이디 중복체크 API", description = "DB에 아이디가 중복으로 존재하는지 체크하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "checkId")
    public ResponseEntity<CommonResponse> checkId(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".CheckId API Start");

        String userId = CmmUtil.nvl(request.getParameter("userId"));

        int res = 0;

        MsgDTO dto = null;

        if (securityService.existsByUserId(userId)) {
            dto = MsgDTO.builder().result(res).msg("이미 존재하는 아이디입니다.").build();
        } else {
            res = 1;
            dto = MsgDTO.builder().result(res).msg("가입 가능한 아이디 입니다.").build();
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

    @Operation(summary = "아이디 중복체크 API", description = "DB에 아이디가 중복으로 존재하는지 체크하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "checkNickName")
    public ResponseEntity<CommonResponse> checkNickname(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".CheckNickname API Start");

        String nickName = CmmUtil.nvl(request.getParameter("nickName"));

        int res = 0;

        MsgDTO dto = null;

        if (securityService.existsByNickName(nickName)) {
            dto = MsgDTO.builder().result(res).msg("이미 존재하는 닉네임입니다.").build();
        } else {
            res = 1;
            dto = MsgDTO.builder().result(res).msg("가입 가능한 닉네임입니다.").build();
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

    @Operation(summary = "아이디 중복체크 API", description = "DB에 아이디가 중복으로 존재하는지 체크하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "checkEmail")
    public ResponseEntity<CommonResponse> checkEmail(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".CheckEmail API Start");

        String email = CmmUtil.nvl(request.getParameter("email"));

        int res = 0;

        MsgDTO dto = null;

        if (securityService.existsByEmail(email)) {
            dto = MsgDTO.builder().result(res).msg("이미 존재하는 이메일입니다.").build();
        } else {
            res = 1;
            dto = MsgDTO.builder().result(res).msg("가입 가능한 이메일입니다.").build();
        }

        log.info(dto.toString());

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

    // 아이디 찾기
    @Operation(summary = "아이디 찾기 API", description = "이름과 이메일을 기준으로 DB에 아이디가 존재하는지 체크하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "findId")
    public ResponseEntity<CommonResponse> findId(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".아이디 찾기");

        String msg = ""; // 결과에 대한 메세지를 전달할 변수
        int res = 0;
        MsgDTO dto; // 결과 메세지 구조

        try {

            String nickName = CmmUtil.nvl(request.getParameter("nickName"));
            String email = CmmUtil.nvl(request.getParameter("email"));

            log.info("nickName : " + nickName);
            log.info("email : " + email);

            UserInfoDTO pDTO = UserInfoDTO.builder()
                    .nickName(nickName)
                    .email(email)
                    .build();

            UserInfoDTO rDTO = securityService.findId(pDTO);
            log.info("rDTO : " + rDTO);

            if (rDTO == null) {
                msg = "아이디가 존재하지 않습니다.";
                res = 0;
            } else {
                msg = "회원님의 아이디는 " + rDTO.userId() + "입니다";
                res = 1;
            }

        } catch (Exception e) {

            msg = "서버 오류가 발생하였습니다. 다시 시도해주시길 바랍니다.";
            log.info(e.toString());

        } finally {

            dto = MsgDTO.builder().result(res).msg(msg).build();

            log.info(this.getClass().getName() + "아이디 찾기 종료");

        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }


    @Operation(summary = "비밀번호 찾기 API", description = "아이디와 이메일을 기준으로 비밀번호를 재설정해주는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "findPw")
    public ResponseEntity<CommonResponse> findPw(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".비밀번호 찾기 시작");

        String msg = ""; // 결과에 대한 메세지를 전달할 변수
        MsgDTO dto; // 결과 메세지 구조
        int res = 0; // 성공 : 1, 실패 : 0

        try {

            String userId = CmmUtil.nvl(request.getParameter("userId"));
            String email = CmmUtil.nvl(request.getParameter("email"));

            log.info("userId : " + userId);
            log.info("email : " + email);

            UserInfoDTO pDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .email(email)
                    .build();

            res = securityService.findPw(pDTO);

            if (res == 1) {
                msg = "회원님의 비밀번호가 0000으로 설정되었습니다. 비밀번호를 변경해주시길 바랍니다.";
            } else {
                res = 0;
                msg = "입력하신 정보가 존재하지 않습니다.";
            }
        } catch (Exception e) {
            msg = "서버측 오류가 발생하였습니다. 다시 시도해주시길 바랍니다.";
            log.info(e.toString());
        } finally {
            log.info(this.getClass().getName() + ".비밀번호 찾기 종료");
            dto = MsgDTO.builder().result(res).msg(msg).build();
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }
}
