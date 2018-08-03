package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.workingbit.share.model.enumarable.EnumAuthority;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 03/08/2018.
 */
public class AuthoritySetConverter implements DynamoDBTypeConverter<Set<String>, Set<EnumAuthority>> {
  @Override
  public Set<String> convert(Set<EnumAuthority> object) {
    Set<String> result = new HashSet<>();
    if (object != null) {
      object.forEach(e -> result.add(e.name()));
    }
    return result;
  }

  @Override
  public Set<EnumAuthority> unconvert(Set<String> object) {
    Set<EnumAuthority> result = new HashSet<>();
    if (object != null) {
      object.forEach(e -> result.add(EnumAuthority.valueOf(e)));
    }
    return result;
  }
}
