package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("RegisterUser")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterUser implements Payload {

  private String username;
  private String password;

  @JsonIgnore
  public String getCredentials() {
    return username + ":" + password;
  }
}
