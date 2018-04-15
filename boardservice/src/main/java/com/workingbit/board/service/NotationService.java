package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;

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
}
