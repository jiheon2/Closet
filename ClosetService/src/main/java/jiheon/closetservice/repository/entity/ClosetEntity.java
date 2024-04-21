package jiheon.closetservice.repository.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "closet")
@Builder
public class ClosetEntity {

    @Id
    private String _id; // MongoDB 오브젝트 ID

    @Field
    private String userId; // 회원 ID

    @Field
    private String parts; // 부위

    @Field
    private String photoUrl; // 이미지 URL

    @Field
    private long photoSeq; // 이미지 개수
}
