package com.workingbit.board.config;

import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.IAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Authority implements IAuthority {

  HOME("/", new HashSet<>()),

  BOARD_ADD_DRAUGHT("/board/add-draught", Constants.BOARD_SECURE_ROLES),
  BOARD("/board", new HashSet<>()),
  BOARD_BY_ID("/board/:id", new HashSet<>()),
  BOARD_MOVE("/board/move", Constants.BOARD_SECURE_ROLES),
  BOARD_HIGHLIGHT("/board/highlight", Constants.BOARD_SECURE_ROLES),
  BOARD_REDO("/board/redo", Constants.BOARD_SECURE_ROLES),
  BOARD_UNDO("/board/undo", Constants.BOARD_SECURE_ROLES),
  BOARD_LOAD_PREVIEW("/board/load-board-preview", new HashSet<>()),
  BOARD_SWITCH("/board/switch", new HashSet<>()),
  BOARD_FORK("/board/fork", Constants.BOARD_SECURE_ROLES),
  BOARD_VIEW_BRANCH("/board/view-branch", new HashSet<>()),
  CHANGE_TURN("/board/change-turn", Constants.BOARD_SECURE_ROLES);

  private String path;
  private Set<EnumAuthority> authorities;

  Authority(String path, Set<EnumAuthority> authorities) {
    this.path = path;
    this.authorities = authorities;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Set<EnumAuthority> getAuthorities() {
    return authorities;
  }

  public Authority setAuthorities(Set<EnumAuthority> authorities) {
    this.authorities.addAll(authorities);
    return this;
  }

  public static class Constants {
    public static final HashSet<EnumAuthority> BOARD_SECURE_ROLES = new HashSet<>(Arrays.asList(EnumAuthority.ADMIN, EnumAuthority.AUTHOR));
  }
}