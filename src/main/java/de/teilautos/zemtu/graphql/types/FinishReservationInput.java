package de.teilautos.zemtu.graphql.types;

import java.lang.String;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 */
@Data
public class FinishReservationInput {
  @NotNull
  private String reservationId;

  private String clientMutationId;
}
