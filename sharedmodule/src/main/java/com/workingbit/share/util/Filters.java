package com.workingbit.share.util;

import org.jetbrains.annotations.NotNull;
import spark.Filter;
import spark.Request;
import spark.Response;

public class Filters {

    // Enable GZIP for all responses
    @NotNull
    public static Filter addGzipHeader = (Request request, Response response) ->
        response.header("Content-Encoding", "br");

    @NotNull
    public static Filter addJsonHeader = (req, res) -> res.type("application/json");
}