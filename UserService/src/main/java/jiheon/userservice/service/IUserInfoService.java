package jiheon.userservice.service;

import jiheon.userservice.dto.UserInfoDTO;

public interface IUserInfoService {

    UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception;
}
