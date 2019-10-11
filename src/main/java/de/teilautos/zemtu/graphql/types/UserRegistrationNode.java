package de.teilautos.zemtu.graphql.types;

import java.lang.String;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 */
@Data
public class UserRegistrationNode {
  @NotNull
  private String type;

  private String settings;
}
