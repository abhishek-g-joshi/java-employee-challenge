package com.reliaquest.api.service;

import com.reliaquest.api.dto.request.CreateEmployeeRequest;
import com.reliaquest.api.dto.response.EmployeeResponse;
import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> getAllEmployees();

    List<EmployeeResponse> getEmployeesByNameSearch(String searchString);

    EmployeeResponse getEmployeeById(String id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    EmployeeResponse createEmployee(CreateEmployeeRequest createEmployeeRequest);

    String deleteEmployeeById(String id);
}
