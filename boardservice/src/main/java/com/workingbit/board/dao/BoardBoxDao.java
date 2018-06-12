package com.workingbit.board.dao;

import com.workingbit.board.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.dao.Unary;
import com.workingbit.share.dao.ValueFilter;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.BoardBoxes;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.DomainIds;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Aleksey Popryaduhin on 06:55 22/09/2017.
 */
public class BoardBoxDao extends BaseDao<BoardBox> {

  public BoardBoxDao(AppProperties appProperties) {
    super(BoardBox.class, appProperties.regionDynamoDB(), appProperties.endpointDynamoDB().toString(), appProperties.local());
  }

  public List<BoardBox> findPublicByIds(@NotNull DomainIds boardBoxIds) {
    DaoFilters filterPublic = new DaoFilters();
    filterPublic.add(new ValueFilter("visiblePublic", true, "=", "BOOL"));
    filterPublic.add(new Unary("("));
    boardBoxIds.getIds().forEach(id ->
        filterPublic.add(new ValueFilter("id", id.getId(), "=", "S")));
    filterPublic.add(new Unary(")"));
    return findByFilter(filterPublic);
  }

  public List<BoardBox> findByIdsAndAuthor(@NotNull DomainIds boardBoxIds, @NotNull AuthUser authUser) {
    DaoFilters filterPublic = new DaoFilters();
    filterPublic
        .add(new ValueFilter("userId.id", authUser.getUserId().getId(), "=", "S"));
    if (!boardBoxIds.isEmpty()) {
      filterPublic.add(new Unary("and"));
    }
    if (boardBoxIds.size() > 1) {
      filterPublic.add(0, new Unary("("));
    }
    boardBoxIds.getIds().forEach(id ->
        filterPublic.add(new ValueFilter("id", id.getId(), "=", "S"))
            .add(new Unary("or"))
    );
    if (!boardBoxIds.isEmpty()) {
      filterPublic.removeLast();
    }
    if (boardBoxIds.size() > 1) {
      filterPublic.add(new Unary(")"));
    }
    return findByFilter(filterPublic);
  }

  public BoardBoxes findByArticleId(@NotNull DomainId articleId) {
    DaoFilters filterPublic = new DaoFilters();
    filterPublic.add(new ValueFilter("articleId.id", articleId.getId(), "=", "S"));
    return new BoardBoxes(findByFilter(filterPublic));
  }

  public BoardBoxes findPublicByArticleId(@NotNull DomainId articleId) {
    DaoFilters filterPublic = new DaoFilters();
    filterPublic.add(new ValueFilter("visiblePublic", true, "=", "BOOL"));
    filterPublic.add(new Unary("and"));
    filterPublic.add(new ValueFilter("articleId.id", articleId.getId(), "=", "S"));
    return new BoardBoxes(findByFilter(filterPublic));
  }

  public BoardBox findPublicById(@NotNull DomainId boardBoxId) {
    DaoFilters filterPublic = new DaoFilters();
    filterPublic.add(new ValueFilter("visiblePublic", true, "=", "BOOL"));
    filterPublic.add(new Unary("and"));
    filterPublic.add(new ValueFilter("id", boardBoxId.getId(), "=", "S"));
    List<BoardBox> byFilter = findByFilter(filterPublic);
    if (byFilter.isEmpty()) {
      throw DaoException.notFound();
    }
    if (byFilter.size() > 1) {
      throw DaoException.expectedOneResult();
    }
    return byFilter.get(0);
  }

//  public List<BoardBox> findByUserAndIds(int limit, String userId, Set<String> boardBoxIds) {
//    List<SimpleFilter> filterList = new ArrayList<>();
//    filterList.push(new SimpleFilter("userId", userId," = ", "S"));
//    return findByFilter(limit, filterList, null, new HashMap<>(), null, null);
//  }

}
