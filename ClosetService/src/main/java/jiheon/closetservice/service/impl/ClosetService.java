package jiheon.closetservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import jiheon.closetservice.dto.ClosetDTO;
import jiheon.closetservice.repository.ClosetRepository;
import jiheon.closetservice.repository.entity.ClosetEntity;
import jiheon.closetservice.service.IClosetService;
import jiheon.closetservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClosetService implements IClosetService {

    private final ClosetRepository closetRepository;
    private final MongoTemplate mongoTemplate;


    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String fileKey;

    public long findMaxValue() {
        ClosetEntity maxPhotoSeq = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        Aggregation.group().max("photoSeq").as("photoSeq")
                ),
                "closet",
                ClosetEntity.class
        ).getUniqueMappedResult();

        if (maxPhotoSeq == null) {
            return 0; // 데이터가 없을 때 반환할 기본값
        } else {
            return maxPhotoSeq.getPhotoSeq();
        }
    }


    @Override
    public List<ClosetDTO> getAllClosetList(String userId) throws Exception {

        log.info("[Service] getAllClosetList Start!");

        log.info("userId : " + userId);

        List<ClosetEntity> rList = closetRepository.findAllByUserIdOrderByPhotoSeqDesc(userId);
        log.info("조회된 ENTITY LIST 개수 : " + rList.size() + "개");

        List<ClosetDTO> cList = new ArrayList<>();
        for (ClosetEntity entity : rList) {
            ClosetDTO dto = new ObjectMapper().convertValue(entity, ClosetDTO.class);
            cList.add(dto);
        }
        log.info("조회된 DTO LIST 개수 : " + cList.size() + "개");

        log.info("[Service] getAllClosetList End!");

        return cList;
    }

    @Override
    public List<ClosetDTO> getAllPartsClosetList(String userId, String parts) throws Exception {

        log.info("[Service] getAllPartsClosetList Start!");

        log.info("userId : " + userId);

        List<ClosetEntity> rList = closetRepository.findAllByUserIdAndPartsOrderByPhotoSeqDesc(userId, parts);
        log.info("조회된 ENTITY LIST 개수 : " + rList.size() + "개");

        List<ClosetDTO> cList = new ArrayList<>();
        for (ClosetEntity entity : rList) {
            ClosetDTO dto = new ObjectMapper().convertValue(entity, ClosetDTO.class);
            cList.add(dto);
        }
        log.info("조회된 DTO LIST 개수 : " + cList.size() + "개");

        log.info("[Service] getAllPartsClosetList End!");

        return cList;
    }

    @Override
    @Transactional
    public int uploadCloset(ClosetDTO pDTO, MultipartFile image) throws Exception {

        log.info("[Service] uploadCloset Start!");

        int res = 0;

        try {
            String userId = CmmUtil.nvl(pDTO.userId());
            String parts = CmmUtil.nvl(pDTO.parts());
            long photoSeq = findMaxValue() + 1;

            log.info("등록할 정보");
            log.info("userId : " + userId);
            log.info("parts : " + parts);
            log.info("photoSeq : " + photoSeq);

            // 이미지 GCS 등록하기
            String ext = image.getContentType(); // 파일 형식 가져오기
            String uuid = UUID.randomUUID().toString();
            InputStream keyFile = ResourceUtils.getURL(fileKey).openStream();

            Storage storage = StorageOptions
                    .newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(keyFile))
                    .build()
                    .getService();

            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder(bucketName, uuid)
                            .setContentType(ext)
                            .build(),
                    image.getInputStream()
            );

            // 이미지 링크
            String photoUrl = blobInfo.getMediaLink();
            log.info("photoUrl : " + photoUrl);

            // 엔터티 생성
            ClosetEntity pEntity = ClosetEntity.builder()
                    .userId(userId)
                    .photoSeq(photoSeq)
                    .parts(parts)
                    .photoUrl(photoUrl)
                    .build();

            // 이미지 등록
            closetRepository.save(pEntity);

            // 등록 확인
            Optional<ClosetEntity> rEntity = Optional.ofNullable(closetRepository.findByPhotoSeq(photoSeq));

            if (rEntity.isPresent()) {
                res = 1;
            }
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
        }

        log.info("[Service] uploadPhoto End!");

        return res;
    }

    @Override
    @Transactional
    public int deleteCloset(long photoSeq) throws Exception {

        log.info("[Service] deleteCloset Start!");

        int res = 0;

        try {
            log.info("photoSeq : " + photoSeq);

            Optional<ClosetEntity> pEntity = Optional.ofNullable(closetRepository.findByPhotoSeq(photoSeq));

            // 삭제
            if (pEntity.isPresent()) {
                closetRepository.deleteById(pEntity.get().getId());
            }

            // 삭제 확인
            Optional<ClosetEntity> rEntity = Optional.ofNullable(closetRepository.findByPhotoSeq(photoSeq));

            if (rEntity.isEmpty()) {
                res = 1;
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info("[Service] deleteCloset End!");

        return res;
    }
}
