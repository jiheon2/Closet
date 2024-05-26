package jiheon.closetservice.repository;

import jiheon.closetservice.repository.entity.ClosetEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosetRepository extends MongoRepository<ClosetEntity, String> {

    // 회원 사진 전체 조회
    List<ClosetEntity> findAllByUserIdOrderByPhotoSeqDesc(String userId) throws Exception;

    // 회원별 파츠 사진 List 조회
    List<ClosetEntity> findAllByUserIdAndPartsOrderByPhotoSeqDesc(String userId, String parts) throws Exception;

    // 등록한 사진 조회
    ClosetEntity findByPhotoSeq(long photoSeq);

    // 사진 갯수 세기
    long count();
}