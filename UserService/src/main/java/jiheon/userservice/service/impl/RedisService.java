package jiheon.userservice.service.impl;

import jiheon.userservice.service.IRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService implements IRedisService {

    private final RedisTemplate redisTemplate;

    // 키-밸류 설정
    @Override
    public void setValues(String token, String userId) throws Exception {

        log.info(this.getClass().getName() + ".setValues 실행");

        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(token, userId, Duration.ofMinutes(10));

        log.info(this.getClass().getName() + ".setValues 종료");
    }

    // 키값으로 벨류 가져오기
    @Override
    public String getValues(String token) throws Exception {

        log.info(this.getClass().getName() + ".getValues 실행");

        ValueOperations<String, String> values = redisTemplate.opsForValue();

        log.info(this.getClass().getName() + ".getValues 종료");

        return values.get(token);
    }

    // 키-벨류 삭제
    @Override
    public void delValues(String token) throws Exception {

        log.info(this.getClass().getName() + ".delValues 실행");

        redisTemplate.delete(token.substring(7));

        log.info(this.getClass().getName() + ".deValues 종료");
    }
}
