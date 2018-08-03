package com.workingbit.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.CryptoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.SecureAuth;
import com.workingbit.share.model.SecureAuthList;
import com.workingbit.share.util.JsonUtils;
import com.workingbit.share.util.SecureUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.workingbit.security.SecurityEmbedded.appProperties;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * Created by Aleksey Popryadukhin on 23/05/2018.
 */
public class PasswordService {

  private Logger logger = LoggerFactory.getLogger(PasswordService.class);

  private ObjectMapper objectMapper = JsonUtils.mapper;

  void registerUser(SecureAuth secureAuth) {
    try {
      ByteArrayInputStream decryptedFile = decryptFile();
      SecureAuthList secureAuthList = objectMapper.readValue(decryptedFile, SecureAuthList.class);
      secureAuthList.getUserAdd(secureAuth);
      byte[] bytes = objectMapper.writeValueAsBytes(secureAuthList);
      ByteArrayInputStream encryptFile = encryptFile(new ByteArrayInputStream(bytes));
      Files.write(Paths.get(appProperties.passwordFilename()), encryptFile.readAllBytes(), StandardOpenOption.WRITE);
    } catch (IOException e) {
      logger.error("Error: Unable to register user " + secureAuth.getEmail());
      throw RequestException.internalServerError();
    }
  }

  Optional<SecureAuth> findByEmail(String email) {
    ByteArrayInputStream decryptedFile = decryptFile();
    SecureAuthList secureAuthList;
    try {
      secureAuthList = objectMapper.readValue(decryptedFile, SecureAuthList.class);
    } catch (IOException e) {
      logger.error("Error: Unable to decrypt file");
      throw RequestException.internalServerError();
    }
    List<SecureAuth> users = secureAuthList.getUsers()
        .stream()
        .filter(u -> email.equals(u.getEmail()))
        .collect(Collectors.toList());
    if (users.size() == 1) {
      return Optional.of(users.get(0));
    }
    if (users.isEmpty()) {
      logger.error("Error: User " + email + " not found");
      throw RequestException.forbidden(ErrorMessages.USER_NOT_FOUND);
    }
    logger.error("Error: NOT UNIQUE USER IN DB!");
    throw RequestException.internalServerError();
  }

  void save(SecureAuth secureAuth) {
    ByteArrayInputStream decryptedFile = decryptFile();
    try {
      SecureAuthList secureAuthList = objectMapper.readValue(decryptedFile, SecureAuthList.class);
      SecureAuthList authList = secureAuthList.getUsers()
          .stream()
          .map(u -> {
            if (secureAuth.getEmail().equals(u.getEmail())) {
              return secureAuth;
            }
            return u;
          })
          .collect(collectingAndThen(toCollection(ArrayList::new), SecureAuthList::new));
      String data = objectMapper.writeValueAsString(authList);
      ByteArrayInputStream in = encryptFile(new ByteArrayInputStream(data.getBytes()));
      Files.write(Paths.get(appProperties.passwordFilename()), in.readAllBytes(), StandardOpenOption.WRITE);
    } catch (IOException e) {
      logger.error("Error: Unable to save file");
      throw RequestException.internalServerError();
    }
  }

  private ByteArrayInputStream encryptFile(@NotNull InputStream decryptedStream) {
    String key = appProperties.passwordFileKey();
    try {
      return SecureUtils.encrypt(key, decryptedStream);
    } catch (CryptoException e) {
      logger.error("Error: " + e.getMessage(), e);
      throw RequestException.internalServerError();
    }
  }

  @NotNull
  private ByteArrayInputStream decryptFile() {
    String key = appProperties.passwordFileKey();
    String passwdFilename = appProperties.passwordFilename();
    try {
      File encrypted = new File(passwdFilename);
      if (!encrypted.exists()) {
        boolean newFile;
        try {
          newFile = encrypted.createNewFile();
        } catch (IOException e) {
          logger.error("Unable to create file: " + passwdFilename, e);
          throw RequestException.internalServerError();
        }
        if (!newFile) {
          logger.error("Unable to create file: " + passwdFilename);
          throw RequestException.internalServerError();
        }
        try {
          SecureAuthList secureAuthList = new SecureAuthList();
          String list = objectMapper.writeValueAsString(secureAuthList);
          FileWriter writer = new FileWriter(encrypted);
          IOUtils.write(list, writer);
          writer.close();
          return new ByteArrayInputStream(FileUtils.readFileToByteArray(encrypted));
        } catch (IOException e) {
          logger.error("Unable to write to file: " + passwdFilename, e);
          throw RequestException.internalServerError();
        }
      }
      try {
        return SecureUtils.decrypt(key, FileUtils.openInputStream(encrypted));
      } catch (IOException e) {
        logger.error("Error: Unable to open stream", e);
        throw RequestException.internalServerError();
      }
    } catch (CryptoException e) {
      logger.error("Error: Unable to decrypt file", e);
      throw RequestException.internalServerError();
    }
  }
}
