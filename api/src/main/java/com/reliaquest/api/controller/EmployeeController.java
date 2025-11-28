package com.reliaquest.api.controller;

import com.reliaquest.api.dto.request.CreateEmployeeRequest;
import com.reliaquest.api.dto.response.EmployeeResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.service.EmployeeService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<EmployeeResponse, CreateEmployeeRequest> {

    @Autowired
    private EmployeeService employeeService;

    @Override
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        try {
            List<EmployeeResponse> employeeResponseList = employeeService.getAllEmployees();
            return ResponseEntity.ok(employeeResponseList);
        } catch (EmployeeServiceException e) {
            log.error("Error in getAllEmployees: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByNameSearch(String searchString) {
        try {
            List<EmployeeResponse> employees = employeeService.getEmployeesByNameSearch(searchString);
            return ResponseEntity.ok(employees);
        } catch (EmployeeServiceException e) {
            log.error("Error in getEmployeesByNameSearch: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<EmployeeResponse> getEmployeeById(String id) {
        try {
            EmployeeResponse employee = employeeService.getEmployeeById(id);
            if (employee != null) {
                return ResponseEntity.ok(employee);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EmployeeServiceException e) {
            log.error("Error in getEmployeeById for id: {} | Error Msg: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        try {
            Integer highestSalary = employeeService.getHighestSalaryOfEmployees();
            return ResponseEntity.ok(highestSalary);
        } catch (EmployeeServiceException e) {
            log.error("Error in getHighestSalaryOfEmployees: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        try {
            List<String> employeeNames = employeeService.getTopTenHighestEarningEmployeeNames();
            return ResponseEntity.ok(employeeNames);
        } catch (EmployeeServiceException e) {
            log.error("Error in getTopTenHighestEarningEmployeeNames: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<EmployeeResponse> createEmployee(CreateEmployeeRequest createEmployeeRequest) {
        try {
            EmployeeResponse employee = employeeService.createEmployee(createEmployeeRequest);
            if (employee != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(employee);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EmployeeServiceException e) {
            log.error("Error in createEmployee: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        try {
            String employee = employeeService.deleteEmployeeById(id);
            if (employee != null) {
                return ResponseEntity.ok(employee);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EmployeeServiceException e) {
            log.error("Error in deleteEmployeeById: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
