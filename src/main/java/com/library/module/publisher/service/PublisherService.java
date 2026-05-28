package com.library.module.publisher.service;

import com.library.module.publisher.entity.Publisher;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PublisherService {

    Publisher savePublisher(Publisher publisher);

    Boolean existPublisher(String name);

    Boolean existPublisherByNameAndNotId(String name, int id);

    List<Publisher> getAllPublisher();

    Boolean deletePublisher(int id);

    Publisher getPublisherById(int id);

    List<Publisher> getAllActivePublisher();

    Page<Publisher> getAllPublisherPagination(Integer pageNo, Integer pageSize);
}
