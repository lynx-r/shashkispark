package com.workingbit.share.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by Aleksey Popryadukhin on 25/05/2018.
 */
public class NotationDrivesSerializer extends JsonSerializer<NotationDrives> {

  @Override
  public void serialize(@NotNull NotationDrives value, @NotNull JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeStartArray();
    for (NotationDrive notationDrive : value) {
      gen.writeObject(notationDrive);
    }
    gen.writeEndArray();
  }
}
