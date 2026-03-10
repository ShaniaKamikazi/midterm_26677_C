package auca.ac.rw.FinanceTracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagDTO {
    private UUID tagId;
    private String name;
    private String color;
    private String description;
    private int transactionCount;
}
