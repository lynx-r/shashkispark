package com.workingbit.share.domain;

import com.rits.cloning.Cloner;

/**
 * Created by Aleksey Popryaduhin on 10:38 20/09/2017.
 */
public interface DeepClone {
  default Object deepClone() {
    Cloner cloner = new Cloner();
    return cloner.deepClone(this);
  };
}
