package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.reliaquest.api.dto.request.CreateEmployeeRequest;
import com.reliaquest.api.dto.response.ApiResponse;
import com.reliaquest.api.dto.response.EmployeeResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.service.Impl.EmployeeServiceImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private static final String API_URL = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(employeeService, "mockApiBaseUrl", API_URL);
    }

    @Test
    void getAllEmployees_returnsListOfEmployees() {
        EmployeeResponse employee1 = createEmployee("Abhishek J", 50000);
        EmployeeResponse employee2 = createEmployee("Ravi Kumar", 60000);
        EmployeeResponse[] employees = {employee1, employee2};

        ApiResponse<EmployeeResponse[]> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);
        ResponseEntity<ApiResponse<EmployeeResponse[]>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(API_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        List<EmployeeResponse> result = employeeService.getAllEmployees();
        assertEquals(2, result.size());
        assertEquals("Abhishek J", result.get(0).getName());
        assertEquals("Ravi Kumar", result.get(1).getName());
    }

    @Test
    void getAllEmployees_throwsException_whenRateLimited() {
        HttpClientErrorException exception =
                new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        when(restTemplate.exchange(eq(API_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);
        assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void getEmployeesByNameSearch_returnsMatchingEmployees() {
        EmployeeResponse[] employees = {
            createEmployee("Abhishek Joshi", 50000),
            createEmployee("Ravi Kumar", 60000),
            createEmployee("Yash Joshi", 70000)
        };

        ApiResponse<EmployeeResponse[]> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);
        ResponseEntity<ApiResponse<EmployeeResponse[]>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(API_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        List<EmployeeResponse> result = employeeService.getEmployeesByNameSearch("Josh");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Abhishek Joshi")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Yash Joshi")));
    }

    @Test
    void getEmployeesByNameSearch_returnsEmptyList_whenNoMatches() {
        EmployeeResponse[] employees = {createEmployee("Abhishek J", 50000), createEmployee("Ravi Kumar", 60000)};

        ApiResponse<EmployeeResponse[]> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);
        ResponseEntity<ApiResponse<EmployeeResponse[]>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(API_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        List<EmployeeResponse> result = employeeService.getEmployeesByNameSearch("Bob");

        assertTrue(result.isEmpty());
    }

    // same result when we pass input as blank(" ")
    @Test
    void getEmployeesByNameSearch_returnsEmptyList() {
        List<EmployeeResponse> result = employeeService.getEmployeesByNameSearch(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeById_returnsEmployee_whenFound() {
        EmployeeResponse employee = createEmployee("Abhishek J", 50000);
        String employeeId = employee.getId().toString();

        ApiResponse<EmployeeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(employee);
        ResponseEntity<ApiResponse<EmployeeResponse>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq(API_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        EmployeeResponse result = employeeService.getEmployeeById(employeeId);
        assertEquals("Abhishek J", result.getName());
        assertEquals(50000, result.getSalary());
    }

    @Test
    void getEmployeeById_returnsNull_whenNotFound() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);
        EmployeeResponse result = employeeService.getEmployeeById("non-exist-id");
        assertNull(result);
    }

    @Test
    void getHighestSalaryOfEmployees_returnsHighestSalary() {
        EmployeeResponse[] employees = {
            createEmployee("Abhishek J", 50000),
            createEmployee("Ravi Kumar", 100000),
            createEmployee("Rohit Pal", 75000)
        };

        ApiResponse<EmployeeResponse[]> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);
        ResponseEntity<ApiResponse<EmployeeResponse[]>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(API_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        Integer result = employeeService.getHighestSalaryOfEmployees();
        assertEquals(100000, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_returnsTop10Names() {
        EmployeeResponse[] employees = new EmployeeResponse[15];
        for (int i = 0; i < 15; i++) {
            employees[i] = createEmployee("Employee " + i, 100000 - (i * 1000));
        }
        ApiResponse<EmployeeResponse[]> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);
        ResponseEntity<ApiResponse<EmployeeResponse[]>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(API_URL), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);
        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();
        assertEquals(10, result.size());
        assertEquals("Employee 0", result.get(0));
        assertEquals("Employee 9", result.get(9));
    }

    @Test
    void createEmployee_returnsCreatedEmployee() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Test Employee")
                .salary(80000)
                .age(30)
                .title("Engineer")
                .build();

        EmployeeResponse createdEmployee = createEmployee("Test Employee", 80000);
        ApiResponse<EmployeeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(createdEmployee);
        ResponseEntity<ApiResponse<EmployeeResponse>> response = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        when(restTemplate.exchange(
                        eq(API_URL), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        EmployeeResponse result = employeeService.createEmployee(request);
        assertEquals("Test Employee", result.getName());
        assertEquals(80000, result.getSalary());
    }

    @Test
    void deleteEmployeeById_returnsEmployeeName_whenDeleted() {
        EmployeeResponse employee = createEmployee("Abhishek J", 50000);
        String employeeId = employee.getId().toString();

        ApiResponse<EmployeeResponse> getResponse = new ApiResponse<>();
        getResponse.setData(employee);
        ResponseEntity<ApiResponse<EmployeeResponse>> getResponseEntity =
                new ResponseEntity<>(getResponse, HttpStatus.OK);
        ApiResponse<Boolean> deleteResponse = new ApiResponse<>();
        deleteResponse.setData(true);
        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = new ResponseEntity<>(deleteResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq(API_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        any(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);

        when(restTemplate.exchange(
                        eq(API_URL),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);
        String result = employeeService.deleteEmployeeById(employeeId);
        assertEquals("Abhishek J", result);
    }

    @Test
    void deleteEmployeeById_returnsNull_whenEmployeeNotFound() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);
        String result = employeeService.deleteEmployeeById("non-exist-id");
        assertNull(result);
    }

    private EmployeeResponse createEmployee(String name, Integer salary) {
        return EmployeeResponse.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .name(name)
                .salary(salary)
                .age(30)
                .title("Developer")
                .email("test@company.com")
                .build();
    }
}
