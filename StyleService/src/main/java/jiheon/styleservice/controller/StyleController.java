package jiheon.styleservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jiheon.styleservice.controller.response.CommonResponse;
import jiheon.styleservice.dto.StyleDTO;
import jiheon.styleservice.service.impl.StyleService;
import jiheon.styleservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:11000", "http://localhost:12000", "http://localhost:18000"},
        allowCredentials = "true",
        allowedHeaders = {"Content-Type, Authorization"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@RestController
@RequestMapping(value = "/style/v1")
@Slf4j
@RequiredArgsConstructor
public class StyleController {

    private final StyleService styleService;

    @GetMapping(value = "/saveVector")
    public void saveVector() throws Exception {

        log.info("[Controller] saveVector Start");

        String folderPath = "C:\\Closet\\StyleService\\src\\main\\resources\\json\\힙합";

        styleService.saveVector(folderPath);

        log.info("[Controller] saveVector End");
    }

    @PostMapping(value = "/answerSheet")
    public ResponseEntity<CommonResponse> answerSheet(HttpServletRequest request) throws Exception {

        log.info("[Controller] answerSheet Start");

        String mainStyle = CmmUtil.nvl(request.getParameter("mainStyle"));
        String subStyle = CmmUtil.nvl(request.getParameter("subStyle"));
        String mainCategory = CmmUtil.nvl(request.getParameter("mainCategory"));
        String subCategory = CmmUtil.nvl(request.getParameter("subCategory"));
        String age = CmmUtil.nvl(request.getParameter("age"));
        String size = CmmUtil.nvl(request.getParameter("size"));
        String color = CmmUtil.nvl(request.getParameter("color"));
        String gender = CmmUtil.nvl(request.getParameter("gender"));

        log.info("mainStyle : " + mainStyle);
        log.info("subStyle : " + subStyle);
        log.info("mainCategory : " + mainCategory);
        log.info("subCategory : " + subCategory);
        log.info("age : " + age);
        log.info("size : " + size);
        log.info("color : " + color);
        log.info("gender : " + gender);

        StyleDTO pDTO = StyleDTO.builder()
                .mainStyle(mainStyle)
                .subStyle(subStyle)
                .mainCategory(mainCategory)
                .subCategory(subCategory)
                .age(age)
                .size(size)
                .color(color)
                .gender(gender)
                .build();

        log.info("[Controller] answerSheet End");

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), styleService.answer(pDTO)));
    }

    @PostMapping(value = "/answer")
    public ResponseEntity<CommonResponse> answer(HttpServletRequest request) throws Exception {

        log.info("[Controller] answer Start");

        String question = CmmUtil.nvl(request.getParameter("question"));

        log.info("question : " + question);

        log.info("[Controller] answer End");

        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), styleService.answer(question)));
    }
    
    @PostMapping(value = "/recommend")
    public ResponseEntity<CommonResponse> recommend() throws Exception {

        log.info("[Controller] recommend Start");




        log.info("[Controller] recommend End");

        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), null));
    }

    @PostMapping(value = "/list")
    public ResponseEntity<CommonResponse> list() throws Exception {
        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), styleService.styleList()));
    }
}
