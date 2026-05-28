package com.library.module.category.service;

import com.library.module.category.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    Category saveCategory(Category category);

    Boolean existCategory(String name);

    Boolean existCategoryByNameAndNotId(String name, int id);

    List<Category> getAllCategory();

    Boolean deleteCategory(int id);

    Category getCategoryById(int id);

    List<Category> getAllActiveCategory();

    Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize);
}
