package com.financedashboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_name", columnList = "name"),
        @Index(name = "idx_categories_type", columnList = "type"),
        @Index(name = "idx_categories_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private CategoryType type;

    @Column(name = "color", length = 7)
    @Builder.Default
    private String color = "#6B7280";

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_system")
    @Builder.Default
    private Boolean isSystem = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_modified_by")
    private Long lastModifiedBy;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<FinancialRecord> financialRecords = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CategoryType {
        INCOME,
        EXPENSE,
        BOTH
    }
}
