package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.enumarable.EnumAuthority;

import java.util.Optional;

import static com.workingbit.board.BoardEmbedded.notationDao;
import static com.workingbit.board.BoardEmbedded.notationStoreService;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
class NotationService {

  Optional<Notation> findById(AuthUser authUser, String notationId) {
    if (authUser == null) {
      return Optional.empty();
    }

    Optional<Notation> notationOptional = notationDao.findById(notationId);
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      return notationOptional;
    } else {
      if (notationOptional.isPresent()) {
        Notation notation = findNotationAndPutInStore(authUser, notationOptional.get(), notationId);
        return Optional.of(notation);
      }
      return Optional.empty();
    }
  }

  void save(AuthUser authUser, Notation notation) {
    if (authUser == null) {
      return;
    }
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      notation.setReadonly(false);
      notationDao.save(notation);
    } else {
      notationStoreService.put(authUser.getUserSession(), notation);
    }
  }

  private Notation findNotationAndPutInStore(AuthUser authUser, Notation notation, String notationId) {
    return notationStoreService.get(authUser.getUserSession(), notationId)
        .orElseGet(() -> {
            notation.setReadonly(true);
            notationStoreService.put(authUser.getUserSession(), notation);
            return notation;
        });
  }
}
