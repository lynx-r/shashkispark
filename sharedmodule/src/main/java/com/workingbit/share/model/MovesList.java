package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MovesList
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-09-19T23:30:23.706+03:00")

public class MovesList {
  @JsonProperty("allowed")
  private List<com.workingbit.share.domain.impl.Square> allowed = new ArrayList<>();

  @JsonProperty("beaten")
  private List<com.workingbit.share.domain.impl.Square> beaten = new ArrayList<>();

  public MovesList allowed(List<com.workingbit.share.domain.impl.Square> allowed) {
    this.allowed = allowed;
    return this;
  }

  public MovesList addAllowedItem(com.workingbit.share.domain.impl.Square allowedItem) {
    if (this.allowed == null) {
      this.allowed = new ArrayList<com.workingbit.share.domain.impl.Square>();
    }
    this.allowed.add(allowedItem);
    return this;
  }

   /**
   * Get allowed
   * @return allowed
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<com.workingbit.share.domain.impl.Square> getAllowed() {
    return allowed;
  }

  public void setAllowed(List<com.workingbit.share.domain.impl.Square> allowed) {
    this.allowed = allowed;
  }

  public MovesList beaten(List<com.workingbit.share.domain.impl.Square> beaten) {
    this.beaten = beaten;
    return this;
  }

  public MovesList addBeatenItem(com.workingbit.share.domain.impl.Square beatenItem) {
    if (this.beaten == null) {
      this.beaten = new ArrayList<com.workingbit.share.domain.impl.Square>();
    }
    this.beaten.add(beatenItem);
    return this;
  }

   /**
   * Get beaten
   * @return beaten
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<com.workingbit.share.domain.impl.Square> getBeaten() {
    return beaten;
  }

  public void setBeaten(List<com.workingbit.share.domain.impl.Square> beaten) {
    this.beaten = beaten;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MovesList movesList = (MovesList) o;
    return Objects.equals(this.allowed, movesList.allowed) &&
        Objects.equals(this.beaten, movesList.beaten);
  }

  @Override
  public int hashCode() {
    return Objects.hash(allowed, beaten);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MovesList {\n");

    sb.append("    allowed: ").append(toIndentedString(allowed)).append("\n");
    sb.append("    beaten: ").append(toIndentedString(beaten)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

