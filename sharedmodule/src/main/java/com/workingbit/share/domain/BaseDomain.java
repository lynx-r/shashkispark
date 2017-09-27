package com.workingbit.share.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Aleksey Popryaduhin on 18:06 15/08/2017.
 */
public interface BaseDomain extends Serializable, DeepClone, Cloneable {

  String getId();

  void setId(String id);

  Date getCreatedAt();

  void setCreatedAt(Date createdAt);
}
