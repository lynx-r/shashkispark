package com.workingbit.article.service;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 18/06/2018.
 */
public class EmailServiceTest {

  private EmailService emailService = new EmailService();

  @Test
  public void send() {
    emailService.send("alex.86p@yandex.ru");
  }
}