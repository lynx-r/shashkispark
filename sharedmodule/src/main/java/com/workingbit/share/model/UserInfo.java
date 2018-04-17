package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("userInfo")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInfo implements Payload {

  private String username;

}
