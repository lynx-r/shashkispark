package com.workingbit.security.service;

import com.workingbit.share.exception.CryptoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.SecureAuth;
import com.workingbit.share.util.SecureUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.workingbit.security.SecurityEmbedded.appProperties;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryadukhin on 23/05/2018.
 */
public class LoggedInService {

  void registerUser(SecureAuth secureAuth) throws CryptoException, IOException {
    File decryptedFile = decryptFile();
    String json = dataToJson(secureAuth) + "\n";
    byte[] data = json.getBytes();
    FileUtils.writeByteArrayToFile(decryptedFile, data, true);
    encryptFile(decryptedFile);
  }

  @Nullable SecureAuth findByUsername(String username) throws CryptoException, IOException {
    File decryptedFile = decryptFile();
    List<String> lines = FileUtils.readLines(decryptedFile);
    for (String line : lines) {
      SecureAuth secureAuth = jsonToData(line, SecureAuth.class);
      if (secureAuth.getUsername().equals(username)) {
        return secureAuth;
      }
    }
    return null;
  }

  void replaceSecureAuth(@NotNull SecureAuth secureAuth, SecureAuth secureAuthUpdated) throws CryptoException, IOException {
    File decryptedFile = decryptFile();
    List<String> lines = FileUtils.readLines(decryptedFile);
    int indexReplace = -1;
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      SecureAuth current = jsonToData(line, SecureAuth.class);
      if (secureAuth.getUsername().equals(current.getUsername())) {
        indexReplace = i;
        break;
      }
    }
    if (indexReplace == -1) {
      throw RequestException.notFound404();
    }
    lines.remove(indexReplace);
    lines.add(indexReplace, dataToJson(secureAuthUpdated));
    String data = lines.stream().collect(Collectors.joining("\n"));
    FileUtils.writeByteArrayToFile(decryptedFile, data.getBytes());
    encryptFile(decryptedFile);
  }

  private void encryptFile(@NotNull File decryptedFile) throws CryptoException, IOException {
    String key = appProperties.passwordFileKey();
    String passwdFilename = appProperties.passwordFilename();
    File encrypted = getEncryptedFile(passwdFilename);
    SecureUtils.encrypt(key, decryptedFile, encrypted);
  }

  @NotNull
  private File decryptFile() throws CryptoException, IOException {
    String key = appProperties.passwordFileKey();
    String passwdFilename = appProperties.passwordFilename();
    File encrypted = getEncryptedFile(passwdFilename);
    File decryptedFile = File.createTempFile("shashkitemp", "pwd");
    SecureUtils.decrypt(key, encrypted, decryptedFile);
    return decryptedFile;
  }

  @NotNull
  private File getEncryptedFile(@NotNull String passwdFilename) throws IOException {
    File encrypted = new File(passwdFilename);
    if (!encrypted.exists()) {
      boolean newFile = encrypted.createNewFile();
      if (!newFile) {
        throw new SecurityException("Не удалось создать файл паролей");
      }
    }
    return encrypted;
  }
}
