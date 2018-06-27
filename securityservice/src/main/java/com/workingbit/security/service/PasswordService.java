package com.workingbit.security.service;

import com.workingbit.share.exception.CryptoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.SecureAuth;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.workingbit.security.SecurityEmbedded.appProperties;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryadukhin on 23/05/2018.
 */
public class PasswordService {

  private Logger logger = LoggerFactory.getLogger(PasswordService.class);

  void registerUser(SecureAuth secureAuth) throws CryptoException, IOException {
    ByteArrayInputStream decryptedFile = decryptFile();
    String json = dataToJson(secureAuth) + "\n";
    byte[] data = json.getBytes();
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      IOUtils.copy(decryptedFile, out);
      out.write(data);
      InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
      InputStream in = encryptFile(inputStream);
      Files.write(Paths.get(appProperties.passwordFilename()), in.readAllBytes(), StandardOpenOption.WRITE);
    } catch (Exception e) {
      logger.error("Registration error: " + e.getMessage(), e);
    }
  }

  Optional<SecureAuth> findByUsername(String username) throws CryptoException, IOException {
    ByteArrayInputStream decryptedFile = decryptFile();
    BufferedReader buffer = new BufferedReader(new InputStreamReader(decryptedFile));
    return buffer.lines()
        .map(s -> jsonToData(s, SecureAuth.class))
        .filter(s -> s.getUsername().equals(username))
        .findFirst();
  }

  void replaceSecureAuth(@NotNull SecureAuth secureAuth, SecureAuth secureAuthUpdated) throws CryptoException, IOException {
    ByteArrayInputStream decryptedFile = decryptFile();
    BufferedReader buffer = new BufferedReader(new InputStreamReader(decryptedFile));
    String data = buffer.lines()
        .map(s -> {
          SecureAuth current = jsonToData(s, SecureAuth.class);
          if (secureAuth.getUsername().equals(current.getUsername())) {
            DomainId uCFh9p7dspQDfnT1yX5a = new DomainId("uCFh9p7dspQDfnT1yX5a", LocalDateTime.parse("2018-06-22T07:40:04.483721"));
            secureAuthUpdated.setUserId(uCFh9p7dspQDfnT1yX5a);
            System.out.println(dataToJson(secureAuthUpdated));
            return dataToJson(secureAuthUpdated);
          }
          return s;
        })
        .collect(Collectors.joining("\n"));
    ByteArrayInputStream in = encryptFile(new ByteArrayInputStream(data.getBytes()));
    Files.write(Paths.get(appProperties.passwordFilename()), in.readAllBytes(), StandardOpenOption.WRITE);
  }

  private ByteArrayInputStream encryptFile(@NotNull InputStream decryptedStream) throws CryptoException {
    String key = appProperties.passwordFileKey();
    return SecureUtils.encrypt(key, decryptedStream);
  }

  @NotNull
  private ByteArrayInputStream decryptFile() throws CryptoException, IOException {
    String key = appProperties.passwordFileKey();
    String passwdFilename = appProperties.passwordFilename();
    FileInputStream encrypted = getEncryptedFile(passwdFilename);
    return SecureUtils.decrypt(key, encrypted);
  }

  @NotNull
  private FileInputStream getEncryptedFile(@NotNull String passwdFilename) throws IOException {
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
        throw RequestException.internalServerError();
      }
    }
    return FileUtils.openInputStream(encrypted);
  }
}
