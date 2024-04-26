package jiheon.communityservice.service.impl;

import jiheon.communityservice.dto.CommentDTO;
import jiheon.communityservice.dto.PostDTO;
import jiheon.communityservice.repository.CommentRepository;
import jiheon.communityservice.repository.PostRepository;
import jiheon.communityservice.repository.entity.PostEntity;
import jiheon.communityservice.service.ICommunityService;
import jiheon.communityservice.util.CmmUtil;
import jiheon.communityservice.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService implements ICommunityService {

    private final PostRepository postRepository;

    private final CommentRepository commentRepository;

    @Override
    public int insertPost(PostDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".insertPost Start!");

        int res = 0; // 성공 : 1, 실패 : 0

        try {

            // 컨트롤러에서 값 받아오기
            String userId = CmmUtil.nvl(pDTO.userId());
            String nickName = CmmUtil.nvl(pDTO.nickName());
            String title = CmmUtil.nvl(pDTO.title());
            String contents = CmmUtil.nvl(pDTO.contents());
            String regDt = DateUtil.getDateTime("yyyyMMdd");
            String chgDt = DateUtil.getDateTime("yyyyMMdd");
            int postSeq = pDTO.postSeq();

            log.info("userId : " + userId);
            log.info("nickName : " + nickName);
            log.info("title : " + title);
            log.info("contents : " + contents);
            log.info("regDt : " + regDt);
            log.info("chgDt : " + chgDt);
            log.info("postSeq : " + postSeq);

            // 게시글 등록 엔터티 생성
            PostEntity pEntity = PostEntity.builder()
                    .userId(userId)
                    .postSeq(postSeq)
                    .nickName(nickName)
                    .title(title)
                    .contents(contents)
                    .regDt(regDt)
                    .chgDt(chgDt)
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
    public int updatePost(PostDTO pDTO) throws Exception {
        return 0;
    }

    @Override
    public int deletePost(PostDTO pDTO) throws Exception {
        return 0;
    }

    @Override
    public List<PostDTO> getAllPostList() throws Exception {
        return null;
    }

    @Override
    public PostDTO getPostInfo(String userId) throws Exception {
        return null;
    }

    @Override
    public int insertComment(CommentDTO pDTO) throws Exception {
        return 0;
    }

    @Override
    public int updateComment(CommentDTO pDTO) throws Exception {
        return 0;
    }

    @Override
    public int deleteComment(CommentDTO pDTO) throws Exception {
        return 0;
    }

    @Override
    public int uploadImage(MultipartFile image, String imageName) throws Exception {
        return 0;
    }

    @Override
    public int updateImage(MultipartFile image, String imageName) throws Exception {
        return 0;
    }
}
