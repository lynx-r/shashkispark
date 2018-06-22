package com.workingbit.article.service;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.simplejavamail.util.ConfigLoader;

/**
 * Created by Aleksey Popryadukhin on 18/06/2018.
 */
public class EmailService {

  void send(String to) {
    ConfigLoader.loadProperties("simplejavamail.properties", false); // optional default
//    ConfigLoader.loadProperties("overrides.properties"); // optional extra

    Email email = EmailBuilder.startingBlank()
        .to("lollypop", to)
//        .ccWithFixedName("C. Bo group", "chocobo1@candyshop.org", "chocobo2@candyshop.org")
//        .withRecipients("Tasting Group", false, Message.RecipientType.TO,
//            "taster1@cgroup.org;taster2@cgroup.org;tester <>")
//        .bcc("Mr Sweetnose <snose@candyshop.org>")
        .from("Алексей Попрядухин", "lynx.p9@gmail.com")
        .withReplyTo("popcorn", "popcorn@mail.shashki.online")
        .withSubject("hey")
        .withHTMLText("<img src='cid:wink1'><b>We should meet up!</b><img src='cid:wink2'>")
        .withPlainText("Please view this email in a modern email client!")
//        .withEmbeddedImage("wink1", imageByteArray, "image/png")
//        .withEmbeddedImage("wink2", imageDatesource)
//        .withAttachment("invitation", pdfByteArray, "application/pdf")
//        .withAttachment("dresscode", odfDatasource)
        .withHeader("X-SES-CONFIGURATION-SET", "ConfigSet")
//        .withReturnReceiptTo()
        .withDispositionNotificationTo("notify-read-emails@candyshop.com")
        .withBounceTo("tech@candyshop.com")
//        .signWithDomainKey(privateKeyData, "somemail.com", "selector")
        .buildEmail();

    Mailer mailer = MailerBuilder
        .withSMTPServer("email-smtp.eu-west-1.amazonaws.com", 587, "AKIAJX3BFC33YG4S3P5A", "ArI18bAXY95ABHs97fUnGnNPAIfYS47aESWvtEz7VpF+")
        .withTransportStrategy(TransportStrategy.SMTP_TLS)
        .withSessionTimeout(10 * 1000)
//        .clearEmailAddressCriteria() // turns off email validation
//        .withProperty("mail.smtp.sendpartial", true)
        .withDebugLogging(true)
        .buildMailer();

    mailer.sendMail(email);
  }
}
