package com.workingbit.share.util;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.simplejavamail.util.ConfigLoader;

import static com.workingbit.share.common.SharedProperties.sharedProperties;

/**
 * Created by Aleksey Popryadukhin on 18/06/2018.
 */
public class EmailUtils {

  public void mail(String name, String to, String subject, String contentHtml, String contentText) {
    new Thread(() -> {
      if (sharedProperties.test()) {
        return;
      }
      ConfigLoader.loadProperties("simplejavamail.properties", false); // optional default
//    ConfigLoader.loadProperties("overrides.properties"); // optional extra

      Email email = EmailBuilder.startingBlank()
          .to(name, to)
//        .ccWithFixedName("C. Bo group", "chocobo1@candyshop.org", "chocobo2@candyshop.org")
//        .withRecipients("Tasting Group", false, Message.RecipientType.TO,
//            "taster1@cgroup.org;taster2@cgroup.org;tester <>")
//        .bcc("Mr Sweetnose <snose@candyshop.org>")
          .from("Шашки онлайн", "popcorn@shashki.online")
          .withReplyTo("Шашки онлайн", "popcorn@shashki.online")
          .withSubject(subject)
          .withHTMLText(contentHtml)
          .withPlainText(contentText)
//        .withEmbeddedImage("wink1", imageByteArray, "image/png")
//        .withEmbeddedImage("wink2", imageDatesource)
//        .withAttachment("invitation", pdfByteArray, "application/pdf")
//        .withAttachment("dresscode", odfDatasource)
//        .withHeader("X-SES-CONFIGURATION-SET", "ConfigSet")
//        .withReturnReceiptTo()
//        .withDispositionNotificationTo("notify-read-emails@candyshop.com")
//        .withBounceTo("admin@mail.shashki.online")
//        .signWithDomainKey(privateKeyData, "somemail.com", "selector")
          .buildEmail();

      Mailer mailer = MailerBuilder
          .withSMTPServer("email-smtp.eu-west-1.amazonaws.com", 587, "AKIAJVJX4JLQJFJRMMEQ", "Aq+LFAk+yUOJLRWWg454p1RNvd+/Znq/cRL9eZSrM41g")
          .withTransportStrategy(TransportStrategy.SMTP_TLS)
          .withSessionTimeout(10 * 1000)
//        .clearEmailAddressCriteria() // turns off email validation
//        .withProperty("mail.smtp.sendpartial", true)
          .withDebugLogging(true)
          .buildMailer();

      mailer.sendMail(email);
    }).start();
  }

  public void mailAdmin(String subject, String contentHtml) {
    mail("Admin", sharedProperties.adminMail(), subject, contentHtml, contentHtml);
  }
}
