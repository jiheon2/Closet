package jiheon.communityservice.service.impl;

import jiheon.communityservice.dto.KafkaDTO;
import jiheon.communityservice.repository.CommentRepository;
import jiheon.communityservice.repository.PostRepository;
import jiheon.communityservice.repository.entity.CommentEntity;
import jiheon.communityservice.repository.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MongoTemplate mongoTemplate;

    @KafkaListener(topics = "user-delete-topic", groupId = "community-group")
    public void consume(String userId) {
        log.info("[Service] Kafka Start");

        log.info("userId : " + userId);

        // 도큐먼트 삭제
        long deletedPost = mongoTemplate.remove(new Query(Criteria.where("userId").is(userId)), "post").getDeletedCount();
        long deletedComment = mongoTemplate.remove(new Query(Criteria.where("userId").is(userId)), "comment").getDeletedCount();
        log.info("삭제된 게시글 수 : " + deletedPost);
        log.info("삭제된 댓글 수 : " + deletedComment);

        log.info("[Service] Kafka End");
    }
}
