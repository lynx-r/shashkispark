package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Data;

/**
 * CreateBoardPayload
 */
@JsonTypeName("ImportPdnPayload")
@Data
public class ImportPdnPayload implements Payload {
  private String articleId;
  private String pdn;
  private EnumRules rules;
  private EnumEditBoardBoxMode editMode;
  private int idInArticle;

  private ImportPdnPayload() {
  }

  @JsonCreator
  public ImportPdnPayload(@JsonProperty("article") String articleId,
                          @JsonProperty("pdn") String pdn,
                          @JsonProperty("idInArticle") int idInArticle,
                          @JsonProperty("editMode") EnumEditBoardBoxMode editMode
  ) {
    this.articleId = articleId;
    this.pdn = pdn;
    this.idInArticle = idInArticle;
    this.editMode = editMode;
  }

  public static ImportPdnPayload createBoardPayload() {
    return new ImportPdnPayload();
  }
}
