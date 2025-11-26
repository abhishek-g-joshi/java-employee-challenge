package com.reliaquest.api.service.Impl;

import com.reliaquest.api.dto.request.CreateEmployeeRequest;
import com.reliaquest.api.dto.response.EmployeeResponse;
import com.reliaquest.api.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("EmployeeServiceImpl")
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return List.of();
    }

    @Override
    public List<EmployeeResponse> getEmployeesByNameSearch(String searchString) {
        return List.of();
    }

    @Override
    public EmployeeResponse getEmployeeById(String id) {
        return null;
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        return 0;
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return List.of();
    }

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest input) {
        return null;
    }

    @Override
    public String deleteEmployeeById(String id) {
        return "";
    }
}
