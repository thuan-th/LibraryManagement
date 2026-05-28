package com.library.module.publisher.service.impl;

import com.library.module.publisher.entity.Publisher;
import com.library.module.publisher.repository.PublisherRepository;
import com.library.module.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    private PublisherRepository publisherRepository;

    @Override
    public Publisher savePublisher(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    @Override
    public Boolean existPublisher(String name) {
        return publisherRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Boolean existPublisherByNameAndNotId(String name, int id) {
        return publisherRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public List<Publisher> getAllPublisher() {
        return publisherRepository.findAll();
    }

    @Override
    public Boolean deletePublisher(int id) {
        Publisher publisher = publisherRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(publisher)) {
            publisher.setIsActive(false);
            publisherRepository.save(publisher);
            return true;
        }

        return false;
    }

    @Override
    public Publisher getPublisherById(int id) {
        return publisherRepository.findById(id).orElse(null);
    }

    @Override
    public List<Publisher> getAllActivePublisher() {
        return publisherRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Publisher> getAllPublisherPagination(Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        if (safePageSize > 50) {
            safePageSize = 50;
        }

        Pageable pageable = PageRequest.of(safePageNo, safePageSize);
        return publisherRepository.findAll(pageable);
    }
}
