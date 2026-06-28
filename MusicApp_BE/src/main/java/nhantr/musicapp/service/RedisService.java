package nhantr.musicapp.service;

public interface RedisService {

    void blacklistToken(String token, long ttlInMs);

    boolean isTokenBlacklisted(String token);

    void setValue(String key, String value, long ttlInSeconds);

    String getValue(String key);

    void deleteKey(String key);
}
