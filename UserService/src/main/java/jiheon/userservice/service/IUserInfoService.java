package jiheon.userservice.service;

import jiheon.userservice.dto.UserInfoDTO;

public interface IUserInfoService {

    // 회원정보 조회
    UserInfoDTO getUserInfo(String userId) throws Exception;

    // 회원정보 수정
    int updateUserInfo(UserInfoDTO pDTO) throws Exception;

    // 비밀번호 변경
    int updatePassword(UserInfoDTO pDTO) throws Exception;

    // 회원탈퇴
    int deleteUserInfo(UserInfoDTO pDTO) throws Exception;

}
