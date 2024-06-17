package jiheon.communityservice.repository;

import jiheon.communityservice.repository.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<PostEntity, String> {

    // 게시글 상세조회
    Optional<PostEntity> findByPostSeq(String postSeq);

    // 자신이 작성한 게시글 조회
    Page<PostEntity> findByUserId(Pageable pageable, String userId);

    // 게시글 개수 세기
    long count();

    void deleteAllByUserId(String userId);
}
