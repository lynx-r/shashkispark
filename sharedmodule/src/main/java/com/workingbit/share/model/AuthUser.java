package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("authUser")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUser implements Payload {

  private String accessToken;
  private String userSession;
}
