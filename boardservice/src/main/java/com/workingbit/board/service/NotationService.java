package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationMove;
import com.workingbit.share.model.NotationSimpleMove;

import java.util.Optional;

import static com.workingbit.board.BoardApplication.notationDao;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
public class NotationService {

  public Optional<Notation> findById(String notationId) {
    return notationDao.findById(notationId);
  }

  public void save(Notation notation) {
    notationDao.save(notation);
  }

  public void setSelectedNotationDrive(Notation notation, Board noHighlight) {
    for (NotationDrive notationDrive : notation.getNotationHistory().getNotation()) {
      for (NotationMove notationMove : notationDrive.getMoves()) {
        for (NotationSimpleMove notationSimpleMove : notationMove.getMove()) {
          if (notationSimpleMove.getBoardId().equals(noHighlight.getId())) {
            notationDrive.setSelected(true);
          } else {
            notationDrive.setSelected(false);
          }
        }
      }
    }
    notationDao.save(notation);
  }
}
