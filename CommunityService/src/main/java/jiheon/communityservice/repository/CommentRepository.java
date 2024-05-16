package jiheon.communityservice.repository;

import jiheon.communityservice.repository.entity.CommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<CommentEntity, String> {

    // 댓글 조회
    List<CommentEntity> findAllByPostSeq(long postSeq);

    // 작성된 댓글 확인하기
    CommentEntity findByCommentSeq(long commentSeq);

    // 댓글 갯수 세기
    long count();

    // POST SEQ를 기반으로 삭제
    void deleteAllByPostSeq(long postSeq);
}
