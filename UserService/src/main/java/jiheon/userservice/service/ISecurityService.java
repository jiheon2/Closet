package jiheon.userservice.service;

import jiheon.userservice.dto.UserInfoDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface ISecurityService extends UserDetailsService {
    // 회원가입하기
    int insertUserInfo(UserInfoDTO pDTO);

    // 아이디 중복체크
    boolean existsByUserId(String userId) throws Exception;

    // 닉네임 중복체크
    boolean existsByNickName(String nickName) throws Exception;

    // 이메일 중복체크
    boolean existsByEmail(String email) throws Exception;

    // ID 찾기
    UserInfoDTO findId(UserInfoDTO pDTO) throws Exception;

    // PW 찾기
    int findPw(UserInfoDTO pDTO) throws Exception;
}
