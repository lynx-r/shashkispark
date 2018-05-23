package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.ErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("UserCredentials")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserCredentials implements Payload {

  @NotNull(message = ErrorMessages.USERNAME_NOT_NULL)
  @Size(min = 3, max = 40, message = ErrorMessages.USERNAME_CONSTRAINTS)
  private String username;

  @NotNull(message = ErrorMessages.PASSWORD_NOT_NULL)
  @Size(min = 6, message = ErrorMessages.PASSWORD_CONSTRAINTS)
  private String password;

  private String creditCard;

  public UserCredentials(@NotNull(message = ErrorMessages.USERNAME_NOT_NULL) @Size(min = 3, max = 40, message = ErrorMessages.USERNAME_CONSTRAINTS) String username, @NotNull(message = ErrorMessages.PASSWORD_NOT_NULL) @Size(min = 6, message = ErrorMessages.PASSWORD_CONSTRAINTS) String password) {
    this.username = username;
    this.password = password;
  }

  @JsonIgnore
  public String getCredentials() {
    return username + ":" + password;
  }
}
