package jiheon.userservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jiheon.userservice.auth.JwtTokenProvider;
import jiheon.userservice.auth.JwtTokenType;
import jiheon.userservice.controller.response.CommonResponse;
import jiheon.userservice.dto.MsgDTO;
import jiheon.userservice.dto.TokenDTO;
import jiheon.userservice.dto.UserInfoDTO;
import jiheon.userservice.service.IUserInfoService;
import jiheon.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:11000", "http://localhost:12000", "http://localhost:13000"},
        allowedHeaders = {"POST", "GET", "FEIGN"},
        allowCredentials = "true")
@Tag(name = "로그인된 사용자들이 접근하는 API", description = "로그인된 사용자들이 접근하는 API 설명입니다.")
@Slf4j
@RequestMapping(value = "/user/v1")
@RequiredArgsConstructor
@RestController
public class UserInfoController {

    private final IUserInfoService userInfoService;

    private final JwtTokenProvider jwtTokenProvider;

    // Spring Security에서 제공하는 비밀번호 암호화 객체(해시 함수)
    private final PasswordEncoder bCryptPasswordEncoder;

    // Token에서 회원 아이디 가져오기
    @PostMapping(value = "getTokenInfo")
    private TokenDTO getTokenInfo(HttpServletRequest request) {

        log.info(this.getClass().getName() + ".토큰값 가져오기 시작");

        // JWT AccessToken 가져오기
        String jwtAccessToken = CmmUtil.nvl(jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS_TOKEN));
        log.info("jwtAccessToken : " + jwtAccessToken);

        TokenDTO dto = Optional.ofNullable(jwtTokenProvider.getTokenInfo(jwtAccessToken))
                .orElseGet(() -> TokenDTO.builder().build());

        log.info("TokenDTO : " + dto);

        log.info(this.getClass().getName() + ".토큰값 가져오기 종료");

        return dto;
    }

    @PostMapping(value = "userInfo")
    public ResponseEntity userInfo(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".userInfo Start!");

        // AccessToken에 저장된 회원아이디 가져오기
        String userId = CmmUtil.nvl(this.getTokenInfo(request).userId());

        log.info("userId : " + userId);

        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        // 회원정보 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserInfo(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info(this.getClass().getName() + ".userInfo End!");

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));
    }
    @PostMapping(value = "loginCheck")
    public ResponseEntity loginCheck(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".loginCheck Start!");

        // AccessToken에 저장된 회원아이디 가져오기
        String userId = CmmUtil.nvl(this.getTokenInfo(request).userId());

        log.info("userId : " + userId);

        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        log.info(this.getClass().getName() + ".loginCheck End!");

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), pDTO));
    }



    @PostMapping(value = "updateUserInfo")
    public ResponseEntity updateUserInfo(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".updateUserInfo Start!");

        // 성공 : 1, 실패 : 0
        int res = 0;
        String msg = ""; // 결과에 대한 메세지를 전달할 변수
        MsgDTO dto; // 결과 메세지 구조

        try {
            String userId = CmmUtil.nvl(this.getTokenInfo(request).userId());
            String nickName = CmmUtil.nvl(request.getParameter("nickName"));
            String email = CmmUtil.nvl(request.getParameter("email"));
            String age = CmmUtil.nvl(request.getParameter("age"));

            log.info("userId : " + userId);
            log.info("nickName : " + nickName);
            log.info("email : " + email);
            log.info("age : " + age);

            UserInfoDTO pDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .nickName(nickName)
                    .age(age)
                    .email(email)
                    .build();

            // 회원정보 수정하기
            res = userInfoService.updateUserInfo(pDTO);

            if (res == 1) {
                msg = "회원정보가 수정되었습니다.";
            } else {
                msg = "오류로 인해 회원정보 수정에 실패하였습니다.";
            }
        } catch (Exception e) {
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();

            log.info(this.getClass().getName() + ".updateUserInfo End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @PostMapping(value = "updatePassword")
    public ResponseEntity updatePassword(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".updatePassword Start!");

        int res = 0; // 성공 : 1, 실패 : 0
        String msg = ""; // 결과에 대한 메세지를 전달할 변수
        MsgDTO dto; // 결과 메세지 구조

        try {
            String userId = CmmUtil.nvl(this.getTokenInfo(request).userId());
            String newPassword = CmmUtil.nvl(request.getParameter("newPassword"));
            String oldPassword = CmmUtil.nvl(request.getParameter("oldPassword"));

            log.info("userId : " + userId);
            log.info("oldPassword : " + oldPassword);
            log.info("newPassword : " + newPassword);

            UserInfoDTO cDTO =  UserInfoDTO.builder().userId(userId).build();

            // 기존 비밀번호 가져오기
            UserInfoDTO rDTO = userInfoService.getUserInfo(cDTO);

            // 스프링 시큐리티에서 제공하는 비밀번호 매치
            boolean checkPw = bCryptPasswordEncoder.matches(oldPassword, rDTO.password());

            if (!checkPw) {
                msg = "기존의 비밀번호가 일치하지 않습니다.";
            } else {

                UserInfoDTO pDTO = UserInfoDTO.builder()
                        .userId(userId)
                        .password(bCryptPasswordEncoder.encode(newPassword))
                        .build();

                res = userInfoService.updatePassword(pDTO);
                msg = "비밀번호 수정에 성공하였습니다.";
            }

        } catch (Exception e) {
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();

            log.info(this.getClass().getName() + ".updatePassword End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @PostMapping(value = "deleteUserInfo")
    public ResponseEntity deleteUserInfo(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".deleteUserInfo Start!");

        int res = 0; // 성공 : 1, 실패 : 0
        String msg = ""; // 결과에 대한 메세지를 전달할 변수
        MsgDTO dto; // 결과 메세지 구조

        try {
            // 토큰에서 userId 가져오기
            String userId = CmmUtil.nvl(this.getTokenInfo(request).userId());
            int userSeq = this.getTokenInfo(request).userSeq();

            UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).userSeq(userSeq).build();

            // 회원탈퇴
            res = userInfoService.deleteUserInfo(pDTO);

            if (res == 1) {
                msg = "회원탈퇴가 완료되었습니다.";
            } else {
                msg = "오류로 인해 회원탈퇴가 완료되지 않았습니다.";
            }
        } catch (Exception e) {
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();

            log.info(this.getClass().getName() + ".deleteUserInfo End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }
}
