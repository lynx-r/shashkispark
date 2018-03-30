package com.workingbit.share.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryaduhin on 18:06 15/08/2017.
 */
public abstract class BaseDomain implements Serializable, DeepClone, Cloneable {

  public abstract String getId();

  public abstract void setId(String id);

  public abstract LocalDateTime getCreatedAt();

  public abstract void setCreatedAt(LocalDateTime createdAt);

  public abstract LocalDateTime getUpdatedAt();

  public abstract void setUpdatedAt(LocalDateTime createdAt);
}
