package com.workingbit.article;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableMongoAuditing
public class ArticleApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArticleApplication.class, args);
  }
}
