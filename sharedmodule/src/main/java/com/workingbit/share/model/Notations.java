package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Notation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksey Popryadukhin on 21/05/2018.
 */
public class Notations {
  
  private Map<String, Notation> notations;

  public Notations() {
    notations = new HashMap<>();
  }

  public void put(String key, Notation notation) {
    notations.put(key, notation);
  }

  public void putAll(@NotNull Map<String, Notation> notations) {
    this.notations.putAll(notations);
  }

  public int size() {
    return notations.size();
  }
}
