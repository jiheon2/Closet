package jiheon.apigateway.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {

    USER("ROLE_USER"); // 권한 변수명 정의

    private String value;
}
