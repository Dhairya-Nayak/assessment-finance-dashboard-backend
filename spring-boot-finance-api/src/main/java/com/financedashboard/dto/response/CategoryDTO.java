package com.financedashboard.dto.response;

import com.financedashboard.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;
    private String name;
    private String description;
    private Category.CategoryType type;
    private String color;
    private String icon;
    private Boolean isActive;
    private Boolean isSystem;
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryDTO fromEntity(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .type(category.getType())
                .color(category.getColor())
                .icon(category.getIcon())
                .isActive(category.getIsActive())
                .isSystem(category.getIsSystem())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public static CategoryDTO fromEntityWithChildren(Category category) {
        CategoryDTO dto = fromEntity(category);
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .filter(Category::getIsActive)
                    .map(CategoryDTO::fromEntityWithChildren)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
