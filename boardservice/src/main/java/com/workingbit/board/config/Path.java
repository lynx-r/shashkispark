package com.workingbit.board.config;

import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.IPath;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

public enum Path implements IPath {

  HOME("/", false, emptySet()),

  BOARD_ADD_DRAUGHT("/board/add-draught", true, Constants.BOARD_SECURE_ROLES),
  BOARD("/board", false, emptySet()),
  BOARD_BY_ID("/board/:id", false, emptySet()),
  BOARD_MOVE("/board/move", true, Constants.BOARD_SECURE_ROLES),
  BOARD_HIGHLIGHT("/board/highlight", true, Constants.BOARD_SECURE_ROLES),
  BOARD_REDO("/board/redo", true, Constants.BOARD_SECURE_ROLES),
  BOARD_UNDO("/board/undo", true, Constants.BOARD_SECURE_ROLES),
  BOARD_LOAD_PREVIEW("/board/load-board-preview", false, emptySet()),
  BOARD_SWITCH("/board/switch", false, emptySet()),
  BOARD_FORK("/board/fork", true, Constants.BOARD_SECURE_ROLES),
  BOARD_VIEW_BRANCH("/board/view-branch", false, emptySet()),
  CHANGE_TURN("/board/change-turn", true, Constants.BOARD_SECURE_ROLES);

  private String path;
  private boolean secure;
  private Set<EnumSecureRole> roles;

  Path(String path, boolean secure, Set<EnumSecureRole> roles) {
    this.path = path;
    this.secure = secure;
    this.roles = roles;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public boolean isSecure() {
    return secure;
  }

  @Override
  public Set<EnumSecureRole> getRoles() {
    return roles;
  }

  public Path setPath(String path) {
    this.path = path;
    return this;
  }

  public Path setSecure(boolean secure) {
    this.secure = secure;
    return this;
  }

  public Path setRoles(Set<EnumSecureRole> roles) {
    this.roles.addAll(roles);
    return this;
  }

  public static class Constants {
    public static final HashSet<EnumSecureRole> BOARD_SECURE_ROLES = new HashSet<>(Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR));
  }
}