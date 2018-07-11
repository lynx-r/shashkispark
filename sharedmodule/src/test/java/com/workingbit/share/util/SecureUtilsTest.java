//package com.workingbit.share.util;
//
//import org.junit.Test;
//
//import java.util.stream.IntStream;
//import java.util.stream.Stream;
//
///**
// * Created by Aleksey Popryadukhin on 16/04/2018.
// */
//public class SecureUtilsTest {
//
//  @Test
//  public void encrypt_test() {
//    IntStream.range(0, 10).parallel().forEach((i)->{
//      System.out.println("Iteration: " + i);
//      String key = Utils.getRandomString(16); // 128 bit key
//      String initVector = Utils.getRandomString(16); // 16 bytes IV
//
//      String text = Utils.getRandomString7();
////      String enc = SecureUtils.encrypt(key, initVector, text);
////      String decr = SecureUtils.decrypt(key, initVector, enc);
////      assertEquals(text, decr);
//    });
//  }
//
//  @Test
//  public void decrypt_test() {
//    String[] test = new String[] {
//        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 q0q5UJdybEEnqhRoHQwYG6swuP2LWJNLJGcLbn9A7D1SUgCusKdLdGODSIaXlNh7/Yjq4QP0v+hKuABiDJLA70MOOmIiBVyyR/qERBh5nzcqeFyQQGBIlV5Zp5DAhZWpn5fkt4BYQdb/ug19upDwLA==",
//        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 3gWcgadnj3crSvC/azmzn4noDrnZ6weoqv8RLGt8VTQsib2dIhm9fEjqOyHoaq4h+nVaotO8W7bekyak+NXjoMBkz6lJZbgkade/tCFRNcQChs+XE9JunX4VdJWH26TgDV7B+1BnDFSKeQ7TuvWS2w==",
//        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 a3NxaNE19TkIztzoU3bssb56hHdGmAFtA7gs2O53LgvqSVHLIBXY/BDAv5vHEU21lGHwdwIEKCHSX3DQhe9tb2DSGFqm+/t/T5jrDkp0hPPw2+T794FFnLypdol5HUDQglsGsNk+KGBIxf+1hcBE1g==",
//        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 5XAneNmRMqeFCUTDbQz27TzdWoqewww1UadYPi/Z8vtMAeE+zEwoU1YVVnqlcSTK/NavlmxY2Fsrx9ibrLKsDATkIH1OTlOXrSQ1vJ5/F9IJqcfFi85nv92JKtHfRxR67MerrcGR5cRc76kFDNPVHA==",
//        "PNaejw7JLhcDdOdK hrwkPzvWF6peMZM7 5XAneNmRMqeFCUTDbQz27TzdWoqewww1UadYPi/Z8vtMAeE+zEwoU1YVVnqlcSTK/NavlmxY2Fsrx9ibrLKsDATkIH1OTlOXrSQ1vJ5/F9IJqcfFi85nv92JKtHfRxR67MerrcGR5cRc76kFDNPVHA==",
//        "m5nQi1CQKcZ8kdBQ puzT1X02n5Nvp931 +SpPoLH9uGnHeVmDJBGEmhSoz3w+JV6EMp2U2UbXtMBiMEcpYdwQBGSUgjwvNsDZTmSoDVYh4+BzGnMuyApTjd2CyJ8nqEzsi7ehWa3fqTjzJgvsrDpOdmwxv6jGIYHwIgYnubiyRmdXf5hmaj2qLA==",
//        "Y5S2rKNJisoNO1JC X40Ahi6jaUsi9X8n DT8JzmBiBdhDqlin94IUxyfEb0ib+tspYdQ+E6RaigC4T4Bvd2DPIsO5Zj88GU1fZBiwNztfmqeARhcwhWiCtxnXGj4FXlgj3eklTPKgAJZNG9T8ElcwAELLxAHo4dwTBU9DPnZDBiemLV35ijj3BQ=="
//    };
//    Stream.of(test).parallel().forEach((i)->{
//      System.out.println("Iteration: " + i);
//      String[] val = i.split(" ");
//      String key = val[0]; // 128 bit key
//      String initVector = val[1]; // 16 bytes IV
//
//      String text = val[2];
////      String enc = SecureUtils.decrypt(key, initVector, text);
////      String decr = Encryptor.decrypt(key, initVector, enc);
////      assertEquals(text, enc);
//    });
//  }
//
//  @Test
//  public void digest_test() {
//  }
//}
