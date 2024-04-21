package jiheon.userservice.service;

public interface IRedisService {

    void setValues(String token, String userId) throws Exception;

    String getValues(String token) throws Exception;

    void delValues(String token) throws Exception;
}
