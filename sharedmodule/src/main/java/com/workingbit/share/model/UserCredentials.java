package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.ErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("UserCredentials")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCredentials implements Payload {

  @NotNull(message = ErrorMessages.USERNAME_NOT_NULL)
  @Size(min = 3, max = 40, message = ErrorMessages.USERNAME_CONSTRAINTS)
  private String username;

  //  @NotBlank(message = ErrorMessages.PASSWORD_NOT_NULL)
  @Size(min = 6, message = ErrorMessages.PASSWORD_CONSTRAINTS)
  private String password;

  //  @NotBlank(message = ErrorMessages.EMAIL_NOT_BLANK)
  @Email(message = ErrorMessages.INVALID_EMAIL)
  private String email;

  private String creditCard;

  public UserCredentials(@NotNull(message = ErrorMessages.USERNAME_NOT_NULL) @Size(min = 3, max = 40, message = ErrorMessages.USERNAME_CONSTRAINTS) String username, @NotNull(message = ErrorMessages.PASSWORD_NOT_NULL) @Size(min = 6, message = ErrorMessages.PASSWORD_CONSTRAINTS) String password) {
    this.username = username;
    this.password = password;
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public String toString() {
    return "UserCredentials{" +
        "username='" + username + '\'' +
        '}';
  }

  @org.jetbrains.annotations.NotNull
  @JsonIgnore
  public String getCredentials() {
    return username + ":" + password;
  }
}
