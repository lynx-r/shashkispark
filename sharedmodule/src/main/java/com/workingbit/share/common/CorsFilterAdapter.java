package com.workingbit.share.common;

/**
 * Created by Aleksey Popryaduhin on 19:04 26/09/2017.
 */
public class CorsFilterAdapter {

  private final String[] clientUrls;
  private final String[] headers;
  private final String[] methods;

  public CorsFilterAdapter(String clientUrls, String headers, String methods) {
    this.clientUrls = clientUrls.split(",");
    this.headers = headers.split(",");
    this.methods = methods.split(",");
  }
}
