package com.financedashboard.service;

import com.financedashboard.dto.request.CategoryRequest;
import com.financedashboard.dto.response.CategoryDTO;
import com.financedashboard.entity.Category;
import com.financedashboard.exception.BadRequestException;
import com.financedashboard.exception.DuplicateResourceException;
import com.financedashboard.exception.ResourceNotFoundException;
import com.financedashboard.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#id")
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryDTO.fromEntity(category);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByIsActiveTrue().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'type-' + #type")
    public List<CategoryDTO> getCategoriesByType(Category.CategoryType type) {
        return categoryRepository.findAllByTypeAndIsActiveTrue(type).stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveCategoriesForRecordType(Category.CategoryType type) {
        return categoryRepository.findActiveCategoriesForType(type).stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryHierarchy() {
        return categoryRepository.findAllByParentIsNull().stream()
                .filter(Category::getIsActive)
                .map(CategoryDTO::fromEntityWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDTO createCategory(CategoryRequest request, Long createdBy) {
        // Check for duplicate
        if (categoryRepository.existsByNameAndType(request.getName(), request.getType())) {
            throw new DuplicateResourceException("Category", "name and type", 
                    request.getName() + " - " + request.getType());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .color(request.getColor() != null ? request.getColor() : "#6B7280")
                .icon(request.getIcon())
                .isActive(true)
                .isSystem(false)
                .createdBy(createdBy)
                .build();

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        log.info("Category created: {} by user {}", category.getName(), createdBy);

        return CategoryDTO.fromEntity(category);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDTO updateCategory(Long id, CategoryRequest request, Long modifiedBy) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Don't allow modifying system categories
        if (category.getIsSystem()) {
            throw new BadRequestException("Cannot modify system categories");
        }

        // Check for duplicate if name or type changed
        if (!category.getName().equals(request.getName()) || !category.getType().equals(request.getType())) {
            if (categoryRepository.existsByNameAndType(request.getName(), request.getType())) {
                throw new DuplicateResourceException("Category", "name and type",
                        request.getName() + " - " + request.getType());
            }
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setType(request.getType());
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        category.setIcon(request.getIcon());
        category.setLastModifiedBy(modifiedBy);

        // Update parent if provided
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryRepository.save(category);
        log.info("Category updated: {} by user {}", category.getName(), modifiedBy);

        return CategoryDTO.fromEntity(category);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id, Long modifiedBy) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Don't allow deleting system categories
        if (category.getIsSystem()) {
            throw new BadRequestException("Cannot delete system categories");
        }

        // Check if category has financial records
        if (!category.getFinancialRecords().isEmpty()) {
            throw new BadRequestException("Cannot delete category with existing financial records. Please reassign or delete the records first.");
        }

        // Soft delete
        category.setIsActive(false);
        category.setLastModifiedBy(modifiedBy);
        categoryRepository.save(category);
        log.info("Category deactivated: {} by user {}", category.getName(), modifiedBy);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getSystemCategories() {
        return categoryRepository.findAllSystemCategories().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCustomCategories() {
        return categoryRepository.findAllCustomCategories().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
