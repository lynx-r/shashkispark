package com.workingbit.article.index;

import spark.Request;
import spark.Response;
import spark.Route;

public class IndexController {
    public static Route serveIndexPage = (Request request, Response response) -> "Home… Sweet home!";
}