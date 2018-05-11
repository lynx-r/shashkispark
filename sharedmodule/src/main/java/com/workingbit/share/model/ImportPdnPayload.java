package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Data;

/**
 * CreateBoardPayload
 */
@JsonTypeName("ImportPdnPayload")
@Data
public class ImportPdnPayload implements Payload {
  private DomainId articleId;
  private String pdn;
  private EnumRules rules;

  private ImportPdnPayload() {
  }

  @JsonCreator
  public ImportPdnPayload(@JsonProperty("articleId") DomainId articleId,
                          @JsonProperty("pdn") String pdn
  ) {
    this.articleId = articleId;
    this.pdn = pdn;
  }

  public static ImportPdnPayload createBoardPayload() {
    return new ImportPdnPayload();
  }
}
