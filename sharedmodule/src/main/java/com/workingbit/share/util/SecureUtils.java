package com.workingbit.share.util;

import com.workingbit.share.exception.CryptoException;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

public class SecureUtils {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES";

  public static String encrypt(@NotNull String key, @NotNull String initVector, @NotNull String value) {
    try {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

      byte[] encrypted = cipher.doFinal(value.getBytes());

      return Base64.encodeBase64String(encrypted);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  public static String decrypt(String key, String initVector, String encrypted)
      throws IllegalBlockSizeException, BadPaddingException {
    try {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
      byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

      return new String(original);
    } catch (@NotNull NoSuchAlgorithmException | InvalidAlgorithmParameterException
        | InvalidKeyException | NoSuchPaddingException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String digest(String data) {
    return sha1Hex(data);
//    try {
//      MessageDigest digest = MessageDigest.getInstance("SHA-256");
//      return new String(digest.digest(data.getBytes(StandardCharsets.UTF_8)));
//    } catch (NoSuchAlgorithmException e) {
//      return "";
//    }
  }

  public static ByteArrayInputStream encrypt(@NotNull String key, @NotNull InputStream inputFile)
      throws CryptoException {
    return doCrypto(Cipher.ENCRYPT_MODE, key, inputFile);
  }

  public static ByteArrayInputStream decrypt(@NotNull String key, @NotNull InputStream inputFile)
      throws CryptoException {
    return doCrypto(Cipher.DECRYPT_MODE, key, inputFile);
  }

  private static ByteArrayInputStream doCrypto(int cipherMode, String key, @NotNull InputStream inputStream) throws CryptoException {
    try {
      Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(cipherMode, secretKey);

      byte[] inputBytes = inputStream.readAllBytes();
      byte[] outputBytes = cipher.doFinal(inputBytes);
      inputStream.close();

      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        outputStream.write(outputBytes);
        return new ByteArrayInputStream(outputStream.toByteArray());
      } catch (Exception e) {
        throw new CryptoException("Error encrypting/decrypting file", e);
      }
    } catch (@NotNull NoSuchPaddingException | NoSuchAlgorithmException
        | InvalidKeyException | BadPaddingException
        | IllegalBlockSizeException | IOException ex) {
      throw new CryptoException("Error encrypting/decrypting file", ex);
    }
  }
}