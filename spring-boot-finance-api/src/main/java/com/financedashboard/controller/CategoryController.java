package com.financedashboard.controller;

import com.financedashboard.dto.request.CategoryRequest;
import com.financedashboard.dto.response.ApiResponse;
import com.financedashboard.dto.response.CategoryDTO;
import com.financedashboard.entity.Category;
import com.financedashboard.security.CurrentUser;
import com.financedashboard.security.UserPrincipal;
import com.financedashboard.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoriesByType(@PathVariable Category.CategoryType type) {
        List<CategoryDTO> categories = categoryService.getCategoriesByType(type);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/for-record/{type}")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoriesForRecordType(
            @PathVariable Category.CategoryType type) {
        List<CategoryDTO> categories = categoryService.getActiveCategoriesForRecordType(type);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoryHierarchy() {
        List<CategoryDTO> categories = categoryService.getCategoryHierarchy();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/system")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getSystemCategories() {
        List<CategoryDTO> categories = categoryService.getSystemCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/custom")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCustomCategories() {
        List<CategoryDTO> categories = categoryService.getCustomCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Creating category: {} by user: {}", request.getName(), currentUser.getUsername());
        CategoryDTO category = categoryService.createCategory(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Updating category: {} by user: {}", id, currentUser.getUsername());
        CategoryDTO category = categoryService.updateCategory(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Deleting category: {} by user: {}", id, currentUser.getUsername());
        categoryService.deleteCategory(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}
