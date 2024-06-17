package jiheon.closetservice.service.impl;


import jiheon.closetservice.repository.ClosetRepository;
import jiheon.closetservice.repository.entity.ClosetEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final ClosetRepository closetRepository;
    private final MongoTemplate mongoTemplate;
    @KafkaListener(topics = "user-delete-topic", groupId = "closet-group")
    public void consume(String userId) throws Exception {
        log.info("[Service] Kafka Start");

        log.info("userId : " + userId);

        // 쿼리 생성
        Query query = new Query(Criteria.where("userId").is(userId));

        // 쿼리 조건 로그
        log.info("쿼리 조건: " + query.toString());

        try {
            // userId로 모든 도큐먼트 찾기
            List<ClosetEntity> closets = mongoTemplate.find(query, ClosetEntity.class, "closet");

            // 각 도큐먼트를 id로 삭제
            for (ClosetEntity closet : closets) {
                Query deleteQuery = new Query(Criteria.where("_id").is(closet.getId()));
                mongoTemplate.remove(deleteQuery, "closet");
                log.info("삭제된 도큐먼트 id: " + closet.getId());
            }

            // 삭제 후 데이터 확인
            boolean exists = mongoTemplate.exists(query, "closet");
            log.info("삭제 후 존재 여부: " + exists);
        } catch (Exception e) {
            log.error("MongoDB 삭제 중 오류 발생", e);
        }

        log.info("[Service] MongoDB End");
    }
}
