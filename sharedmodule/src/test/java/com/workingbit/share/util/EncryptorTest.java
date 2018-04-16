package com.workingbit.share.util;

import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class EncryptorTest {

  @Test
  public void encrypt_test() {
    String[] test = new String[] {
        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 q0q5UJdybEEnqhRoHQwYG6swuP2LWJNLJGcLbn9A7D1SUgCusKdLdGODSIaXlNh7/Yjq4QP0v+hKuABiDJLA70MOOmIiBVyyR/qERBh5nzcqeFyQQGBIlV5Zp5DAhZWpn5fkt4BYQdb/ug19upDwLA==",
        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 3gWcgadnj3crSvC/azmzn4noDrnZ6weoqv8RLGt8VTQsib2dIhm9fEjqOyHoaq4h+nVaotO8W7bekyak+NXjoMBkz6lJZbgkade/tCFRNcQChs+XE9JunX4VdJWH26TgDV7B+1BnDFSKeQ7TuvWS2w==",
        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 a3NxaNE19TkIztzoU3bssb56hHdGmAFtA7gs2O53LgvqSVHLIBXY/BDAv5vHEU21lGHwdwIEKCHSX3DQhe9tb2DSGFqm+/t/T5jrDkp0hPPw2+T794FFnLypdol5HUDQglsGsNk+KGBIxf+1hcBE1g==",
        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 5XAneNmRMqeFCUTDbQz27TzdWoqewww1UadYPi/Z8vtMAeE+zEwoU1YVVnqlcSTK/NavlmxY2Fsrx9ibrLKsDATkIH1OTlOXrSQ1vJ5/F9IJqcfFi85nv92JKtHfRxR67MerrcGR5cRc76kFDNPVHA==",
        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 5XAneNmRMqeFCUTDbQz27TzdWoqewww1UadYPi/Z8vtMAeE+zEwoU1YVVnqlcSTK/NavlmxY2Fsrx9ibrLKsDATkIH1OTlOXrSQ1vJ5/F9IJqcfFi85nv92JKtHfRxR67MerrcGR5cRc76kFDNPVHA=="
    };
    Stream.of(test).parallel().forEach((i)->{
      System.out.println("Iteration: " + i);
      String[] val = i.split(" ");
      String key = val[0]; // 128 bit key
      String initVector = val[1]; // 16 bytes IV

      String text = val[2];
      String enc = Encryptor.encrypt(key, initVector, text);
      String decr = Encryptor.decrypt(key, initVector, enc);
      assertEquals(text, decr);
    });
  }

  @Test
  public void decrypt_test() {
  }

  @Test
  public void digest_test() {
  }
}
