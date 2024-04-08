package jiheon.userservice.service;

import jiheon.userservice.dto.UserInfoDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface ISecurityService extends UserDetailsService {

    // 회원가입하기
    int insertUserInfo(UserInfoDTO pDTO);

    // 본인 회원 정보 조회
    UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception;

}
