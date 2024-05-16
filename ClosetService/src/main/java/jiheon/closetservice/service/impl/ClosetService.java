package jiheon.closetservice.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.List;

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

    @Override
    public List<ClosetDTO> getClosetList(ClosetDTO pDTO) throws Exception {
        return null;
    }

    @Override
    public int uploadCloset(ClosetDTO pDTO, MultipartFile photo) {

        log.info(this.getClass().getName() + ".uploadCloset 실행");

        String ext = photo.getContentType(); // 파일의 형식 ex) JPG
        String userId = CmmUtil.nvl(pDTO.userId());
        String parts = CmmUtil.nvl(pDTO.parts());
        long photoSeq = closetRepository.countByUserIdAndParts(userId, parts);
        String photoName = userId + "/" + parts + "/" + photoSeq;

        log.info("userId : " + userId);
        log.info("parts : " + parts);
        log.info("photoSeq : " + photoSeq);
        log.info("photoName : " + photoName);

        int res = 0; // 성공 : 1, 기타 에러 발생 : 0

        try {
            Storage storage = StorageOptions
                    .newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(ServiceAccountCredentials.fromStream(
                            new FileInputStream("C://Closet/ClosetService/src/main/resources/closetproject-419105-297cdb4ae5b4.json")))
                    .build()
                    .getService();

            // GCS에 이미지 업로드
            BlobId blobId = BlobId.of(bucketName, photoName);

/*
            @Deprecated > create 쓰지말고 write써야하는데
            코드를 모르겠다 url이 안받아와진다

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(ext)
                    .build();

            WriteChannel writer = storage.writer(blobInfo);
            byte[] bytes = photo.getBytes();
            writer.write(ByteBuffer.wrap(bytes));
 */


            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder(bucketName, photoName)
                            .setContentType(ext)
                            .build(),
                    photo.getInputStream()
            );

            // DB에 메타데이터 저장
            String imageUrl = blobInfo.getMediaLink();

            ClosetEntity rEntity = ClosetEntity.builder()
                    .userId(CmmUtil.nvl(pDTO.userId()))
                    .parts(CmmUtil.nvl(pDTO.parts()))
                    .photoUrl(CmmUtil.nvl(imageUrl))
                    .photoSeq(photoSeq)
                    .build();

            mongoTemplate.save(rEntity);

            log.info("imageUrl : " + imageUrl);

            res = 1;

        } catch (Exception e) {
            log.info(e.toString());
            res = 0;
        }

        log.info(this.getClass().getName() + ".uploadCloset 종료");

        return res;
    }

    @Override
    public int deleteCloset(ClosetDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".deleteCloset 실행");

        String userId = CmmUtil.nvl(pDTO.userId());
        String parts = CmmUtil.nvl(pDTO.parts());
        long photoSeq = pDTO.photoSeq();
        String photoName = userId + "/" + parts + "/" + photoSeq;

        log.info("photoName : " + photoName);

        int res = 0; // 성공 : 1, Storage에 없음 : 2, 기타 에러 : 0

        try {
            // GCS 파일 삭제
            // 환경설정
            Storage storage = StorageOptions
                    .newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(ServiceAccountCredentials.fromStream(
                            new FileInputStream("C://Closet/ClosetService/src/main/resources/closetproject-419105-297cdb4ae5b4.json")))
                    .build()
                    .getService();

            // 객체 조회
            Blob blob = storage.get(bucketName, photoName);
            if (blob == null) {
                res = 2;
            }

            // 데이터 무결성을 위한 코드 / 세대번호(generation Number)가 일치하지 않으면 업로드 요청 실패
            Storage.BlobSourceOption precondition =
                    Storage.BlobSourceOption.generationMatch(blob.getGeneration());

            // 객체 삭제
            storage.delete(bucketName, photoName, precondition);

            // MongoDB 정보 삭제
            ClosetEntity rEntity = ClosetEntity.builder()
                    ._id(closetRepository.getIdByUserIdAndPartsAndPhotoSeq(userId, parts, photoSeq))
                    .userId(userId)
                    .parts(parts)
                    .photoSeq(photoSeq)
                    .build();

            closetRepository.delete(rEntity);

            res = 1;

        } catch (Exception e) {
            res = 0;
            log.info(e.toString());
        }

        log.info(this.getClass().getName() + ".deleteCloset 종료");

        return res;
    }
}
