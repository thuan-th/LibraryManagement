package com.library.module.address.dto;

import lombok.Data;

import java.util.List;

@Data
public class VietnamProvince {

    private String code;

    private String name;

    private List<VietnamWard> wards;
}