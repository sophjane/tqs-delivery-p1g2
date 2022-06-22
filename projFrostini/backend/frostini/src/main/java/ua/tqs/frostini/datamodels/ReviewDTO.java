package ua.tqs.frostini.datamodels;


import lombok.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
  Long riderId;
  @PositiveOrZero
  Double points;
}
