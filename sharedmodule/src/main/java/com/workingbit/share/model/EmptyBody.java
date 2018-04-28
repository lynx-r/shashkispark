package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
@JsonTypeName("EmptyBody")
@AllArgsConstructor
@Data
public class EmptyBody implements Payload {
}
