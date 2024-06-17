package jiheon.communityservice.service;

import jiheon.communityservice.dto.CommentDTO;
import jiheon.communityservice.dto.PostDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICommunityService {

    // 게시글 등록(이미지O)
    int insertPost(PostDTO pDTO, MultipartFile image) throws Exception;

    // 게시글 등록(이미지X)
    int insertPost(PostDTO pDTO) throws Exception;

    // 게시글 수정(이미지O)
    int updatePost(PostDTO pDTO, MultipartFile image) throws Exception;

    // 게시글 수정(이미지O)
    int updatePost(PostDTO pDTO) throws Exception;

    // 게시글 삭제
    int deletePost(PostDTO pDTO) throws Exception;

    // 게시글 상세 조회
    PostDTO getPostInfo(String postSeq) throws Exception;

    // 댓글 불러오기
    List<CommentDTO> getCommentList(String postSeq) throws Exception;

    // 댓글 등록
    int insertComment(CommentDTO pDTO) throws Exception;

    // 댓글 수정
    int updateComment(CommentDTO pDTO) throws Exception;

    // 댓글 삭제
    int deleteComment(String commentSeq) throws Exception;

    // 게시글 전체조회 (무한 스크롤)
    List<PostDTO> post(int page, int size) throws Exception;
    // 작성한 게시글 조회(무한 스크롤)
    List<PostDTO> myPost(int page, int size, String userId) throws Exception;
}
