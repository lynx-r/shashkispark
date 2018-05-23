package com.workingbit.share.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryadukhin on 25/05/2018.
 */
public class NotationDrivesDeserializer extends JsonDeserializer<NotationDrives> {
  @NotNull
  @Override
  public NotationDrives deserialize(@NotNull JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    NotationDrives notationDrives = new NotationDrives();
    TreeNode tn = p.readValueAsTree();
    for (int i = 0; i < tn.size(); i++) {
      TreeNode obj = tn.get(i);
      String ndJson = obj.toString();
      NotationDrive notationDrive = jsonToData(ndJson, NotationDrive.class);
      notationDrives.add(notationDrive);
    }
    return notationDrives;
  }
}
