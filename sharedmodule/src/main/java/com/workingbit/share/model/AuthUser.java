package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUser implements Payload {

  private String accessToken;
  private String session;
}
