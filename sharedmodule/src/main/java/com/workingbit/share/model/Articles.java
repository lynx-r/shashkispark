package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.Article;
import lombok.Data;

import java.util.List;

/**
 * Created by Aleksey Popryaduhin on 19:31 01/10/2017.
 */
@JsonTypeName("articles")
@Data
public class Articles implements Payload {
  private List<Article> articles;
}
