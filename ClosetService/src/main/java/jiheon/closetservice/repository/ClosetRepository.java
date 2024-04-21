package jiheon.closetservice.repository;

import jiheon.closetservice.repository.entity.ClosetEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosetRepository extends MongoRepository<ClosetEntity, String> {

    // 회원별 옷 사진 전체 조회
    List<ClosetEntity> findAllByUserId(String userId);

    // 회원 and 파츠 별 사진 조회
    List<ClosetEntity> findAllByPartsAndUserId(String parts, String userId);

    // 회원 및 파츠별 사진 개수 조회
    long countByUserIdAndParts(String userId, String parts);

    String getIdByUserIdAndPartsAndPhotoSeq(String userId, String parts, long photoSeq);
}