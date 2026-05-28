package com.library.module.publisher.repository;

import com.library.module.publisher.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {

    Boolean existsByNameIgnoreCase(String name);

    Boolean existsByNameIgnoreCaseAndIdNot(String name, int id);

    List<Publisher> findByIsActiveTrue();
}
