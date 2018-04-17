package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.model.AuthUser;

import java.util.Optional;

import static com.workingbit.board.BoardApplication.notationDao;
import static com.workingbit.board.BoardApplication.notationStoreService;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
public class NotationService {

  public Optional<Notation> findById(AuthUser authUser, String notationId) {
    if (authUser == null) {
      return Optional.empty();
    }

    Optional<Notation> notationOptional = notationDao.findById(notationId);
    switch (authUser.getRole()) {
      case ADMIN:
      case EDITOR:
        return notationOptional;
      default:
        Notation notation = findNotationAndPutInStore(authUser, notationOptional);
        return Optional.ofNullable(notation);
    }
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

  private Notation findNotationAndPutInStore(AuthUser authUser, Optional<Notation> notationOptional) {
    return notationStoreService.get(authUser.getUserSession())
        .orElseGet(() -> {
          if (notationOptional.isPresent()) {
            Notation notation = notationOptional.get();
            notation.setReadonly(true);
            notationStoreService.put(authUser.getUserSession(), notation);
            return notation;
          }
          return null;
        });
  }
}
