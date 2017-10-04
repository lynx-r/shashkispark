package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Created by Aleksey Popryaduhin on 21:30 03/10/2017.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Notation {

  private String whitePlayer;
  private String blackPlayer;
  private String event;
  private String site;
  private String round;
  private LocalDate date;
  private String result;
  private String gameType;

  private NotationStrokes notationStrokes = new NotationStrokes();
}
