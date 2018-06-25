package com.workingbit.share.common;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Aleksey Popryadukhin on 14/05/2018.
 */
public interface ISharedProperties {
  @NotNull List<String> validFilters();

  String adminMail();
}
