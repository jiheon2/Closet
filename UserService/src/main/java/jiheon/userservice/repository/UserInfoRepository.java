package jiheon.userservice.repository;

import jiheon.userservice.repository.entity.UserInfoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends MongoRepository<UserInfoEntity, String> {

    // 회원 존재 여부 체크
    Optional<UserInfoEntity> findByUserId(String userId);

    // userSeq 자동 증가 코드
    long count();
}
