package jiheon.userservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jiheon.userservice.dto.UserInfoDTO;
import jiheon.userservice.repository.UserInfoRepository;
import jiheon.userservice.repository.entity.UserInfoEntity;
import jiheon.userservice.service.IUserInfoService;
import jiheon.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;

    @Override
    public UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getUserInfo Start!");

        // 회원아이디
        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("userId : " + userId);

        UserInfoDTO rDTO = null;

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {

            rDTO = new ObjectMapper().convertValue(rEntity,
                    new TypeReference<UserInfoDTO>() {
                    });
        }

        log.info(this.getClass().getName() + ".getUserInfo End!");

        return rDTO;
    }
}
