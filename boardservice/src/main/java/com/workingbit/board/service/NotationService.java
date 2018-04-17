package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.EnumSecureRole;

import java.util.Optional;

import static com.workingbit.board.BoardApplication.notationDao;
import static com.workingbit.board.BoardApplication.notationStoreService;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
public class NotationService {

  public Optional<Notation> findById(AuthUser authUser, String notationId) {
    if (authUser == null
        || authUser.getRole().equals(EnumSecureRole.ADMIN)
        || authUser.getRole().equals(EnumSecureRole.EDITOR)) {
      return notationDao.findById(notationId);
    }
    if (authUser.getRole().equals(EnumSecureRole.ANONYMOUSE)) {
      return notationStoreService.get(authUser.getUserSession());
    }
    return Optional.empty();
  }

  public void save(AuthUser authUser, Notation notation) {
    if (authUser == null) {
      return;
    }
    switch (authUser.getRole()) {
      case ADMIN:
      case EDITOR:
        notationDao.save(notation);
        return;
      default:
        notationStoreService.put(authUser.getUserSession(), notation);
    }
  }
}
