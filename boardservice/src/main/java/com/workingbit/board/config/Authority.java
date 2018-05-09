package com.workingbit.board.config;

import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.IAuthority;

import java.util.HashSet;
import java.util.Set;

public enum Authority implements IAuthority {

  HOME("/", new HashSet<>()),

  // for everybody
  BOARD_VIEW_BRANCH("/board/view-branch", Constants.INSECURE_ROLES),
  BOARD_BY_ID("/board/:id", Constants.INSECURE_ROLES),
  BOARD_BY_IDS("/board/by-ids", Constants.INSECURE_ROLES),
  BOARD_LOAD_PREVIEW("/board/load-board-preview", Constants.INSECURE_ROLES),
  BOARD_SWITCH("/board/switch", Constants.INSECURE_ROLES),

  // must be protected
  BOARD("/board", Constants.SECURE_ROLES),
  PARSE_PDN("/parse-pdn", Constants.SECURE_ROLES),
  BOARD_PUT("/board", Constants.SECURE_ROLES),
  BOARD_ADD_DRAUGHT("/board/add-draught", Constants.SECURE_ROLES),
  BOARD_MOVE("/board/move", Constants.SECURE_ROLES),
  BOARD_HIGHLIGHT("/board/highlight", Constants.SECURE_ROLES),
  BOARD_REDO("/board/redo", Constants.SECURE_ROLES),
  BOARD_UNDO("/board/undo", Constants.SECURE_ROLES),
  BOARD_FORK("/board/fork", Constants.SECURE_ROLES),
  CHANGE_TURN("/board/change-turn", Constants.SECURE_ROLES)
  ;

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
    public static final HashSet<EnumAuthority> SECURE_ROLES = new HashSet<>(Set.of(EnumAuthority.ADMIN, EnumAuthority.AUTHOR, EnumAuthority.INTERNAL));
    private static final HashSet<EnumAuthority> INSECURE_ROLES = new HashSet<>();
  }
}