package com.workingbit.share.common;

/**
 * Created by Aleksey Popryaduhin on 16:49 01/10/2017.
 */
public class RequestConstants {
  public static final String LIMIT = "limit";
  public static final String ID = ":id";
  public static final String HRU = ":hru";
  public static final String BBID = ":bbid";
  public static final String SIGN = "sign";
  public static final String SIGN_REQUEST = "sign-request";
  public static final int SESSION_LENGTH = 20;
  public static final int COOKIE_AGE = 31 * 24 * 60 * 60;
  public static final String ANONYMOUS_SESSION_HEADER = "anonymous-session";
  public static final String USER_SESSION_HEADER = "user-session";
  public static final String USER_ROLE_HEADER = "user-authorities";
  public static final String FILTERS_HEADER = "filters";
  public static final String AUTH_COUNTER_HEADER = "auth-timestamp";
  public static final String ACCESS_TOKEN_HEADER = "access-token";
  public static final String INTERNAL_KEY_HEADER = "internal-key";
  public static final String SUPER_HASH_HEADER = "super-hash";
  public static final String USER_ID_HEADER = "user-id";
  public static final String USERNAME_HEADER = "username";
  public static final String USER_CREATED_AT_HEADER = "user-created-at";
  public static final String PUBLIC_QUERY = "public";
}
