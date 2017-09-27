package com.workingbit.article.util;

import spark.Filter;
import spark.Request;
import spark.Response;

public class Filters {

    // Enable GZIP for all responses
    public static Filter addGzipHeader = (Request request, Response response) ->
        response.header("Content-Encoding", "gzip");
    ;
}