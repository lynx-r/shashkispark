package com.workingbit.article;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

  private SparkLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

  @Override
  public AwsProxyResponse handleRequest(AwsProxyRequest input, Context context) {
    if (handler == null) {
      try {
        handler = SparkLambdaContainerHandler.getAwsProxyHandler();
        ArticleApplication.start();
      } catch (ContainerInitializationException e) {
        throw new RuntimeException("Failed to initialize server container", e);
      }
    }

    return handler.proxy(input, context);
  }
}