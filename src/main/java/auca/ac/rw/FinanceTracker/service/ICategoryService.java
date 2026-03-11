package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.CategoryDTO;
import auca.ac.rw.FinanceTracker.DTO.CategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {

    CategoryDTO createCategory(UUID userId, CategoryRequest request);

    CategoryDTO getCategoryById(UUID categoryId, UUID userId);

    List<CategoryDTO> getCategoriesByUser(UUID userId);

    Page<CategoryDTO> getCategoriesByUserPaginated(UUID userId, Pageable pageable);

    CategoryDTO updateCategory(UUID categoryId, UUID userId, CategoryRequest request);

    void deleteCategory(UUID categoryId, UUID userId);

    CategoryDTO restoreCategory(UUID categoryId, UUID userId);
}
