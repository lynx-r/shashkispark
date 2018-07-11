//package com.workingbit.article;
//
//import org.jetbrains.annotations.NotNull;
//import org.junit.Test;
//
//import java.util.LinkedList;
//import java.util.stream.Collectors;
//
///**
// * Created by Aleksey Popryadukhin on 14/05/2018.
// */
//public class DaoFiltersTest {
//
//  interface BaseFilter {
//    String asString();
//  }
//
//  class Unary implements BaseFilter {
//
//    String condition;
//
//    public Unary(String condition) {
//      this.condition = condition;
//    }
//
//    @Override
//    public String asString() {
//      return condition;
//    }
//  }
//
//  class ValueFilter implements BaseFilter {
//    String key;
//    Object value;
//    String operator;
//    String type;
//
//    public ValueFilter(String key, Object value, String operator, String type) {
//      this.key = key;
//      this.value = value;
//      this.operator = operator;
//      this.type = type;
//    }
//
//    @NotNull
//    @Override
//    public String asString() {
//      return key + " " + operator + " " + value;
//    }
//  }
//
//  static class Filters {
//    LinkedList<BaseFilter> filters;
//
//    Filters() {
//      filters = new LinkedList<>();
//    }
//
//    @NotNull Filters add(BaseFilter filter) {
//      filters.add(filter);
//      return this;
//    }
//
//    String build() {
//      return filters.stream()
//          .map(BaseFilter::asString)
//          .collect(Collectors.joining(" "));
//    }
//  }
//
//  @Test
//  public void testFilters() {
//    Filters filters = new Filters();
//    filters
//        .add(new Unary("("))
//        .add(new ValueFilter("id", "123", "=", "S"))
//        .add(new Unary("or"))
//        .add(new ValueFilter("id", "321", "=", "S"))
//        .add(new Unary(")"))
//        .add(new Unary("and"))
//        .add(new ValueFilter("status", "DRAFT", "=", "S"));
//
//    System.out.println(filters.build());
//  }
//}
