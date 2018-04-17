package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;

/**
 * Created by Aleksey Popryaduhin on 16:13 01/10/2017.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateArticlePayload.class, name = "createArticlePayload"),
    @JsonSubTypes.Type(value = CreateArticleResponse.class, name = "createArticleResponse"),
    @JsonSubTypes.Type(value = CreateBoardPayload.class, name = "createBoardPayload"),

    @JsonSubTypes.Type(value = Article.class, name = "article"),
    @JsonSubTypes.Type(value = Articles.class, name = "articles"),
    @JsonSubTypes.Type(value = BoardBox.class, name = "boardBox"),
    @JsonSubTypes.Type(value = Board.class, name = "board"),

    @JsonSubTypes.Type(value = RegisterUser.class, name = "registerUser"),
    @JsonSubTypes.Type(value = AuthUser.class, name = "authUser"),
    @JsonSubTypes.Type(value = UserInfo.class, name = "userInfo"),
    @JsonSubTypes.Type(value = ParamPayload.class, name = "params")
})
public interface Payload {
}
