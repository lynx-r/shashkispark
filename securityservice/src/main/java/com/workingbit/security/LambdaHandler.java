package com.workingbit.security;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
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
        SecurityApplication.start();
      } catch (ContainerInitializationException e) {
        throw new RuntimeException("Failed to initialize server container", e);
      }
    }

    return handler.proxy(input, context);
  }
}