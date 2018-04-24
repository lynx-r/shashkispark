package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.EnumSecureRole;

import java.util.Arrays;
import java.util.Optional;

import static com.workingbit.board.BoardEmbedded.notationDao;
import static com.workingbit.board.BoardEmbedded.notationStoreService;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
public class NotationService {

  public Optional<Notation> findById(AuthUser authUser, String notationId) {
    if (authUser == null) {
      return Optional.empty();
    }

    Optional<Notation> notationOptional = notationDao.findById(notationId);
    boolean secure = authUser.getRoles().containsAll(Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR));
    if (secure) {
      return notationOptional;
    } else {
      Notation notation = findNotationAndPutInStore(authUser, notationOptional, notationId);
      return Optional.ofNullable(notation);
    }
  }

  public void save(AuthUser authUser, Notation notation) {
    if (authUser == null) {
      return;
    }
    boolean secure = authUser.getRoles().containsAll(Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR));
    if (secure) {
      notation.setReadonly(false);
      notationDao.save(notation);
    } else {
      notationStoreService.put(authUser.getUserSession(), notation);
    }
  }

  private Notation findNotationAndPutInStore(AuthUser authUser, Optional<Notation> notationOptional, String notationId) {
    return notationStoreService.get(authUser.getUserSession(), notationId)
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
