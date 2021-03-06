package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Subscriber;

/**
 * Created by Aleksey Popryaduhin on 16:13 01/10/2017.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateArticlePayload.class, name = "CreateArticlePayload"),
    @JsonSubTypes.Type(value = CreateArticleResponse.class, name = "CreateArticleResponse"),
    @JsonSubTypes.Type(value = CreateBoardPayload.class, name = "CreateBoardPayload"),
    @JsonSubTypes.Type(value = ImportPdnPayload.class, name = "ImportPdnPayload"),

    @JsonSubTypes.Type(value = DomainIds.class, name = "DomainIds"),
    @JsonSubTypes.Type(value = DomainId.class, name = "DomainId"),

    @JsonSubTypes.Type(value = Article.class, name = "Article"),
    @JsonSubTypes.Type(value = Articles.class, name = "Articles"),
    @JsonSubTypes.Type(value = BoardBox.class, name = "BoardBox"),
    @JsonSubTypes.Type(value = BoardBoxes.class, name = "BoardBoxes"),
    @JsonSubTypes.Type(value = Board.class, name = "Board"),

    @JsonSubTypes.Type(value = RegisteredUser.class, name = "RegisteredUser"),
    @JsonSubTypes.Type(value = UserCredentials.class, name = "UserCredentials"),
    @JsonSubTypes.Type(value = AuthUser.class, name = "AuthUser"),
    @JsonSubTypes.Type(value = UserInfo.class, name = "UserInfo"),
    @JsonSubTypes.Type(value = ParamPayload.class, name = "Params"),
    @JsonSubTypes.Type(value = ResultPayload.class, name = "Result"),
    @JsonSubTypes.Type(value = EmptyBody.class, name = "EmptyBody"),

    @JsonSubTypes.Type(value = Subscriber.class, name = "Subscriber"),
    @JsonSubTypes.Type(value = Subscribed.class, name = "Subscribed")
})
public interface Payload {
}
