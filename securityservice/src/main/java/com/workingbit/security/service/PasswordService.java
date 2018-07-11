//package com.workingbit.security.service;
//
//import com.workingbit.share.exception.CryptoException;
//import com.workingbit.share.exception.RequestException;
//import com.workingbit.share.model.SecureAuth;
//import com.workingbit.share.util.SecureUtils;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.jetbrains.annotations.NotNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import static com.workingbit.security.SecurityEmbedded.appProperties;
//import static com.workingbit.share.util.JsonUtils.dataToJson;
//import static com.workingbit.share.util.JsonUtils.jsonToData;
//
///**
// * Created by Aleksey Popryadukhin on 23/05/2018.
// */
//public class PasswordService {
//
//  private Logger logger = LoggerFactory.getLogger(PasswordService.class);
//
//  void registerUser(SecureAuth secureAuth) {
//    ByteArrayInputStream decryptedFile = decryptFile();
//    String json = dataToJson(secureAuth) + "\n";
//    byte[] data = json.getBytes();
//    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//      IOUtils.copy(decryptedFile, out);
//      out.write(data);
//      InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
//      InputStream in = encryptFile(inputStream);
//      Files.write(Paths.get(appProperties.passwordFilename()), in.readAllBytes(), StandardOpenOption.WRITE);
//    } catch (Exception e) {
//      logger.error("Error: " + e.getMessage(), e);
//      throw RequestException.internalServerError();
//    }
//  }
//
//  Optional<SecureAuth> findByEmail(String email) {
//    ByteArrayInputStream decryptedFile = decryptFile();
//    BufferedReader buffer = new BufferedReader(new InputStreamReader(decryptedFile));
//    List<SecureAuth> users = buffer.lines()
//        .map(s -> jsonToData(s, SecureAuth.class))
//        .filter(s -> s.getEmail().equals(email))
//        .collect(Collectors.toList());
//    if (users.size() == 1) {
//      return Optional.of(users.get(0));
//    }
//    logger.error("Error: NOT UNIQUE USER IN DB!");
//    throw RequestException.internalServerError();
//  }
//
//  void save(SecureAuth secureAuth) {
//    ByteArrayInputStream decryptedFile = decryptFile();
//    BufferedReader buffer = new BufferedReader(new InputStreamReader(decryptedFile));
//    String data = buffer.lines()
//        .map(s -> {
//          SecureAuth current = jsonToData(s, SecureAuth.class);
//          if (secureAuth.getEmail().equals(current.getEmail())) {
//            return dataToJson(secureAuth);
//          }
//          return s;
//        })
//        .collect(Collectors.joining("\n"));
//    ByteArrayInputStream in = encryptFile(new ByteArrayInputStream(data.getBytes()));
//    try {
//      Files.write(Paths.get(appProperties.passwordFilename()), in.readAllBytes(), StandardOpenOption.WRITE);
//    } catch (IOException e) {
//      logger.error("Error: " + e.getMessage(), e);
//      throw RequestException.internalServerError();
//    }
//  }
//
//  private ByteArrayInputStream encryptFile(@NotNull InputStream decryptedStream) {
//    String key = appProperties.passwordFileKey();
//    try {
//      return SecureUtils.encrypt(key, decryptedStream);
//    } catch (CryptoException e) {
//      logger.error("Error: " + e.getMessage(), e);
//      throw RequestException.internalServerError();
//    }
//  }
//
//  @NotNull
//  private ByteArrayInputStream decryptFile() {
//    String key = appProperties.passwordFileKey();
//    String passwdFilename = appProperties.passwordFilename();
//    try {
//      FileInputStream encrypted = getEncryptedFile(passwdFilename);
//      return SecureUtils.decrypt(key, encrypted);
//    } catch (CryptoException e) {
//      logger.error("Error: Unable to decrypt file", e);
//      throw RequestException.internalServerError();
//    }
//  }
//
//  @NotNull
//  private FileInputStream getEncryptedFile(@NotNull String passwdFilename) {
//    File encrypted = new File(passwdFilename);
//    if (!encrypted.exists()) {
//      boolean newFile;
//      try {
//        newFile = encrypted.createNewFile();
//      } catch (IOException e) {
//        logger.error("Unable to create file: " + passwdFilename, e);
//        throw RequestException.internalServerError();
//      }
//      if (!newFile) {
//        throw RequestException.internalServerError();
//      }
//    }
//    try {
//      return FileUtils.openInputStream(encrypted);
//    } catch (IOException e) {
//      logger.error("Error: Unable to open stream", e);
//      throw RequestException.internalServerError();
//    }
//  }
//}
