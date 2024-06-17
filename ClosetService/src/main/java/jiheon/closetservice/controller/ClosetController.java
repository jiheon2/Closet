package jiheon.closetservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jiheon.closetservice.controller.response.CommonResponse;
import jiheon.closetservice.dto.ClosetDTO;
import jiheon.closetservice.dto.KafkaDTO;
import jiheon.closetservice.dto.MsgDTO;
import jiheon.closetservice.dto.TokenDTO;
import jiheon.closetservice.service.IClosetService;
import jiheon.closetservice.service.ITokenService;
import jiheon.closetservice.service.impl.KafkaService;
import jiheon.closetservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;


@CrossOrigin(origins = {"http://localhost:11000", "http://localhost:12000"},
        allowedHeaders = {"Content-Type, Authorization"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
        allowCredentials = "true")
@Tag(name = "옷장 서비스", description = "옷장 서비스 API")
@Slf4j
@RequestMapping(value = "/closet/v1")
@RequiredArgsConstructor
@RestController
public class ClosetController {

    private final IClosetService closetService;
    private final ITokenService tokenService;
    private final KafkaService kafkaService;

    private final String HEADER_PREFIX = "Bearer "; // Bearer 토큰 사용을 위한 선언값
    // 이미지 파일만 업로드
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");

    @Operation(summary = "회원별 옷장 전체 조회 API", description = "회원별 옷장의 전체 이미지를 제공하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found")})
    @PostMapping(value = "/list")
    public ResponseEntity getAllClosetList(@CookieValue(value = "${jwt.token.access.name}") String token) throws Exception {

        log.info("[Controller] getAllClosetList Start!");

        TokenDTO tDTO = tokenService.getTokenInfo(HEADER_PREFIX + token);
        String userId = tDTO.userId();

        List<ClosetDTO> rList = closetService.getAllClosetList(userId);

        log.info("[Controller] getAllClosetList End!");

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList));
    }

    @Operation(summary = "회원별 파츠별 옷장 조회 API", description = "회원별 파츠별 옷장의 전체 이미지를 제공하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found")})
    @PostMapping(value = "/partsList")
    public ResponseEntity getAllPartsClosetList(@CookieValue(value = "${jwt.token.access.name}") String token, HttpServletRequest request) throws Exception {

        log.info("[Controller] getAllPartsClosetList Start!");

        TokenDTO tDTO = tokenService.getTokenInfo(HEADER_PREFIX + token);
        String userId = CmmUtil.nvl(tDTO.userId());
        String parts = CmmUtil.nvl(request.getParameter("parts"));

        log.info("userId : " + userId);
        log.info("parts : " + parts);

        List<ClosetDTO> rList = closetService.getAllPartsClosetList(userId, parts);

        log.info("[Controller] getAllPartsClosetList End!");

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList));
    }

    @Operation(summary = "옷장 사진 등록 API", description = "옷장 사진과 메타데이터를 GCP Storage 및 MongoDB에 등록하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad Request")})
    @PostMapping(value = "upload")
    public ResponseEntity upload(HttpServletRequest request, @CookieValue(value = "${jwt.token.access.name}") String token, @RequestParam(name = "photo") MultipartFile photo) {

        log.info("[Controller] upload Start!");

        String msg = "";
        int res = 0; // 성공 : 1, 기타 에러 발생 : 0
        MsgDTO dto = null;

        try {
            if (!ALLOWED_FILE_TYPES.contains(photo.getContentType())) {
                msg = "이미지 파일만 업로드 가능합니다.";
                dto = MsgDTO.builder().result(0).msg(msg).build();
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.series().name(), dto));
            }

            TokenDTO tDTO = tokenService.getTokenInfo(HEADER_PREFIX + token);
            String userId = CmmUtil.nvl(tDTO.userId());
            String parts = CmmUtil.nvl(request.getParameter("parts"));

            log.info("userId : " + userId);
            log.info("parts : " + parts);

            ClosetDTO pDTO = ClosetDTO.builder().userId(userId).parts(parts).build();

            res = closetService.uploadCloset(pDTO, photo);

            if (res == 1) {
                msg = "등록 성공";
            } else {
                msg = "등록 실패";
            }
        } catch (Exception e) {
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();

            log.info("[Controller] upload End!");
        }
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @Operation(summary = "옷장 사진 삭제 API", description = "회원별 등록된 사진을 삭제하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad Request")})
    @PostMapping(value = "/delete")
    public ResponseEntity deleteCloset(HttpServletRequest request, @CookieValue(value = "${jwt.token.access.name}") String token) {

        log.info("[Controller] delete Start!");

        String msg = "";
        int res = 0; // 성공 : 1, 기타 에러 발생 : 0
        MsgDTO dto = null;

        try {
            long photoSeq = Long.parseLong(request.getParameter("photoSeq"));

            res = closetService.deleteCloset(photoSeq);

            if (res == 1) {
                msg = "삭제 성공";
            } else {
                msg = "삭제 실패";
            }

        } catch (Exception e) {
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();
            log.info("[Controller] delete End!");
        }
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @DeleteMapping("/delete")
    public void deleteUser(String userId) throws Exception {
        kafkaService.consume(userId);
    }
}
