package jiheon.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jiheon.userservice.auth.AuthInfo;
import jiheon.userservice.auth.UserRole;
import jiheon.userservice.dto.MsgDTO;
import jiheon.userservice.dto.UserInfoDTO;
import jiheon.userservice.repository.UserInfoRepository;
import jiheon.userservice.repository.entity.UserInfoEntity;
import jiheon.userservice.service.ISecurityService;
import jiheon.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:13000", "http://localhost:14000"},
        allowedHeaders = {"POST, GET"},
        allowCredentials = "true")
@Tag(name = "회원가입을 위한 API", description = "회원가입을 위한 API 설명입니다.")
@Slf4j
@RequestMapping(value = "/security")
@RequiredArgsConstructor
@RestController
public class SecurityController {

    private final ISecurityService securityService;

    private final UserInfoRepository userInfoRepository;

    // Spring Security에서 제공하는 비밀번호 암호화 객체(해시 함수)
    private final PasswordEncoder bCryptPasswordEncoder;

    /**
     * 회원가입 화면 이동
     */
    @GetMapping(value = "signUp")
    public String signUp() {
        log.info(this.getClass().getName() + ".signUp 페이지 이동");

        return "/security/signUp";
    }

    @GetMapping(value = "login")
    public String login() {
        log.info(this.getClass().getName() + ".login 실행");

        return "/security/login";
    }

    @GetMapping(value = "loginResult")
    public String loginResult(HttpServletRequest request) {
        log.info(this.getClass().getName() + ".loginResult 실행");

        return "/security/loginResult";
    }

    @PostMapping(value = "loginSuccess")
    public MsgDTO loginSuccess(@AuthenticationPrincipal AuthInfo authInfo, HttpSession session) {

        log.info(this.getClass().getName() + ".loginSuccess 실행");

        UserInfoDTO rDTO = Optional.ofNullable(authInfo.userInfoDTO())
                .orElseGet(() -> UserInfoDTO.builder().build());

        String userId = CmmUtil.nvl(rDTO.userId());
        String userRoles = CmmUtil.nvl(rDTO.roles());

        log.info("userId : " + userId);
        log.info("userRoles : " + userRoles);

        // 세션에 값 담기(수정)
        session.setAttribute(userId, "userId");
        session.setAttribute(userRoles, "userRoles");

        // 레디스에 세션정보 저장하기(수정)
        // try catch문으로 작성하기

        return MsgDTO.builder().build();
    }

    @PostMapping(value = "loginFail")
    public MsgDTO loginFail() {

        log.info(this.getClass().getName() + ".loginFail 실행");

        MsgDTO dto = MsgDTO.builder().result(0).msg("로그인 실패").build();

        log.info(this.getClass().getName() + ".loginFail 종료");

        return dto;
    }

    @PostMapping(value = "logout")
    public MsgDTO logout(HttpServletRequest request, HttpServletResponse response) {

        log.info(this.getClass().getName() + ".logout 실행");

        // 로그아웃 실행
        new SecurityContextLogoutHandler().logout(
                request, response, SecurityContextHolder.getContext().getAuthentication()
        );

        MsgDTO dto = MsgDTO.builder().result(1).msg("로그아웃 하였습니다").build();

        log.info(this.getClass().getName() + ".logout 끝");

        return dto;
    }

    @Operation(summary = "회원가입  API", description = "회원가입 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "insertUserInfo")
    public MsgDTO insertUserInfo(HttpServletRequest request) {

        log.info(this.getClass().getName() + ".insertUserInfo 실행");

        int res = 0; // 회원가입 결과
        String msg = ""; // 결과에 대한 메세지를 전달할 변수
        MsgDTO dto; // 결과 메세지 구조

        UserInfoDTO pDTO = null; // 웹에서 받는 정보를 저장할 변수

        try {
            String userId = CmmUtil.nvl(pDTO.userId());
            String userName = CmmUtil.nvl(pDTO.name());
            String nickName = CmmUtil.nvl(pDTO.nickName());
            String password = CmmUtil.nvl(pDTO.password());
            String email = CmmUtil.nvl(pDTO.email());
            String age = CmmUtil.nvl(pDTO.age());
            String gender = CmmUtil.nvl(pDTO.gender());
            String isKakao = CmmUtil.nvl(pDTO.isKakao());
            String roles = CmmUtil.nvl(pDTO.roles());
            // 회원의 userSeq 자동증가를 위해 DB Count
            int userSeq = (int) userInfoRepository.count();

            log.info("userId : " + userId);
            log.info("userName : " + userName);
            log.info("nickName : " + nickName);
            log.info("password : " + password);
            log.info("email : " + email);
            log.info("age : " + age);
            log.info("gender : " + gender);
            log.info("isKakao : " + isKakao);
            log.info("roles : " + roles);
            log.info("userSeq : " + userSeq);

            pDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .name(userName)
                    .nickName(nickName)
                    .password(bCryptPasswordEncoder.encode(password))
                    .email(email) // 암호화 해야하나?
                    .age(age)
                    .gender(gender)
                    .isKakao(isKakao)
                    .roles(UserRole.USER.getValue())
                    .userSeq(userSeq)
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

        return dto;
    }
}
