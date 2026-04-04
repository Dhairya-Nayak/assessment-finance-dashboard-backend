package com.financedashboard.repository;

import com.financedashboard.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndType(String name, Category.CategoryType type);

    List<Category> findAllByIsActiveTrue();

    List<Category> findAllByType(Category.CategoryType type);

    List<Category> findAllByTypeAndIsActiveTrue(Category.CategoryType type);

    @Query("SELECT c FROM Category c WHERE c.isActive = true AND (c.type = :type OR c.type = 'BOTH')")
    List<Category> findActiveCategoriesForType(@Param("type") Category.CategoryType type);

    List<Category> findAllByParentIsNull();

    List<Category> findAllByParentId(Long parentId);

    boolean existsByNameAndType(String name, Category.CategoryType type);

    @Query("SELECT c FROM Category c WHERE c.isSystem = true")
    List<Category> findAllSystemCategories();

    @Query("SELECT c FROM Category c WHERE c.isSystem = false AND c.isActive = true")
    List<Category> findAllCustomCategories();
}
