package jiheon.userservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jiheon.userservice.auth.AuthInfo;
import jiheon.userservice.dto.UserInfoDTO;
import jiheon.userservice.repository.UserInfoRepository;
import jiheon.userservice.repository.entity.UserInfoEntity;
import jiheon.userservice.service.ISecurityService;
import jiheon.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SecurityService implements ISecurityService {

    private final UserInfoRepository userInfoRepository;

    private final PasswordEncoder bCryptPasswordEncoder;

    /**
     * Spring Security에서 로그인 처리를 하기 위해 실행하는 함수
     * Spring Security의 인증 기능을 사용하기 위해선 반드시 만들어야 하는 함수
     * Controller에서 호출되지않고, Spring Security가 바로 호출함
     * 아이디로 검색하고, 검색한 결과를 기반으로 Spring Security가 비밀번호가 같은지 판단함
     * 아이디와 패스워드가 일치하지 않으면 자동으로 UsernameNotFoundException 발생시킴
     *
     * @param userId 사용자 아이디
     * @return AuthInfo
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        log.info(this.getClass().getName() + ".loadUserByUserName Start!");

        log.info("userId : " + userId);

        // 로그인 요청한 사용자 아이디를 검색
        UserInfoEntity rEntity = userInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId + " Not Found User"));

        // rEntity 데이터를 DTO로 변환하기
        UserInfoDTO rDTO = new ObjectMapper().convertValue(rEntity, UserInfoDTO.class);

        log.info("rEntity 데이터 : " + rEntity.getUserId());
        log.info("rEntity 데이터 : " + rEntity.getPassword());

        // 비밀번호가 맞는지 체크 및 권한 부여를 위해 rDTO를 UserDetails를 구현한 AuthInfo에 넣어주기
        return new AuthInfo(rDTO);
    }

    @Override
    public int insertUserInfo(UserInfoDTO pDTO) {

        log.info(this.getClass().getName() + ".insertUserInfo Start!");

        int res = 0; // 성공 : 1, 중복으로 인한 가입 취소 : 2, 기타 에러 발생 : 0

        String userId = CmmUtil.nvl(pDTO.userId());
        String nickName = CmmUtil.nvl(pDTO.nickName());
        String password = CmmUtil.nvl(pDTO.password());
        String email = CmmUtil.nvl(pDTO.email());
        String age = CmmUtil.nvl(pDTO.age());
        String gender = CmmUtil.nvl(pDTO.gender());
        String isKakao = CmmUtil.nvl(pDTO.isKakao());
        String roles = CmmUtil.nvl(pDTO.roles());

        log.info("userId : " + userId);
        log.info("nickName : " + nickName);
        log.info("password : " + password);
        log.info("email : " + email);
        log.info("age : " + age);
        log.info("gender : " + gender);
        log.info("isKakao : " + isKakao);
        log.info("roles : " + roles);

        // 회원가입 중복 방지를 위해 DB에서 데이터 조회
        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        // 값이 존재한다면
        if (rEntity.isPresent()) {
            res = 2;
        } else {
            // 회원가입 엔터티 생성
            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(userId)
                    .nickName(nickName)
                    .password(password)
                    .email(email) // 암호화 해야하나?
                    .age(age)
                    .gender(gender)
                    .isKakao(isKakao)
                    .roles(roles)
                    .build();

            // 회원가입 DB에 저장
            userInfoRepository.save(pEntity);

            // 회원가입 성공여부 확인 및 중복 방지 조회
            rEntity = userInfoRepository.findByUserId(userId);

            if (rEntity.isPresent()) {
                res = 1;
            }
        }

        log.info(this.getClass().getName() + ".insertUserInfo End!");

        return res;
    }

    @Override
    public boolean existsByUserId(String userId) throws Exception {

        log.info(this.getClass().getName() + ".아이디 중복체크");

        return userInfoRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsByNickName(String nickName) throws Exception {

        log.info(this.getClass().getName() + ".닉네임 중복체크");

        return userInfoRepository.existsByNickName(nickName);
    }

    @Override
    public boolean existsByEmail(String email) throws Exception {

        log.info(this.getClass().getName() + ".이메일 중복체크");

        return userInfoRepository.existsByEmail(email);
    }

    @Override
    public UserInfoDTO findId(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".아이디 찾기 시작");

        String nickName = CmmUtil.nvl(pDTO.nickName());
        String email = CmmUtil.nvl(pDTO.email());

        log.info("nickName : " + nickName);
        log.info("email : " + email);

        UserInfoDTO rDTO = null;

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndNickName(email, nickName);

        log.info("rEntity : " + rEntity);

        if (rEntity.isPresent()) {

            rDTO = new ObjectMapper().convertValue(rEntity.get(),
                    new TypeReference<UserInfoDTO>() {
                    });

        }

        log.info(this.getClass().getName() + ".아이디 찾기 종료");

        return rDTO;
    }

    @Override
    public int findPw(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".비밀번호 찾기 시작");

        // 성공 : 1, 실패 : 0
        int res = 0;

        try {

            String userId = CmmUtil.nvl(pDTO.userId());
            String email = CmmUtil.nvl(pDTO.email());

            log.info("userId : " + userId);
            log.info("email : " + email);

            Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserIdAndEmail(userId, email);
            log.info("rEntity id : " + rEntity.get().getUserId());

            if (rEntity.isPresent()) {

                String newPassword = "0000";

                userInfoRepository.save(UserInfoEntity.builder()
                        .userId(userId)
                        .age(rEntity.get().getAge())
                        .email(rEntity.get().getEmail())
                        .nickName(rEntity.get().getNickName())
                        .userSeq(rEntity.get().getUserSeq())
                        .roles(rEntity.get().getRoles())
                        .isKakao(rEntity.get().getIsKakao())
                        .gender(rEntity.get().getGender())
                        .password(bCryptPasswordEncoder.encode(newPassword))
                        .build());

                res = 1;
            }

        } catch (Exception e) {
            log.info(e.toString());
        }

        return res;
    }

}
