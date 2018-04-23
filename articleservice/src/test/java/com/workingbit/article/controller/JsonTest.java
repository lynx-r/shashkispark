package com.workingbit.article.controller;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Aleksey Popryadukhin on 26/03/2018.
 */
public class JsonTest {

  @Test
  public void whenSerializingPolymorphic_thenCorrect()
      throws IOException {
    Dog dog = new Dog(1.0);
    Zoo zoo = new Zoo(dog);

    ObjectMapper mapper = new ObjectMapper();
    String result = mapper
        .writeValueAsString(zoo);
    System.out.println(result);

    Cat cat = new Cat(1.0, "cat");
    zoo = new Zoo(cat);

    mapper = new ObjectMapper();
    String result1 = mapper
        .writeValueAsString(zoo);
    System.out.println(result1);

//    assertThat(result, containsString("type"));
//    assertThat(result, containsString("Animal"));

    Zoo zoo1 = mapper.readValue(result, Zoo.class);
    System.out.println(zoo1);

    zoo1 = mapper.readValue(result1, Zoo.class);
    System.out.println(zoo1);
  }
}

@ToString
class Zoo {
  public Animal animal;

  @JsonCreator
  public Zoo(@JsonProperty("animal") Animal dog) {
    this.animal = dog;
  }

}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Dog.class, name = "dog"),
    @JsonSubTypes.Type(value = Cat.class, name = "cat")
})
class Animal {
}

@ToString
@JsonTypeName("dog")
class Dog extends Animal {
  public double barkVolume;

  Dog(@JsonProperty("barkVolume") double barkVolume) {
    this.barkVolume = barkVolume;
  }
}

@ToString
@JsonTypeName("cat")
class Cat extends Animal {
  public double barkVolume;
  public String name;

  Cat(@JsonProperty("barkVolume") double barkVolume, @JsonProperty("name") String name) {
    this.barkVolume = barkVolume;
    this.name = name;
  }
}