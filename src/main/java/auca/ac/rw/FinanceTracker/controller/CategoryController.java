package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.CategoryDTO;
import auca.ac.rw.FinanceTracker.DTO.CategoryRequest;
import auca.ac.rw.FinanceTracker.service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        CategoryDTO category = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        CategoryDTO category = categoryService.getCategoryById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getMyCategories(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<CategoryDTO>>> getMyCategoriesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<CategoryDTO> categories = categoryService.getCategoriesByUserPaginated(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        CategoryDTO category = categoryService.updateCategory(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        categoryService.deleteCategory(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<CategoryDTO>> restoreCategory(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        CategoryDTO category = categoryService.restoreCategory(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Category restored successfully", category));
    }
}
