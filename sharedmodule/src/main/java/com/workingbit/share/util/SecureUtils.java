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

  public static void encrypt(@NotNull String key, @NotNull File inputFile, @NotNull File outputFile)
      throws CryptoException {
    doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
  }

  public static void decrypt(@NotNull String key, @NotNull File inputFile, @NotNull File outputFile)
      throws CryptoException {
    doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
  }

  private static void doCrypto(int cipherMode, String key, @NotNull File inputFile,
                               @NotNull File outputFile) throws CryptoException {
    try {
      Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(cipherMode, secretKey);

      FileInputStream inputStream = new FileInputStream(inputFile);
      byte[] inputBytes = new byte[(int) inputFile.length()];
      inputStream.read(inputBytes);

      byte[] outputBytes = cipher.doFinal(inputBytes);

      FileOutputStream outputStream = new FileOutputStream(outputFile);
      outputStream.write(outputBytes);

      inputStream.close();
      outputStream.close();

    } catch (@NotNull NoSuchPaddingException | NoSuchAlgorithmException
        | InvalidKeyException | BadPaddingException
        | IllegalBlockSizeException | IOException ex) {
      throw new CryptoException("Error encrypting/decrypting file", ex);
    }
  }
}