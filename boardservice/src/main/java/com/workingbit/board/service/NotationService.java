package com.workingbit.board.service;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.Utils;

import java.util.stream.Collectors;

import static com.workingbit.board.BoardEmbedded.*;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
class NotationService {

  Notation findById(AuthUser authUser, DomainId notationId) {
    if (authUser == null) {
      throw RequestException.notFound404();
    }

    var notation = notationDao.findById(notationId);
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      return notation;
    } else {
      NotationDrives notTask = notation.getNotationHistory().getNotation()
          .stream()
          .filter(nd -> !nd.isTaskBelow())
          .collect(Collectors.toCollection(NotationDrives::new));
      if (notation.getNotationHistory().getNotation().size() != notTask.size()) {
        notation.getNotationHistory().setNotation(notTask);
      }
      return notation;
    }
  }

  void save(AuthUser authUser, Notation notation) {
    if (authUser == null) {
      return;
    }
    notation.setAsTreeString(notation.asTreeString());
    notation.setAsString(notation.asString());
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      notation.setReadonly(false);
      notationDao.save(notation);
    } else {
      notationStoreService.put(authUser.getUserSession(), notation);
    }
  }

  private Notation findNotationAndPutInStore(AuthUser authUser, Notation notation, DomainId notationId) {
    return notationStoreService.get(authUser.getUserSession(), notationId)
        .orElseGet(() -> {
          notation.setReadonly(true);
          notationStoreService.put(authUser.getUserSession(), notation);
          return notation;
        });
  }

  public void createNotationForBoardBox(BoardBox boardBox) {
    Notation notation = new Notation();
    Utils.setRandomIdAndCreatedAt(notation);
    notation.setBoardBoxId(boardBox.getDomainId());
    notation.setRules(boardBox.getBoard().getRules());
    boardBox.setNotationId(notation.getDomainId());
    boardBox.setNotation(notation);
  }

  public BoardBox clearNotationInBoardBox(BoardBox bb) {
    // clear notation
    var notation = notationDao.findById(bb.getNotationId());
    clearNotation(notation);
    bb.setNotation(notation);
    boardBoxDao.save(bb);
    return bb;
  }

  private void clearNotation(Notation notation) {
    notation.setNotationHistory(NotationHistory.createWithRoot());
    notationDao.save(notation);
  }
}
