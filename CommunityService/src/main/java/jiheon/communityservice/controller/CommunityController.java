package jiheon.communityservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jiheon.communityservice.controller.response.CommonResponse;
import jiheon.communityservice.dto.*;
import jiheon.communityservice.service.ICommunityService;
import jiheon.communityservice.service.ITokenService;
import jiheon.communityservice.service.impl.KafkaService;
import jiheon.communityservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:11000", "http://localhost:12000"},
        allowCredentials = "true",
        allowedHeaders = {"Content-Type, Authorization"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@Tag(name = "커뮤니티 서비스", description = "커뮤니티 서비스를 위한 API")
@Slf4j
@RequestMapping(value = "/community/v1")
@RequiredArgsConstructor
@RestController
public class CommunityController {

    private final ITokenService tokenService;
    private final ICommunityService communityService;
    private final KafkaService kafkaService;
    // Bearer 토큰 사용을 위한 선언값
    private final String HEADER_PREFIX = "Bearer ";
    // 이미지 파일만 업로드
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");


    @Operation(summary = "게시글 리스트 조회 API", description = "게시글 정보를 MongoDB에서 전체 조회하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "post")
    public ResponseEntity<CommonResponse> post(HttpServletRequest request) throws Exception {
        int page = Integer.parseInt(request.getParameter("page"));
        int size = Integer.parseInt(request.getParameter("size"));
        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), communityService.post(page, size)));
    }

    @PostMapping(value = "myPost")
    public ResponseEntity<CommonResponse> myPost(HttpServletRequest request) throws Exception {
        int page = Integer.parseInt(request.getParameter("page"));
        int size = Integer.parseInt(request.getParameter("size"));
        String userId = CmmUtil.nvl(request.getParameter("userId"));
        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), communityService.myPost(page, size, userId)));
    }

    @Operation(summary = "게시글 상세조회 API", description = "게시글 단건 정보를 MongoDB에서 조회하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "postInfo")
    public ResponseEntity<CommonResponse> postInfo(@RequestParam String postSeq) throws Exception {

        log.info("[Controller] postInfo Start!");
        log.info("게시글 번호 : " + postSeq);

        PostDTO pDTO = communityService.getPostInfo(postSeq);
        log.info("조회한 게시글 정보");
        log.info("제목 : " + pDTO.title());
        log.info("내용 : " + pDTO.contents());
        log.info("닉네임 : " + pDTO.nickName());
        log.info("회원아이디 : " + pDTO.userId());
        log.info("등록일 : " + pDTO.regDt());
        log.info("이미지 경로 : " + pDTO.imagePath());

        List<CommentDTO> commentList = Optional.ofNullable(communityService.getCommentList(postSeq)).orElseGet(ArrayList::new);
        log.info("댓글 리스트 값 존재 여부 : " + commentList);

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), pDTO, commentList));
    }


    @Operation(summary = "게시글 등록 API", description = "게시글을 MongoDB에 등록하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "insertPost")
    public ResponseEntity<CommonResponse> insertPost(HttpServletRequest request, @RequestParam(name = "image", required = false) MultipartFile image,
                                                     @CookieValue(value = "${jwt.token.access.name}") String token) {

        log.info(this.getClass().getName() + ".insertPost Start!");

        String msg = "";
        int res = 0;
        MsgDTO dto;

        try {
            // UserService로부터 회원아이디 가져오기
            TokenDTO tDTO = tokenService.getTokenInfo(HEADER_PREFIX + token);
            log.info("TokenDTO : " + tDTO);

            // JWT AccessToken으로부터 회원아이디 가져오기
            String userId = CmmUtil.nvl(tDTO.userId());
            log.info("userId : " + userId);

            String title = CmmUtil.nvl(request.getParameter("title"));
            String contents = CmmUtil.nvl(request.getParameter("contents"));
            String nickName = CmmUtil.nvl(request.getParameter("nickName"));
            String regDt = CmmUtil.nvl(request.getParameter("regDt"));

            PostDTO pDTO = PostDTO.builder()
                    .title(title)
                    .nickName(nickName)
                    .regDt(regDt)
                    .contents(contents)
                    .userId(userId)
                    .build();

            // 게시글 저장 로직
            if (image != null && !image.isEmpty()) {
                if (!ALLOWED_FILE_TYPES.contains(image.getContentType())) {
                    msg = "이미지 파일만 업로드 가능합니다.";
                    dto = MsgDTO.builder().result(0).msg(msg).build();
                    return ResponseEntity.ok(CommonResponse.of(HttpStatus.BAD_REQUEST,
                            HttpStatus.BAD_REQUEST.series().name(), dto));
                }
                communityService.insertPost(pDTO, image);
            } else {
                communityService.insertPost(pDTO);
            }
            // 사용자에게 보여줄 메세지
            msg = "게시글이 등록되었습니다.";
            res = 1;

        } catch (Exception e) {
            msg = "게시글 등록에 실패하였습니다.";
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().msg(msg).result(res).build();

            log.info(this.getClass().getName() + ".insertPost End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }


    @Operation(summary = "게시글 수정 API", description = "MongoDB에 저장된 게시글을 수정하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "updatePost")
    public ResponseEntity<CommonResponse> updatePost(HttpServletRequest request, @RequestParam(name = "image", required = false) MultipartFile image,
                                                     @CookieValue(value = "${jwt.token.access.name}") String token) {

        log.info("[Controller] updatePost Start!");

        String msg = "";
        int res = 0;
        MsgDTO dto;

        try {
            // UserService로부터 회원아이디 가져오기
            TokenDTO tDTO = tokenService.getTokenInfo(HEADER_PREFIX + token);
            log.info("TokenDTO : " + tDTO);

            // JWT AccessToken으로부터 회원아이디 가져오기
            String userId = CmmUtil.nvl(tDTO.userId());
            log.info("userId : " + userId);

            String title = CmmUtil.nvl(request.getParameter("title"));
            String contents = CmmUtil.nvl(request.getParameter("contents"));
            String postSeq = CmmUtil.nvl(request.getParameter("postSeq"));

            PostDTO pDTO = PostDTO.builder()
                    .title(title)
                    .postSeq(postSeq)
                    .contents(contents)
                    .userId(userId)
                    .build();

            // 게시글 수정 로직
            if (image != null && !image.isEmpty()) {
                if (!ALLOWED_FILE_TYPES.contains(image.getContentType())) {
                    msg = "이미지 파일만 업로드 가능합니다.";
                    dto = MsgDTO.builder().result(0).msg(msg).build();
                    return ResponseEntity.ok(CommonResponse.of(HttpStatus.BAD_REQUEST,
                            HttpStatus.BAD_REQUEST.series().name(), dto));
                }
                communityService.updatePost(pDTO, image);
            } else {
                communityService.updatePost(pDTO);
            }
            // 사용자에게 보여줄 메세지
            msg = "게시글이 수정되었습니다.";
            res = 1;

        } catch (Exception e) {
            msg = "게시글 수정에 실패하였습니다.";
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().msg(msg).result(res).build();

            log.info("[Controller] updatePost End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @Operation(summary = "게시글 수정 API", description = "MongoDB에 저장된 게시글을 수정하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found!"),
            }
    )
    @PostMapping(value = "deletePost")
    public ResponseEntity<CommonResponse> deletePost(HttpServletRequest request,
                                                     @CookieValue(value = "${jwt.token.access.name}") String token) {

        log.info("[Controller] deletePost Start!");

        String msg = "";
        int res = 0;
        MsgDTO dto;

        try {
            String postSeq = CmmUtil.nvl(request.getParameter("postSeq"));
            log.info("postSeq : " + postSeq);

            PostDTO pDTO = PostDTO.builder()
                    .postSeq(postSeq)
                    .build();

            res = communityService.deletePost(pDTO);

            if (res == 1) {
                // 사용자에게 보여줄 메세지
                msg = "게시글이 삭제되었습니다.";
            } else {
                msg = "오류로 인해 삭제가 되지 않았습니다.";
            }

        } catch (Exception e) {
            msg = "게시글 삭제에 실패하였습니다.";
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().msg(msg).result(res).build();

            log.info("[Controller] deletePost End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @PostMapping(value = "insertComment")
    public ResponseEntity<CommonResponse> insertComment(HttpServletRequest request,
                                                        @CookieValue(value = "${jwt.token.access.name}") String token) throws Exception {

        log.info("[Controller] insertComment Start!");

        String msg = "";
        int res = 0;
        MsgDTO dto;

        try {
            // UserService로부터 회원아이디 가져오기
            TokenDTO tDTO = tokenService.getTokenInfo(HEADER_PREFIX + token);
            log.info("TokenDTO : " + tDTO);

            // JWT AccessToken으로부터 회원아이디 가져오기
            String userId = CmmUtil.nvl(tDTO.userId());
            log.info("userId : " + userId);

            String comment = CmmUtil.nvl(request.getParameter("comment"));
            String nickName = CmmUtil.nvl(request.getParameter("nickName"));
            String postSeq = CmmUtil.nvl(request.getParameter("postSeq"));

            log.info("comment : " + comment);
            log.info("nickName : " + nickName);
            log.info("postSeq : " + postSeq);

            CommentDTO pDTO = CommentDTO.builder()
                    .comment(comment)
                    .nickName(nickName)
                    .postSeq(postSeq)
                    .userId(userId)
                    .build();

            res = communityService.insertComment(pDTO);

            msg = "댓글이 등록되었습니다.";

        } catch (Exception e) {
            log.info(e.toString());
            msg = "댓글 등록에 실패하였습니다.";
        } finally {
            dto = MsgDTO.builder().msg(msg).result(res).build();

            log.info("[Controller] insertComment End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @PostMapping(value = "updateComment")
    public ResponseEntity<CommonResponse> updateComment(HttpServletRequest request) throws Exception {

        log.info("[Controller] updateComment Start!");

        String msg = "";
        int res = 0;
        MsgDTO dto;

        try {
            String comment = CmmUtil.nvl(request.getParameter("comment"));
            String commentSeq = CmmUtil.nvl(request.getParameter("commentSeq"));

            CommentDTO pDTO = CommentDTO.builder()
                    .commentSeq(commentSeq)
                    .comment(comment)
                    .build();

            res = communityService.updateComment(pDTO);
            msg = "댓글 수정에 완료하였습니다.";

        } catch (Exception e) {
            log.info(e.toString());
            msg = "댓글 수정에 실패하였습니다.";
        } finally {
            dto = MsgDTO.builder().msg(msg).result(res).build();

            log.info("[Controller] updateComment End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @PostMapping(value = "deleteComment")
    public ResponseEntity<CommonResponse> deleteComment(HttpServletRequest request) throws Exception {

        log.info("[Controller] deleteComment Start!");

        String msg = "";
        int res = 0;
        MsgDTO dto;

        try {
            String commentSeq = CmmUtil.nvl(request.getParameter("commentSeq"));

            res = communityService.deleteComment(commentSeq);
            msg = "댓글 삭제에 완료하였습니다.";

        } catch (Exception e) {
            log.info(e.toString());
            msg = "댓글 삭제에 실패하였습니다.";
        } finally {
            dto = MsgDTO.builder().msg(msg).result(res).build();

            log.info("[Controller] deleteComment End!");
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @DeleteMapping("/delete")
    public void deleteUser(String userId) throws Exception {
        kafkaService.consume(userId);
    }
}
