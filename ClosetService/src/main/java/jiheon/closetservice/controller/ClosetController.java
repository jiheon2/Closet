package jiheon.closetservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jiheon.closetservice.dto.ClosetDTO;
import jiheon.closetservice.dto.MsgDTO;
import jiheon.closetservice.service.IClosetService;
import jiheon.closetservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:13000", "http://localhost:14000"},
        allowedHeaders = {"POST", "GET"},
        allowCredentials = "true")
@Tag(name = "옷장 서비스", description = "옷장 서비스 API")
@Slf4j
@RequestMapping(value = "/closet")
@RequiredArgsConstructor
@RestController
public class ClosetController {

    private final IClosetService closetService;

    @Operation(summary = "회원별 옷장 전체 조회 API", description = "회원별 옷장의 전체 이미지를 제공하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found")})
    @PostMapping(value = "/list")
    public List<ClosetDTO> getAllClosetList(HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".getAllClosetList 실행");

        String userId = CmmUtil.nvl(session.getId());

        log.info("userId : " + userId);

        ClosetDTO pDTO = ClosetDTO.builder().userId(userId).build();

        List<ClosetDTO> rList = Optional.ofNullable(closetService.getClosetList(pDTO)).orElseGet(ArrayList::new);

        log.info(this.getClass().getName() + ".getAllClosetList 종료");

        return rList;
    }

    @Operation(summary = "회원별 파츠별 옷장 조회 API", description = "회원별 파츠별 옷장의 전체 이미지를 제공하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Page Not Found")})
    @PostMapping(value = "/partsList")
    public List<ClosetDTO> getPartsClosetList(HttpSession session, HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".getPartsClosetList 실행");

        String userId = CmmUtil.nvl(session.getId());
        String parts = CmmUtil.nvl(request.getParameter("parts"));

        log.info("userId : " + userId);
        log.info("parts : " + parts);

        ClosetDTO pDTO = ClosetDTO.builder().userId(userId).parts(parts).build();

        List<ClosetDTO> rList = Optional.ofNullable(closetService.getClosetList(pDTO)).orElseGet(ArrayList::new);

        log.info(this.getClass().getName() + ".getAllClosetList 종료");

        return rList;
    }

    @Operation(summary = "옷장 사진 등록 API", description = "옷장 사진과 메타데이터를 GCP Storage 및 MongoDB에 등록하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad Request")})
    @PostMapping(value = "upload")
    public MsgDTO upload(HttpServletRequest request, HttpSession session
            , @RequestParam("photo") MultipartFile photo) {

        log.info(this.getClass().getName() + ".upload 실행");

        String msg = "";
        int res = 0; // 성공 : 1, 기타 에러 발생 : 0
        MsgDTO dto = null;

        try {
            String userId = CmmUtil.nvl(session.getId());
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

            log.info(this.getClass().getName() + ".upload 종료");
        }
        return dto;
    }

    @Operation(summary = "옷장 사진 삭제 API", description = "회원별 등록된 사진을 삭제하는 API",
            responses = {@ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad Request")})
    @PostMapping(value = "/delete")
    public MsgDTO deleteCloset(HttpServletRequest request, HttpSession session) {

        String msg = "";
        int res = 0; // 성공 : 1, 기타 에러 발생 : 0
        MsgDTO dto = null;

        try {
            log.info(this.getClass().getName() + ".delete 실행");

            String photoName = CmmUtil.nvl(request.getParameter("photoName"));

            String[] parsing = photoName.split("/");

            String userId = parsing[0];
            String parts = parsing[1];
            int photoSeq = Integer.parseInt(parsing[2]);

            log.info("userId : " + userId);
            log.info("parts : " + parts);
            log.info("photoSeq : " + photoSeq);

            ClosetDTO pDTO = ClosetDTO.builder().userId(userId).parts(parts).photoSeq(photoSeq).build();

            res = closetService.deleteCloset(pDTO);

            if (res == 1) {
                msg = "삭제 성공";
            } else {
                msg = "삭제 실패";
            }

        } catch (Exception e) {
            log.info(e.toString());
        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();
            log.info(this.getClass().getName() + ".delete 종료");
        }

        return dto;
    }
}