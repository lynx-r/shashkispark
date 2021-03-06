//package com.workingbit.share.function;
//
//import com.workingbit.share.exception.RequestException;
//import com.workingbit.share.model.Answer;
//import com.workingbit.share.model.enumarable.IAuthority;
//import com.workingbit.share.model.Payload;
//import spark.Request;
//import spark.Response;
//
//import static com.workingbit.share.util.AuthRequestUtils.logRequest;
//import static com.workingbit.share.util.AuthRequestUtils.logResponse;
//import static com.workingbit.share.util.JsonUtils.dataToJson;
//import static com.workingbit.share.util.JsonUtils.jsonToData;
//
///**
// * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
// */
//@FunctionalInterface
//public interface ModelHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {
//
//  default String handleRequest(Request request, Response response, IAuthority path, Class<T> clazz) throws RequestException {
//    logRequest(request);
//
//    String json = request.body();
//    T data = jsonToData(json, clazz);
//
//    Answer answer = getAnswer(request, response, path, data);
//    response.status(answer.getStatusCode());
//
//    logResponse(request.url(), response, answer.getAuthUser());
//    return dataToJson(answer);
//  }
//
//  default String handleAuthRequest(Request request, Response response, Class<T> clazz) throws RequestException {
//    logRequest(request);
//
//    String json = request.body();
//    T data = jsonToData(json, clazz);
//
//    Answer answer = getAnswerForAuth(request, response, data);
//    response.status(answer.getStatusCode());
//
//    logResponse(request.url(), response, answer.getAuthUser());
//    return dataToJson(answer);
//  }
//
//}
