package com.workingbit.article.service;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Subscriber;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.Subscribed;
import com.workingbit.share.util.Utils;

import static com.workingbit.article.ArticleEmbedded.emailService;
import static com.workingbit.article.ArticleEmbedded.subscriberDao;

/**
 * Created by Aleksey Popryadukhin on 18/06/2018.
 */
public class SubscriberService {

  Subscribed subscribe(Subscriber subscriber) {
    boolean found = false;
    try {
      subscriberDao.findByEmail(subscriber.getEmail());
      found = true;
    } catch (DaoException ignore) {
    }
    if (found) {
      throw RequestException.badRequest(ErrorMessages.DUPLICATE_SUBSCRIBER);
    }
    Utils.setRandomIdAndCreatedAt(subscriber);
    subscriber.setSubscribed(true);
    String contentHtml = "Вы подписаны на получение новых статей с сайта <a href=\"https://www.shashki.online\">shashki.online</a>.";
    String contentText = "Вы подписаны на получение новых статей с сайта https://www.shashki.online.";
    String subject = "Подписка на новости сайта";
    subscriberDao.save(subscriber);
    emailService.send(subscriber.getName(), subscriber.getEmail(), subject, contentHtml, contentText);
    return new Subscribed(true);
  }
}
