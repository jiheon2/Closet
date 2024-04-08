package jiheon.userservice.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {

    USER("ROLE_USER");

    private String value;
}
