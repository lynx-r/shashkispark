package com.workingbit.orchestrate.util;

import com.workingbit.orchestrate.config.ModuleProperties;
import com.workingbit.orchestrate.exception.OrchestrateException;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.SecureAuth;
import com.workingbit.share.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.workingbit.orchestrate.config.OrchestalConstants.*;

/**
 * Created by Aleksey Popryadukhin on 06/05/2018.
 */
public class RedisUtil {

  private static RedissonClient reddison;
  private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);
  private static ModuleProperties moduleProperties;

  public static void init(ModuleProperties moduleProperties) {
    RedisUtil.moduleProperties = moduleProperties;
    initReddisonClient();
  }

  public static void putInternalRequest(String internalKey, AuthUser authUser) {
    RMap<Object, Object> map = reddison.getMap(INTERNAL_REQUEST_MAP);
    String internalHash = authUser.getInternalHash();
    if (StringUtils.isBlank(internalHash)) {
      throw new OrchestrateException("Unable to createNotationDrives hash " + authUser.toString());
    }
    map.put(internalKey, internalHash);
  }

  public static boolean checkInternalRequest(String internalKey, AuthUser authUser) {
    RMap<Object, Object> map = reddison.getMap(INTERNAL_REQUEST_MAP);
    String reddisDigest = (String) map.get(internalKey);
    String inputDigest = authUser.getInternalHash();
    return reddisDigest.equals(inputDigest);
  }

  private static void initReddisonClient() {
    Config config = new Config();
//    config.setTransportMode(TransportMode.NIO);
    config.useSingleServer()
        .setAddress("redis://" + moduleProperties.redisHost() + ":" + moduleProperties.redisPort());
    config.setNettyThreads(4);
    config.setThreads(4);
    config.setCodec(new JsonJacksonCodec(JsonUtils.mapper));
    reddison = Redisson.create(config);
  }

  public static void cacheRequest(String key, Payload payload) {
    RMapCache<Object, Object> requestCache = reddison.getMapCache(CACHE_REQUEST_MAP);
    String hashCode = String.format("%x", payload.hashCode());
    RBucket<Object> answerBucket = reddison.getBucket(hashCode);
    answerBucket.set(payload);
    requestCache.put(key, answerBucket);
  }

  public static void cacheToken(AuthUser authUser) {
    RListMultimapCache<Object, Object> tokenCache = reddison.getListMultimapCache(CACHE_TOKEN_MAP);
    String key = authUser.getUserSession();
    String value = authUser.getAccessToken();
    if (!tokenCache.get(key).contains(value)) {
      tokenCache.put(key, value);
    }
  }

  public static String getTokenCache(AuthUser authUser) {
    RListMultimapCache<Object, Object> tokenCache = reddison.getListMultimapCache(CACHE_TOKEN_MAP);
    String key = authUser.getUserSession();
    String value = authUser.getAccessToken();
    RList<Object> tokens = tokenCache.get(key);
    boolean contains = tokens.contains(value);
    if (contains) {
      for (int i = tokens.size() - 1; i >= 0; i--) {
        String lastToken = (String) tokens.get(i);
        if (lastToken != null && !lastToken.equals(value)) {
          return lastToken;
        }
      }
    }
    return null;
  }

  public static void cacheSecureAuth(String userSession, SecureAuth authUser) {
    RMapCache<Object, Object> tokenCache = reddison.getMapCache(CACHE_SECURE_AUTH_USERSESSION);
    tokenCache.put(userSession, authUser);
  }

  @NotNull
  public static SecureAuth getSecureAuthByUserSession(String userSession) {
    RMapCache<Object, Object> mapCache = reddison.getMapCache(CACHE_SECURE_AUTH_USERSESSION);
    return (SecureAuth) mapCache.get(userSession);
  }

  public static void cacheSecureAuthUsername(String username, String userSession) {
    RMapCache<Object, Object> tokenCache = reddison.getMapCache(CACHE_SECURE_AUTH_USERNAME);
    tokenCache.put(username, userSession);
  }

  public static SecureAuth getSecureAuthByUsername(String username) {
    RMapCache<Object, Object> userSessionMap = reddison.getMapCache(CACHE_SECURE_AUTH_USERNAME);
    String userSession = (String) userSessionMap.get(username);
    if (userSession == null) {
      return null;
    }
    RMapCache<Object, Object> mapCache = reddison.getMapCache(CACHE_SECURE_AUTH_USERSESSION);
    return (SecureAuth) mapCache.get(userSession);
  }

  public static void removeSecureAuthByUserSession(String userSession) {
    RMapCache<Object, Object> userSessionMap = reddison.getMapCache(CACHE_SECURE_AUTH_USERSESSION);
    userSessionMap.remove(userSession);
  }

  public static void removeSecureAuthByUserUsername(String username) {
    RMapCache<Object, Object> userSessionMap = reddison.getMapCache(CACHE_SECURE_AUTH_USERNAME);
    userSessionMap.remove(username);
  }
}
