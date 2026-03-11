package auca.ac.rw.FinanceTracker.service;

import auca.ac.rw.FinanceTracker.DTO.CategoryDTO;
import auca.ac.rw.FinanceTracker.DTO.CategoryRequest;
import auca.ac.rw.FinanceTracker.enums.CategoryType;
import auca.ac.rw.FinanceTracker.exception.DuplicateResourceException;
import auca.ac.rw.FinanceTracker.exception.ResourceNotFoundException;
import auca.ac.rw.FinanceTracker.exception.UnauthorizedException;
import auca.ac.rw.FinanceTracker.model.Category;
import auca.ac.rw.FinanceTracker.model.User;
import auca.ac.rw.FinanceTracker.repository.ICategoryRepository;
import auca.ac.rw.FinanceTracker.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final ICategoryRepository categoryRepository;
    private final IUserRepository userRepository;

    public CategoryService(ICategoryRepository categoryRepository, IUserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(UUID userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (categoryRepository.existsByNameAndUserUserIdAndDeletedFalse(request.getName(), userId)) {
            throw new DuplicateResourceException("Category already exists for this user");
        }

        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        category.setCategoryType(request.getCategoryType() != null
                ? CategoryType.valueOf(request.getCategoryType().toUpperCase())
                : CategoryType.BOTH);

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findByCategoryIdAndDeletedFalse(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            if (!parent.getUser().getUserId().equals(userId)) {
                throw new UnauthorizedException("You do not have access to the parent category");
            }
            category.setParentCategory(parent);
        }

        category = categoryRepository.save(category);
        log.info("Category created for user: {}", userId);
        return toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findByCategoryIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        verifyOwnership(category, userId);
        return toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByUser(UUID userId) {
        return categoryRepository.findByUserUserIdAndDeletedFalse(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getCategoriesByUserPaginated(UUID userId, Pageable pageable) {
        return categoryRepository.findByUserUserIdAndDeletedFalse(userId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(UUID categoryId, UUID userId, CategoryRequest request) {
        Category category = categoryRepository.findByCategoryIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        verifyOwnership(category, userId);

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        if (request.getCategoryType() != null) {
            category.setCategoryType(CategoryType.valueOf(request.getCategoryType().toUpperCase()));
        }

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findByCategoryIdAndDeletedFalse(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            if (!parent.getUser().getUserId().equals(userId)) {
                throw new UnauthorizedException("You do not have access to the parent category");
            }
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        category = categoryRepository.save(category);
        log.info("Category updated: {}", categoryId);
        return toDTO(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findByCategoryIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        verifyOwnership(category, userId);

        category.softDelete();
        categoryRepository.save(category);
        log.info("Category soft-deleted: {}", categoryId);
    }

    @Override
    @Transactional
    public CategoryDTO restoreCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findByCategoryIdAndDeletedTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted category not found"));
        verifyOwnership(category, userId);

        category.restore();
        category = categoryRepository.save(category);
        log.info("Category restored: {}", categoryId);
        return toDTO(category);
    }

    private void verifyOwnership(Category category, UUID userId) {
        if (!category.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this category");
        }
    }

    private CategoryDTO toDTO(Category category) {
        List<CategoryDTO> subcategoryDTOs = category.getSubcategories() != null
                ? category.getSubcategories().stream()
                        .filter(c -> !c.isDeleted())
                        .map(c -> new CategoryDTO(
                                c.getCategoryId(),
                                c.getName(),
                                c.getDescription(),
                                c.getColor(),
                                c.getCategoryType().name(),
                                category.getCategoryId(),
                                category.getName(),
                                Collections.emptyList()
                        ))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return new CategoryDTO(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getColor(),
                category.getCategoryType().name(),
                category.getParentCategory() != null ? category.getParentCategory().getCategoryId() : null,
                category.getParentCategory() != null ? category.getParentCategory().getName() : null,
                subcategoryDTOs
        );
    }
}