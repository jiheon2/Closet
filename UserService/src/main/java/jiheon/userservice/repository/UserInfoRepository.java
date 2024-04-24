package jiheon.userservice.repository;

import jakarta.transaction.Transactional;
import jiheon.userservice.dto.UserInfoDTO;
import jiheon.userservice.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    // 회원 존재 여부 체크
    Optional<UserInfoEntity> findByUserId(String userId);

    // 아이디 중복체크
    boolean existsByUserId(String userId);

    // 닉네임 중복체크
    boolean existsByNickName(String nickName);

    // 이메일 중복체크
    boolean existsByEmail(String email);

    // 아이디 찾기
    Optional<UserInfoEntity> findByEmailAndName(String email, String name);

    // 비밀번호 찾기
    Optional<UserInfoEntity> findByUserIdAndEmail(String userId, String email);
}

