package jiheon.communityservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jiheon.communityservice.dto.CommentDTO;
import jiheon.communityservice.dto.PostDTO;
import jiheon.communityservice.repository.CommentRepository;
import jiheon.communityservice.repository.PostRepository;
import jiheon.communityservice.repository.entity.CommentEntity;
import jiheon.communityservice.repository.entity.PostEntity;
import jiheon.communityservice.service.ICommunityService;
import jiheon.communityservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService implements ICommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MongoTemplate mongoTemplate;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    @Value("${spring.cloud.gcp.storage.credentials.location")
    private String fileKey;

    public long findMaxPostValue() {
        return Objects.requireNonNull(mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        Aggregation.group().max("postSeq").as("postSeq")
                ),
                "post",
                PostEntity.class
        ).getUniqueMappedResult()).getPostSeq();
    }

    public long findMaxCommentValue() {
        return Objects.requireNonNull(mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        Aggregation.group().max("commentSeq").as("commentSeq")
                ),
                "comment",
                CommentEntity.class
        ).getUniqueMappedResult()).getCommentSeq();
    }

    @Override
    public int insertPost(PostDTO pDTO) throws Exception {
        log.info("[Service] insertPost(image X) Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        try {
            // 컨트롤러에서 값 받아오기
            String userId = CmmUtil.nvl(pDTO.userId());
            String nickName = CmmUtil.nvl(pDTO.nickName());
            String title = CmmUtil.nvl(pDTO.title());
            String contents = CmmUtil.nvl(pDTO.contents());
            String regDt = CmmUtil.nvl(pDTO.regDt());
            long postSeq = findMaxPostValue() + 1;

            log.info("userId : " + userId);
            log.info("nickName : " + nickName);
            log.info("title : " + title);
            log.info("contents : " + contents);
            log.info("regDt : " + regDt);
            log.info("postSeq : " + postSeq);

            // 게시글 등록 엔터티 생성
            PostEntity pEntity = PostEntity.builder()
                    .userId(userId)
                    .postSeq(postSeq)
                    .nickName(nickName)
                    .title(title)
                    .contents(contents)
                    .regDt(regDt)
                    .imagePath("none")
                    .build();

            // 게시글 등록
            postRepository.save(pEntity);

            // 등록이 잘 되었는지 확인
            Optional<PostEntity> rEntity = postRepository.findByPostSeq(postSeq);

            log.info("등록한 게시글 정보");
            log.info("rEntity : " + rEntity);

            if (rEntity.isPresent()) {
                res = 1;
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info("[Service] insertPost(image X) End!");

        return res;
    }

    @Override
    public int insertPost(PostDTO pDTO, MultipartFile image) throws Exception {

        log.info(this.getClass().getName() + ".insertPost Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        try {
            // 컨트롤러에서 값 받아오기
            String userId = CmmUtil.nvl(pDTO.userId());
            String nickName = CmmUtil.nvl(pDTO.nickName());
            String title = CmmUtil.nvl(pDTO.title());
            String contents = CmmUtil.nvl(pDTO.contents());
            String regDt = CmmUtil.nvl(pDTO.regDt());
            long postSeq = findMaxPostValue() + 1;

            log.info("userId : " + userId);
            log.info("nickName : " + nickName);
            log.info("title : " + title);
            log.info("contents : " + contents);
            log.info("regDt : " + regDt);
            log.info("postSeq : " + postSeq);

            // 이미지 GCS 등록하기
            String ext = image.getContentType(); // 파일형식 가져오기
            String uuid = UUID.randomUUID().toString(); // GCS에 저장될 파일명 및 db에 저장될 경로
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

            // 이미지 링크 가져오기
            String imagePath = blobInfo.getMediaLink();
            log.info("imagePath : " + imagePath);

            // 게시글 등록 엔터티 생성
            PostEntity pEntity = PostEntity.builder()
                    .userId(userId)
                    .postSeq(postSeq)
                    .nickName(nickName)
                    .title(title)
                    .contents(contents)
                    .regDt(regDt)
                    .imagePath(imagePath)
                    .build();

            // 게시글 등록
            postRepository.save(pEntity);

            // 등록이 잘 되었는지 확인
            Optional<PostEntity> rEntity = postRepository.findByPostSeq(postSeq);

            log.info("등록한 게시글 정보");
            log.info("rEntity : " + rEntity);

            if (rEntity.isPresent()) {
                res = 1;
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info(this.getClass().getName() + ".insertPost End!");

        return res;
    }

    @Override
    public int updatePost(PostDTO pDTO, MultipartFile image) throws Exception {

        log.info("[Service] updatePost Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        try {
            // 컨트롤러에서 값 받아오기
            String userId = CmmUtil.nvl(pDTO.userId());
            String title = CmmUtil.nvl(pDTO.title());
            String contents = CmmUtil.nvl(pDTO.contents());
            long postSeq = pDTO.postSeq();

            log.info("userId : " + userId);
            log.info("title : " + title);
            log.info("contents : " + contents);
            log.info("postSeq : " + postSeq);

            // 기존에 존재하는 이미지 정보 가져오기
            Optional<PostEntity> postEntity = postRepository.findByPostSeq(postSeq);
            String existImagePath = null;
            if (postEntity.isPresent()) {
                PostEntity pEntity = postEntity.get();
                existImagePath = pEntity.getImagePath();
                log.info("기존 이미지 파일 경로 : " + existImagePath);

                // 이미지 업로드 여부 확인 및 업로드
                String imagePath = null;
                if (image != null && !image.isEmpty()) {

                    String ext = image.getContentType(); // 파일형식 가져오기
                    String uuid = UUID.randomUUID().toString(); // GCS에 저장될 파일명 및 db에 저장될 경로
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

                    // 이미지 링크 가져오기
                    imagePath = blobInfo.getMediaLink();
                    log.info("imagePath : " + imagePath);

                    // 기존 이미지 삭제
                    Storage.BlobSourceOption precondition = Storage.BlobSourceOption.generationMatch(blobInfo.getGeneration());
                    storage.delete(bucketName, existImagePath, precondition);
                }

                // 게시글 업데이트 엔터티 생성
                pEntity = PostEntity.builder()
                        .id(pEntity.getId())
                        .userId(userId)
                        .postSeq(postSeq)
                        .nickName(pEntity.getNickName())
                        .title(title)
                        .contents(contents)
                        .regDt(pEntity.getRegDt())
                        .imagePath(imagePath) // 변경된 이미지 경로 설정
                        .build();

                // 게시글 업데이트
                postRepository.save(pEntity);

                // 업데이트가 잘 되었는지 확인
                Optional<PostEntity> rEntity = postRepository.findByPostSeq(postSeq);

                log.info("업데이트된 게시글 정보");
                log.info("rEntity : " + rEntity);

                if (rEntity.isPresent()) {
                    res = 1;
                }
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info("[Service] updatePost End!");

        return res;
    }

    @Override
    public int updatePost(PostDTO pDTO) throws Exception {

        log.info("[Service] updatePost(image X) Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        try {
            // DB에서 조회
            long postSeq = pDTO.postSeq();
            Optional<PostEntity> postEntity = postRepository.findByPostSeq(postSeq);

            if (postEntity.isPresent()) {
                PostEntity pEntity = postEntity.get();

                // 컨트롤러에서 값 받아오기
                String userId = CmmUtil.nvl(pDTO.userId());
                String title = CmmUtil.nvl(pDTO.title());
                String contents = CmmUtil.nvl(pDTO.contents());


                log.info("userId : " + userId);
                log.info("title : " + title);
                log.info("contents : " + contents);
                log.info("postSeq : " + postSeq);

                // 게시글 등록 엔터티 생성
                pEntity = PostEntity.builder()
                        .id(pEntity.getId())
                        .userId(userId)
                        .postSeq(postSeq)
                        .nickName(pEntity.getNickName())
                        .title(title)
                        .contents(contents)
                        .regDt(pEntity.getRegDt())
                        .imagePath("none")
                        .build();

                // 게시글 등록
                postRepository.save(pEntity);

                // 등록이 잘 되었는지 확인
                Optional<PostEntity> rEntity = postRepository.findByPostSeq(postSeq);

                log.info("등록한 게시글 정보");
                log.info("rEntity : " + rEntity);

                if (rEntity.isPresent()) {
                    res = 1;
                }
            }

        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info("[Service] updatePost(image X) End!");

        return res;
    }

    @Override
    @Transactional
    public int deletePost(PostDTO pDTO) throws Exception {

        log.info("[Service] deletePost Start!");

        int res = 0; // 성공 1, 실패 0

        try {
            // 컨트롤러 값 받기
            long postSeq = pDTO.postSeq();
            log.info("postSeq : " + postSeq);

            // seq를 통해 삭제할 게시글 정보 담기
            Optional<PostEntity> pEntity = postRepository.findByPostSeq(postSeq);
            log.info("pEntity : " + pEntity);

            // 삭제
            if (pEntity.isPresent()) {
                postRepository.deleteById(pEntity.get().getId());
                commentRepository.deleteAllByPostSeq(postSeq);
            }

            // 삭제 확인
            Optional<PostEntity> rEntity = postRepository.findByPostSeq(postSeq);
            Optional<List<CommentEntity>> cEntity = Optional.ofNullable(commentRepository.findAllByPostSeq(postSeq));
            log.info("cEntity : " + cEntity);
            if (rEntity.isEmpty()) {
                res = 1;
                log.info("res : " + res);
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info("[Service] deletePost End!");

        return res;
    }

    @Override
    public List<PostDTO> getAllPostList() {

        log.info("[Service] getAllPostList Start!");

        // 게시글 전체조회
        List<PostEntity> rList = postRepository.findAllByOrderByPostSeqDesc();
        for (PostEntity post : rList) {
            log.info("PostEntity 정보 : {}", post.toString());
        }

        // 엔티티 값을 DTO에 맞게 변화
        List<PostDTO> nList = new ArrayList<>();
        for (PostEntity entity : rList) {
            PostDTO dto = new ObjectMapper().convertValue(entity, PostDTO.class);
            nList.add(dto);
        }
        for (PostDTO dto : nList) {
            log.info("PostDTO 정보 : {}", dto.toString());
        }

        int dataSize = nList.size();
        log.info("조회된 데이터의 개수 : " + dataSize);

        log.info("[Service] getAllPostList End!");

        return nList;
    }

    @Override
    public PostDTO getPostInfo(long postSeq) throws Exception {

        log.info("[Service] getPostInfo Start!");

        // DB에서 가져오기
        Optional<PostEntity> postEntity = postRepository.findByPostSeq(postSeq);
        log.info("DB에서 조회한 게시글 정보");
        log.info("조회한 게시글 정보");
        log.info("제목 : " + postEntity.get().getTitle());
        log.info("내용 : " + postEntity.get().getContents());
        log.info("닉네임 : " + postEntity.get().getNickName());
        log.info("회원아이디 : " + postEntity.get().getUserId());
        log.info("등록일 : " + postEntity.get().getRegDt());
        log.info("이미지 경로 : " + postEntity.get().getImagePath());

        // Entity를 DTO로 변환
        PostDTO postDTO = new ObjectMapper().convertValue(postEntity.get(),
                new TypeReference<PostDTO>() {
                });

        log.info("[Service] getPostInfo End!");

        return postDTO;
    }

    @Override
    public List<CommentDTO> getCommentList(long postSeq) throws Exception {

        log.info("[Service] getComment Start!");

        List<CommentEntity> commentList = commentRepository.findAllByPostSeq(postSeq);
        for (CommentEntity comment : commentList) {
            log.info("CommentEntity 정보 : {}", comment.toString());
        }

        // 엔티티 값을 DTO에 맞게 변화
        List<CommentDTO> cList = new ArrayList<>();
        for (CommentEntity entity : commentList) {
            CommentDTO dto = new ObjectMapper().convertValue(entity, CommentDTO.class);
            cList.add(dto);
        }
        for (CommentDTO dto : cList) {
            log.info("CommentDTO 정보 : {}", dto.toString());
        }

        int dataSize = cList.size();
        log.info("조회된 데이터의 개수 : " + dataSize);

        log.info("[Service] getComment End!");

        return cList;
    }

    @Override
    public int insertComment(CommentDTO pDTO) throws Exception {

        log.info("[Service] insertComment Start!");

        int res = 0; // 성공 1, 실패 0

        try {
            String userId = CmmUtil.nvl(pDTO.userId());
            String comment = CmmUtil.nvl(pDTO.comment());
            String nickName = CmmUtil.nvl(pDTO.nickName());
            long postSeq = pDTO.postSeq();
            long commentSeq = findMaxCommentValue() + 1;

            // 댓글 등록 엔터티 생성
            CommentEntity cEntity = CommentEntity.builder()
                    .userId(userId)
                    .postSeq(postSeq)
                    .comment(comment)
                    .nickName(nickName)
                    .commentSeq(commentSeq)
                    .build();

            // 저장
            commentRepository.save(cEntity);
            log.info("작성한 댓글");
            log.info("cEntity : " + cEntity);

            // 댓글 확인하기
            Optional<CommentEntity> rEntity = Optional.ofNullable(commentRepository.findByCommentSeq(commentSeq));
            log.info("작성된 댓글 정보");
            log.info("rEntity : " + rEntity);

            if (rEntity.isPresent()) {
                res = 1;
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info("[Service] insertComment End!");

        return res;
    }

    @Override
    public int updateComment(CommentDTO pDTO) throws Exception {

        log.info("[Service] updateComment Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        try {
            // 컨트롤러에서 받아온 값
            String comment = pDTO.comment();
            long commentSeq = pDTO.commentSeq();

            log.info("수정 내용");
            log.info("comment : " + comment);
            log.info("commentSeq : " + commentSeq);

            // 수정을 위한 엔터티 조회 및 생성
            Optional<CommentEntity> pEntity = Optional.ofNullable(commentRepository.findByCommentSeq(commentSeq));

            if (pEntity.isPresent()) {

                CommentEntity cEntity = pEntity.get();

                CommentEntity commentEntity = CommentEntity.builder()
                        .id(cEntity.getId())
                        .commentSeq(commentSeq)
                        .postSeq(cEntity.getPostSeq())
                        .userId(cEntity.getUserId())
                        .nickName(cEntity.getNickName())
                        .comment(comment)
                        .build();

                // 업데이트
                commentRepository.save(commentEntity);

                // 댓글 수정 확인
                Optional<CommentEntity> rEntity = Optional.ofNullable(commentRepository.findByCommentSeq(commentSeq));
                log.info("수정된 댓글 정보");
                log.info("rEntity : " + rEntity);

                if (rEntity.isPresent()) {
                    res = 1;
                }
            }
        } catch (Exception e) {
            log.info(e.toString());
        }
        log.info("[Service] updateComment End!");

        return res;
    }

    @Override
    public int deleteComment(long commentSeq) throws Exception {

        log.info("[Service] deleteComment Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        try {
            log.info("commentSeq : " + commentSeq);

            // 댓글 정보
            CommentEntity cEntity = commentRepository.findByCommentSeq(commentSeq);
            log.info("cEntity : " + cEntity);

            // 댓글 삭제
            commentRepository.deleteById(cEntity.getId());

            Optional<CommentEntity> rEntity = Optional.ofNullable(commentRepository.findByCommentSeq(commentSeq));

            if (rEntity.isEmpty()) {
                res = 1;
            }
        } catch (Exception e) {
            log.info(e.toString());
        }

        log.info("[Service] deleteComment End!");

        return res;
    }
}
