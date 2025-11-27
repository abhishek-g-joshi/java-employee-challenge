package com.reliaquest.api.service.Impl;

import com.reliaquest.api.dto.request.CreateEmployeeRequest;
import com.reliaquest.api.dto.request.DeleteEmployeeRequest;
import com.reliaquest.api.dto.response.ApiResponse;
import com.reliaquest.api.dto.response.EmployeeResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.service.EmployeeService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.*;

@Slf4j
@Service("EmployeeServiceImpl")
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    @Value("${mock.api.base-url}")
    private String mockApiBaseUrl;

    private final RestTemplate restTemplate;

    // get list of all employees
    @Override
    public List<EmployeeResponse> getAllEmployees() {
        try {
            ResponseEntity<ApiResponse<EmployeeResponse[]>> response = restTemplate.exchange(
                    mockApiBaseUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<EmployeeResponse[]>>() {});
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getData() != null) {
                return Arrays.asList(response.getBody().getData());
            }
            return Collections.emptyList();
        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException ex) {
            handleHttpException("fetching all employees", ex);
            return Collections.emptyList();
        }
    }

    // all employees whose name contains or matches the input string
    @Override
    public List<EmployeeResponse> getEmployeesByNameSearch(String searchString) {
        if (!StringUtils.hasText(searchString)) {
            return Collections.emptyList();
        }
        List<EmployeeResponse> allEmployees = getAllEmployees();
        return allEmployees.stream()
                .filter(employee -> employee.getName() != null
                        && employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    // get a single employee by id
    @Override
    public EmployeeResponse getEmployeeById(String id) {
        try {
            String url = mockApiBaseUrl + "/" + id;
            ResponseEntity<ApiResponse<EmployeeResponse>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<EmployeeResponse>>() {});

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            return null;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            handleHttpException("fetching employee by id", ex);
            return null;
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            handleHttpException("fetching employee by id", ex);
            return null;
        }
    }

    // highest salary of amongst all employees
    @Override
    public Integer getHighestSalaryOfEmployees() {
        List<EmployeeResponse> allEmployees = getAllEmployees();

        return allEmployees.stream()
                .map(EmployeeResponse::getSalary)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }

    // list of the top 10 employees based on salary
    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<EmployeeResponse> allEmployees = getAllEmployees();

        return allEmployees.stream()
                .filter(employee -> employee.getName() != null && employee.getSalary() != null)
                .sorted(Comparator.comparing(EmployeeResponse::getSalary).reversed())
                .limit(10)
                .map(EmployeeResponse::getName)
                .collect(Collectors.toList());
    }

    // create a employee
    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest createEmployeeRequest) {
        try {
            HttpEntity<CreateEmployeeRequest> requestEntity = new HttpEntity<>(createEmployeeRequest);
            ResponseEntity<ApiResponse<EmployeeResponse>> response = restTemplate.exchange(
                    mockApiBaseUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<ApiResponse<EmployeeResponse>>() {});
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException ex) {
            handleHttpException("creating employee", ex);
            return null;
        }
    }

    // delete a employee by id
    @Override
    public String deleteEmployeeById(String id) {
        try {
            EmployeeResponse employee = getEmployeeById(id);
            if (employee == null || employee.getName() == null) {
                return null;
            }

            DeleteEmployeeRequest deleteRequest =
                    DeleteEmployeeRequest.builder().name(employee.getName()).build();
            HttpEntity<DeleteEmployeeRequest> request = new HttpEntity<>(deleteRequest);
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    mockApiBaseUrl,
                    HttpMethod.DELETE,
                    request,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {});
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().getData())) {
                return employee.getName();
            }
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException ex) {
            handleHttpException("deleting employee", ex);
            return null;
        }
    }

    private void handleHttpException(String operation, Exception ex) {
        if (ex instanceof HttpClientErrorException httpException) {
            if (httpException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("Rate limit exceeded while {}", operation);
                throw new EmployeeServiceException("Rate limit exceeded. Please try after some time..", ex);
            }
            log.error("Error in {}: {}", operation, ex.getMessage());
            throw new EmployeeServiceException("Failed to " + operation, ex);
        } else if (ex instanceof HttpServerErrorException) {
            log.error("Error in {}: {}", operation, ex.getMessage());
            throw new EmployeeServiceException("Failed to " + operation, ex);
        } else {
            log.error("Connection error while {}: {}", operation, ex.getMessage());
            throw new EmployeeServiceException("Unable to connect to mock API", ex);
        }
    }
}
