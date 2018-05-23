package com.workingbit.security.service;

import com.workingbit.share.exception.CryptoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.SecureAuth;
import com.workingbit.share.util.SecureUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static com.workingbit.security.SecurityEmbedded.appProperties;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryadukhin on 23/05/2018.
 */
public class LoggedInService {

  public void registerUser(SecureAuth secureAuth) throws CryptoException, IOException {
    String key = appProperties.passwordFileKey();
    URL registeredUsersPath = getClass().getResource("/RegisteredUsers/ShashkiUsers.pwd");
    File encrypted = new File(registeredUsersPath.getFile());
    File decryptedFile = File.createTempFile("shashkitemp", "pwd");
    SecureUtils.decrypt(key, encrypted, decryptedFile);
    String json = dataToJson(secureAuth) + "\n";
    byte[] data = json.getBytes();
    FileUtils.writeByteArrayToFile(decryptedFile, data, true);
    SecureUtils.encrypt(key, decryptedFile, encrypted);
  }

  public SecureAuth findByUsername(String username) throws CryptoException, IOException {
    String key = appProperties.passwordFileKey();
    URL registeredUsersPath = getClass().getResource("/RegisteredUsers/ShashkiUsers.pwd");
    File encrypted = new File(registeredUsersPath.getFile());
    File decryptedFile = File.createTempFile("shashkitemp", "pwd");
    SecureUtils.decrypt(key, encrypted, decryptedFile);
    List<String> lines = FileUtils.readLines(decryptedFile);
    for (String line : lines) {
      SecureAuth secureAuth = jsonToData(line, SecureAuth.class);
      if (secureAuth.getUsername().equals(username)) {
        return secureAuth;
      }
    }
    return null;
  }

  public void replaceSecureAuth(SecureAuth secureAuth, SecureAuth secureAuthUpdated) throws CryptoException, IOException {
    String key = appProperties.passwordFileKey();
    URL registeredUsersPath = getClass().getResource("/RegisteredUsers/ShashkiUsers.pwd");
    File encrypted = new File(registeredUsersPath.getFile());
    File decryptedFile = File.createTempFile("shashkitemp", "pwd");
    SecureUtils.decrypt(key, encrypted, decryptedFile);
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
    SecureUtils.encrypt(key, decryptedFile, encrypted);
  }
}
