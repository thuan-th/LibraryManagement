package com.library.module.category.repository;

import com.library.module.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Boolean existsByNameIgnoreCase(String name);

    Boolean existsByNameIgnoreCaseAndIdNot(String name, int id);

    List<Category> findByIsActiveTrue();
}
