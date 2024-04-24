package jiheon.userservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:11000", "http://localhost:12000"},
        allowedHeaders = {"POST, GET", "FEIGN"},
        allowCredentials = "true",
        methods = {RequestMethod.POST, RequestMethod.GET},
        originPatterns = {"user/**"})
@Tag(name = "로그인된 사용자들이 접근하는 API", description = "로그인된 사용자들이 접근하는 API 설명입니다.")
@Slf4j
@RequestMapping(value = "/user/v1")
@RequiredArgsConstructor
@RestController
public class UserInfoController {


}
