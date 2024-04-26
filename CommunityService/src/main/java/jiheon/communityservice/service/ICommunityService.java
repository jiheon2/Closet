package jiheon.communityservice.service;

import jiheon.communityservice.dto.CommentDTO;
import jiheon.communityservice.dto.PostDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICommunityService {

    // 게시글 등록
    int insertPost(PostDTO pDTO) throws Exception;

    // 게시글 수정
    int updatePost(PostDTO pDTO) throws Exception;

    // 게시글 삭제
    int deletePost(PostDTO pDTO) throws Exception;

    // 게시글 전체 조회
    List<PostDTO> getAllPostList() throws Exception;

    // 게시글 상세 조회
    PostDTO getPostInfo(String userId) throws Exception;

    // 게시글 이미지 업로드
    int uploadImage(MultipartFile image, String imageName) throws Exception;

    // 게시글 이미지 수정
    int updateImage(MultipartFile image, String imageName) throws Exception;

    // 댓글 등록
    int insertComment(CommentDTO pDTO) throws Exception;

    // 댓글 수정
    int updateComment(CommentDTO pDTO) throws Exception;

    // 댓글 삭제
    int deleteComment(CommentDTO pDTO) throws Exception;
}
