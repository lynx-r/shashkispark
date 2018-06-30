package com.workingbit.article.service;

import com.workingbit.share.util.EmailUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksey Popryadukhin on 18/06/2018.
 */
public class EmailServiceTest {

  private EmailUtils emailUtils = new EmailUtils();

  @Test
  public void send() throws InterruptedException {
    emailUtils.mailAdmin("test", "test");
    TimeUnit.MINUTES.sleep(1);
  }
}