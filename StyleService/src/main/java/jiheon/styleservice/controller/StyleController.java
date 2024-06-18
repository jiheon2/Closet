package jiheon.styleservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jiheon.styleservice.controller.response.CommonResponse;
import jiheon.styleservice.dto.StyleDTO;
import jiheon.styleservice.service.impl.StyleService;
import jiheon.styleservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://gateway:11000", "http://front:12000", "http://style:18000"},
        allowCredentials = "true",
        allowedHeaders = {"Content-Type, Authorization"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@RestController
@RequestMapping(value = "/style/v1")
@Slf4j
@RequiredArgsConstructor
public class StyleController {

    private final StyleService styleService;

    @PostMapping(value = "/saveVector")
    public void saveVector(@RequestParam(value = "folder") String folder) throws Exception {

        log.info("[Controller] saveVector Start");

        log.info("folder : " + folder);

        String folderPath = "C:\\Closet\\StyleService\\src\\main\\resources\\json\\" + folder;

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
    public ResponseEntity<CommonResponse> answer(@RequestParam(value = "question", defaultValue = "의류 스타일 3가지만 추천해줘") String question) throws Exception {
        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), styleService.answer(question)));
    }

    @PostMapping(value = "/dictionary")
    public ResponseEntity<CommonResponse> dictionary(@RequestParam(value = "style", defaultValue = "레트로") String style) throws Exception {
        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), styleService.styleDictionary(style)));
    }

    @PostMapping(value = "/styleInfo")
    public ResponseEntity<CommonResponse> styleInfo(@RequestParam(value = "imageNum") int imageNum) throws Exception {
        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), styleService.styleInfo(imageNum)));
    }

    @GetMapping(value = "/shop")
    public ResponseEntity<CommonResponse> shop(@RequestParam(value = "item") String item) throws Exception {
        return ResponseEntity.ok(CommonResponse.of(
                HttpStatus.OK, HttpStatus.OK.series().name(), styleService.shop(item)));
    }
}
