package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.ErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
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
public class RegisteredUser implements Payload {

  @NotNull(message = ErrorMessages.FIRSTNAME_NOT_NULL)
  @Size(min = 2, max = 40, message = ErrorMessages.FIRSTNAME_CONSTRAINTS)
  private String firstname;

  @Size(max = 40, message = ErrorMessages.MIDDLENAME_CONSTRAINTS)
  private String middlename;

  @NotNull(message = ErrorMessages.LASTNAME_NOT_NULL)
  @Size(min = 2, max = 40, message = ErrorMessages.FIRSTNAME_CONSTRAINTS)
  private String secondname;

  private EnumRank rank;

  @NotNull(message = ErrorMessages.EMAIL_NOT_BLANK)
  @Email(message = ErrorMessages.INVALID_EMAIL)
  private String email;

  @NotBlank(message = ErrorMessages.PASSWORD_NOT_NULL)
  @Size(min = 12, message = ErrorMessages.PASSWORD_CONSTRAINTS)
  private String passwordHash;

  @org.jetbrains.annotations.NotNull
  @Override
  public String toString() {
    return "UserCredentials{" +
        "email='" + firstname + '\'' +
        '}';
  }

  @org.jetbrains.annotations.NotNull
  @JsonIgnore
  public String getCredentials() {
    return firstname + ":" + passwordHash;
  }
}
