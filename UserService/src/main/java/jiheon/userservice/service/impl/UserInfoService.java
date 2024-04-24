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
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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

    @Override
    public int updateUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".updateUserInfo Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        String userId = CmmUtil.nvl(pDTO.userId());
        String name = CmmUtil.nvl(pDTO.name());
        String nickName = CmmUtil.nvl(pDTO.nickName());
        String email = CmmUtil.nvl(pDTO.email());
        String age = CmmUtil.nvl(pDTO.age());

        log.info("userId : " + userId);
        log.info("name : " + name);
        log.info("nickName : " + nickName);
        log.info("email : " + email);
        log.info("age : " + age);

        // 이메일 수정 시 회원가입하는 데이터와 중복방지를 위해 DB 조회
        if (userInfoRepository.existsByEmail(email) || userInfoRepository.existsByNickName(nickName)) {
            log.warn("이메일 혹은 닉네임이 중복되어 회원정보 수정을 할 수 없습니다.");
            return 0;

        } else {
            // 정보 수정
            userInfoRepository.save(
                    UserInfoEntity.builder()
                            .name(name)
                            .nickName(nickName)
                            .email(email)
                            .age(age)
                            .build()
            );
            log.info("회원정보 수정을 성공하였습니다.");

            // 수정된 값 확인
            Optional<UserInfoEntity> userInfoEntity = userInfoRepository.findByUserId(userId);

            log.info("수정된 값");
            log.info("name : " + userInfoEntity.get().getName());
            log.info("nickName : " + userInfoEntity.get().getNickName());
            log.info("email : " + userInfoEntity.get().getEmail());
            log.info("age : " + userInfoEntity.get().getAge());

            res = 1;
        }

        log.info(this.getClass().getName() + ".updateUserInfo End!");

        return res;
    }

    @Override
    public int updatePassword(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".updatePassword Start!");

        // 성공 : 1, 실패 : 0
        int res = 0;

        String password = CmmUtil.nvl(pDTO.password());

        log.info("password : " + password);

        if (StringUtils.isEmpty(password)) {
            log.warn("비밀번호 값이 존재하지 않습니다.");
            return 0;

        } else {
            // 비밀번호 수정
            userInfoRepository.save(
                    UserInfoEntity.builder().password(password).build()
            );
            log.info("비밀번호를 수정했습니다.");

            res = 1;
        }

        log.info(this.getClass().getName() + ".updatePassword End!");

        return res;
    }

    @Override
    public int deleteUserInfo(String userId) throws Exception {

        log.info(this.getClass().getName() + ".deleteUserInfo Start!");

        // 성공 : 1, 실패 : 0
        int res = 0;

        if (StringUtils.isEmpty(userId)) {
            log.warn("아이디 값이 존재하지 않습니다.");
            return 0;

        } else {
            // 회원 삭제
            userInfoRepository.deleteById(userId);
            log.info(userId + "님이 회원탈퇴를 하였습니다.");

            res = 1;
        }

        log.info(this.getClass().getName() + ".deleteUserInfo End!");

        return res;
    }
}
