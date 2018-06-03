package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.NotationFen;
import com.workingbit.share.model.NotationHistory;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.workingbit.board.BoardEmbedded.*;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
class NotationService {

//  Notation findById(@NotNull DomainId notationId, @Nullable AuthUser authUser) {
//    if (authUser == null) {
//      throw RequestException.notFound404();
//    }
//    return notationDao.findById(notationId);
//  }

  void save(@NotNull Notation notation, @Nullable AuthUser authUser) {
    if (authUser == null) {
      return;
    }
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      notation.setReadonly(false);
      notationDao.save(notation);
      notationStoreService.remove(notation);
    } else {
      notationStoreService.put(authUser.getUserSession(), notation);
    }
  }

  void createNotationForBoardBox(@NotNull BoardBox boardBox) {
    Notation notation = new Notation();
    Utils.setRandomIdAndCreatedAt(notation);
    notation.setBoardBoxId(boardBox.getDomainId());
    notation.setRules(boardBox.getBoard().getRules());
    boardBox.setNotationId(notation.getDomainId());
    boardBox.setNotation(notation);
  }

  @NotNull
  BoardBox clearNotationInBoardBox(@NotNull BoardBox bb) {
    // clear notation
    var notation = notationDao.findById(bb.getNotationId());
    clearNotation(notation);
    bb.setNotation(notation);
    boardBoxDao.save(bb);
    return bb;
  }

  private void clearNotation(Notation notation) {
    notation.setNotationHistory(NotationHistory.createWithRoot());
    notation.setNotationFen(new NotationFen());
    notationDao.save(notation);
  }

  void removeVariant(@NotNull BoardBox boardBox) {
    NotationHistory notationHistory = boardBox.getNotation().getNotationHistory();
    int currentNotationDrive = notationHistory.getCurrentNotationDrive();
    int variantNotationDrive = notationHistory.getVariantNotationDrive();

    notationHistory
        .removeByNotationNumberInVariants(currentNotationDrive, variantNotationDrive);

    notationDao.save(boardBox.getNotation());
  }

  void setNotationFenFromBoard(@NotNull Notation notation, @NotNull Board board) {
    NotationFen notationFen = new NotationFen();
    notationFen.setBoardId(board.getDomainId());
    int dimension = board.getRules().getDimension();
    updateDraughtsDimension(dimension, board.getBlackDraughts());
    updateDraughtsDimension(dimension, board.getWhiteDraughts());
    notationFen.setBlackTurn(board.isBlackTurn());
    notationFen.setSequenceFromBoard(board.getBlackDraughts(), true);
    notationFen.setSequenceFromBoard(board.getWhiteDraughts(), false);
    notation.setNotationFen(notationFen);
  }

  private void updateDraughtsDimension(int dimension, Map<String, Draught> whiteDraughts) {
    whiteDraughts.replaceAll((notation, draught) -> {
      draught.setDim(dimension);
      return draught;
    });
  }

  Notation findById(@NotNull DomainId notationId, @Nullable AuthUser authUser) {
    if (authUser == null) {
      throw RequestException.notFound404();
    }
    return notationStoreService
        .get(authUser.getUserSession(), notationId)
        .orElseGet(() -> notationDao.findById(notationId));
  }
}
