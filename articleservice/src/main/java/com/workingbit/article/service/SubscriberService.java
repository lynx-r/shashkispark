package com.workingbit.article.service;

import com.workingbit.share.domain.impl.Subscriber;
import com.workingbit.share.model.Subscribed;
import com.workingbit.share.util.Utils;

import static com.workingbit.article.ArticleEmbedded.emailService;
import static com.workingbit.article.ArticleEmbedded.subscriberDao;

/**
 * Created by Aleksey Popryadukhin on 18/06/2018.
 */
public class SubscriberService {

  Subscribed subscribe(Subscriber subscriber) {
    Utils.setRandomIdAndCreatedAt(subscriber);
    subscriber.setSubscribed(true);
    subscriberDao.save(subscriber);
    emailService.send("alex.86p@yandex.ru");
    return new Subscribed(true);
  }
}
