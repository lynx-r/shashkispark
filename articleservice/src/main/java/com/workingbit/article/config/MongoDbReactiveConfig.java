package com.workingbit.article.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * Created by Aleksey Popryadukhin on 11/07/2018.
 */
@EnableReactiveMongoRepositories
public class MongoDbReactiveConfig extends AbstractReactiveMongoConfiguration {

  @Override
  public MongoClient reactiveMongoClient() {
    return MongoClients.create();
  }

  @Override
  protected String getDatabaseName() {
    return "jsa_mongodb";
  }
}
