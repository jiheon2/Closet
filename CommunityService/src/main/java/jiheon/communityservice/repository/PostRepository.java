package jiheon.communityservice.repository;

import jiheon.communityservice.repository.entity.PostEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<PostEntity, String> {

    // 게시글 전체 조회
    List<PostEntity> findAllByOrderByPostSeqDesc();

    // 회원아이디별 게시글 조회
    List<PostEntity> findAllByUserIdOrderByPostSeqDesc(String userId);

    // 게시글 상세조회
    Optional<PostEntity> findByPostSeq(int postSeq);

    // 게시글 개수 세기
    long count();
}
