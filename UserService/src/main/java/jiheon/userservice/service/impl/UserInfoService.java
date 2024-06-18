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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-delete-topic";
    @Override
    public UserInfoDTO getUserInfo(String userId) throws Exception {

        log.info(this.getClass().getName() + ".getUserInfo Start!");

        log.info("userId : " + userId);

        UserInfoDTO rDTO = null;

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            // FIXME 여기 .get() 해줘야 하는거 아니야??
            rDTO = new ObjectMapper().convertValue(rEntity.get(),
                    new TypeReference<UserInfoDTO>() {
                    });
        }

        log.info(this.getClass().getName() + ".getUserInfo End!");

        return rDTO;
    }

    @Override
    @Transactional
    public int updateUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".updateUserInfo Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        String userId = CmmUtil.nvl(pDTO.userId());
        String nickName = CmmUtil.nvl(pDTO.nickName());
        String email = CmmUtil.nvl(pDTO.email());
        String age = CmmUtil.nvl(pDTO.age());

        log.info("userId : " + userId);
        log.info("nickName : " + nickName);
        log.info("email : " + email);
        log.info("age : " + age);

        try {
            // 정보 가져오기
            Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

            // 수정한 값 반영
            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userSeq(rEntity.get().getUserSeq())
                    .userId(userId)
                    .nickName(nickName)
                    .email(email)
                    .age(age)
                    .password(rEntity.get().getPassword())
                    .roles(rEntity.get().getRoles())
                    .gender(rEntity.get().getGender())
                    .isKakao(rEntity.get().getIsKakao())
                    .build();

            // 정보 수정
            userInfoRepository.save(pEntity);
            log.info("회원정보 수정을 성공하였습니다.");

            // 수정된 값 확인
            Optional<UserInfoEntity> userInfoEntity = userInfoRepository.findByUserId(userId);

            log.info("수정된 값");
            log.info("nickName : " + userInfoEntity.get().getNickName());
            log.info("email : " + userInfoEntity.get().getEmail());
            log.info("age : " + userInfoEntity.get().getAge());

            if (userInfoRepository.countByEmail(email) + userInfoRepository.countByNickName(nickName) >= 5) {
                log.info("새로운 회원가입으로 인해 값이 중복되므로 롤백처리");
                log.info("이메일 중복 조회 : " + userInfoRepository.countByEmail(email));
                log.info("닉네임 중복 조회 : " + userInfoRepository.countByNickName(nickName));
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                res = 0;
            } else {
                log.info("회원정보 수정완료");
                res = 1;
            }
        } catch (Exception e) {
            log.info("회원정보 수정 중 에러 발생 : " + e.toString());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        log.info(this.getClass().getName() + ".updateUserInfo End!");

        return res;
    }

    @Override
    public int updatePassword(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".updatePassword Start!");

        // 성공 : 1, 실패 : 0
        int res;

        try {
            String userId = CmmUtil.nvl(pDTO.userId());
            String password = CmmUtil.nvl(pDTO.password());

            log.info("userId : " + userId);
            log.info("password : " + password);

            // 정보 가져오기
            Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

            // 수정한 값 반영
            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userSeq(rEntity.get().getUserSeq())
                    .userId(userId)
                    .nickName(rEntity.get().getNickName())
                    .email(rEntity.get().getEmail())
                    .age(rEntity.get().getAge())
                    .password(password)
                    .roles(rEntity.get().getRoles())
                    .gender(rEntity.get().getGender())
                    .isKakao(rEntity.get().getIsKakao())
                    .build();

            // 비밀번호 수정
            userInfoRepository.save(pEntity);

            log.info("비밀번호를 수정했습니다.");

            res = 1;

        } catch (Exception e) {
            log.info("에러 : " + e);
            res = 0;
        }

        log.info(this.getClass().getName() + ".updatePassword End!");

        return res;
    }

    @Override
    @Transactional
    public int deleteUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".deleteUserInfo Start!");

        // 성공 : 1, 실패 : 0
        int res = 0;

        try {
            String userId = CmmUtil.nvl(pDTO.userId());
            int userSeq = pDTO.userSeq();

            log.info("userId : " + userId);
            log.info("userSeq : " + userSeq);

            // 회원 삭제
            userInfoRepository.deleteByUserIdAndUserSeq(userId, userSeq);

            // 회원 삭제값 확인
            Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

            kafkaTemplate.send(TOPIC, userId);

            if (rEntity.isEmpty()) {
                res = 1;
                log.info(userId + "님께서 회원탈퇴 하셨습니다.");
            } else {
                log.info("회원탈퇴가 되지 않았습니다.");
                log.info("userId : " + rEntity.get().getUserId());
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info(this.getClass().getName() + ".deleteUserInfo End!");

        return res;
    }
}
