package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterUser implements Payload {

  private String username;
  private String password;

  public String getCredentials() {
    return username + ":" + password;
  }
}
