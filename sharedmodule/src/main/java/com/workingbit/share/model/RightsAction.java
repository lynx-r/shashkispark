package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.workingbit.share.common.DBConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@DynamoDBTable(tableName = DBConstants.RIGHTS_ACTIONS)
public class RightsAction {

  private DomainId rightsId;
  private DomainId groupId;
  private int sign;
  private EnumAction action;
}
