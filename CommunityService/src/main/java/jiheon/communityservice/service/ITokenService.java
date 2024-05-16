package jiheon.communityservice.service;

import jiheon.communityservice.dto.TokenDTO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@RefreshScope
@FeignClient(name = "ITokenService", url = "${api.gateway}")
public interface ITokenService {
    @PostMapping(value = "/user/v1/getTokenInfo")
    TokenDTO getTokenInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);
}
