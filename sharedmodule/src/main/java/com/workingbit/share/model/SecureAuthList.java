package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksey Popryadukhin on 03/08/2018.
 */
@Data
@AllArgsConstructor
public class SecureAuthList {
  private List<SecureAuth> users;

  public SecureAuthList() {
    users = new ArrayList<>();
  }

  public void getUserAdd(SecureAuth secureAuth) {
    users.add(secureAuth);
  }
}
