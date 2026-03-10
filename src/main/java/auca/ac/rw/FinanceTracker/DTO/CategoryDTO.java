package auca.ac.rw.FinanceTracker.DTO;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private UUID categoryId;
    private String name;
    private String description;
    private String color;
    private String categoryType;
    private UUID parentCategoryId;
    private String parentCategoryName;
    private List<CategoryDTO> subcategories;
}

