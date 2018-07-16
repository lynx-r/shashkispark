package com.workingbit.share.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryaduhin on 18:06 15/08/2017.
 */
@Getter
@Setter
@Document(collection = BaseDomain.COLLECTION_NAME)
public class BaseDomain implements Serializable, DeepClone, Cloneable {

  static final String COLLECTION_NAME = "baseDomain";

  @Id
  private String id;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime modifiedAt;

}
