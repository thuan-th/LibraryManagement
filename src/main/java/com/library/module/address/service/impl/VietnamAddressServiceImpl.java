package com.library.module.address.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.module.address.dto.VietnamProvince;
import com.library.module.address.service.VietnamAddressService;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class VietnamAddressServiceImpl implements VietnamAddressService {

    private final ObjectMapper objectMapper;

    private List<VietnamProvince> provinces = new ArrayList<>();

    public VietnamAddressServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadAddressData() {
        try {
            ClassPathResource resource = new ClassPathResource("static/data/vietnam-addresses.json");

            try (InputStream inputStream = resource.getInputStream()) {
                provinces = objectMapper.readValue(inputStream, new TypeReference<List<VietnamProvince>>() {});
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải dữ liệu địa chỉ Việt Nam", e);
        }
    }

    @Override
    public boolean isValidProvinceAndWard(String provinceName, String wardName) {
        if (!StringUtils.hasText(provinceName) || !StringUtils.hasText(wardName)) {
            return false;
        }

        return provinces.stream()
                .filter(province -> provinceName.equals(province.getName()))
                .anyMatch(province -> province.getWards() != null
                        && province.getWards().stream()
                        .anyMatch(ward -> wardName.equals(ward.getName())));
    }
}