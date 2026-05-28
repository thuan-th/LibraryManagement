package com.library.module.category.service.impl;

import com.library.module.category.entity.Category;
import com.library.module.category.repository.CategoryRepository;
import com.library.module.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Boolean existCategory(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Boolean existCategoryByNameAndNotId(String name, int id) {
        return categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public Boolean deleteCategory(int id) {
        Category category = categoryRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(category)) {
            category.setIsActive(false);
            categoryRepository.save(category);
            return true;
        }

        return false;
    }

    @Override
    public Category getCategoryById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public List<Category> getAllActiveCategory() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        if (safePageSize > 50) {
            safePageSize = 50;
        }

        Pageable pageable = PageRequest.of(safePageNo, safePageSize);
        return categoryRepository.findAll(pageable);
    }
}
