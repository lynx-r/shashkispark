package com.workingbit.share.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecureUtils {

  public static String encrypt(String key, String initVector, String value) {
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
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
        | InvalidKeyException | NoSuchPaddingException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String digest(String data) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return new String(digest.digest(data.getBytes(StandardCharsets.UTF_8)));
  }
}