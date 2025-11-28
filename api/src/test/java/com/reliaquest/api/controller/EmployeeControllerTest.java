package com.reliaquest.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.request.CreateEmployeeRequest;
import com.reliaquest.api.dto.response.EmployeeResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllEmployees_returnsListOfEmployees() throws Exception {
        List<EmployeeResponse> employees =
                Arrays.asList(createEmployee("Abhishek J", 50000), createEmployee("Ravi Kumar", 60000));
        when(employeeService.getAllEmployees()).thenReturn(employees);
        mockMvc.perform(get("/api/v1/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employee_name").value("Abhishek J"))
                .andExpect(jsonPath("$[1].employee_name").value("Ravi Kumar"));
    }

    @Test
    void getAllEmployees_returnsEmptyList_whenServiceThrowsException() throws Exception {
        when(employeeService.getAllEmployees()).thenThrow(new EmployeeServiceException("Service error"));
        mockMvc.perform(get("/api/v1/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEmployeesByNameSearch_returnsMatchingEmployees() throws Exception {
        List<EmployeeResponse> employees =
                Arrays.asList(createEmployee("Abhishek Joshi", 50000), createEmployee("Yash Joshi", 60000));
        when(employeeService.getEmployeesByNameSearch("Josh")).thenReturn(employees);
        mockMvc.perform(get("/api/v1/employees/search/Josh").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employee_name").value("Abhishek Joshi"))
                .andExpect(jsonPath("$[1].employee_name").value("Yash Joshi"));
    }

    @Test
    void getEmployeesByNameSearch_returnsEmptyList_whenNoMatches() throws Exception {
        when(employeeService.getEmployeesByNameSearch("rohit")).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/employees/search/rohit").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEmployeeById_returnsEmployee_whenFound() throws Exception {
        EmployeeResponse employee = createEmployee("Abhishek J", 50000);
        String employeeId = employee.getId().toString();

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);
        mockMvc.perform(get("/api/v1/employees/" + employeeId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Abhishek J"))
                .andExpect(jsonPath("$.employee_salary").value(50000));
    }

    @Test
    void getEmployeeById_returnsNotFound_whenEmployeeNotFound() throws Exception {
        when(employeeService.getEmployeeById("non-exist-id")).thenReturn(null);
        mockMvc.perform(get("/api/v1/employees/non-exist-id").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHighestSalaryOfEmployees_returnsHighestSalary() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(100000);
        mockMvc.perform(get("/api/v1/employees/highestSalary").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100000));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_returnsTop10Names() throws Exception {
        List<String> names = Arrays.asList(
                "Employee 1",
                "Employee 2",
                "Employee 3",
                "Employee 4",
                "Employee 5",
                "Employee 6",
                "Employee 7",
                "Employee 8",
                "Employee 9",
                "Employee 10");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(names);
        mockMvc.perform(get("/api/v1/employees/topTenHighestEarningEmployeeNames")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void createEmployee_returnsCreatedEmployee() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Test Employee")
                .salary(80000)
                .age(30)
                .title("Engineer")
                .build();

        EmployeeResponse createdEmployee = createEmployee("Test Employee", 80000);
        when(employeeService.createEmployee(request)).thenReturn(createdEmployee);
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee_name").value("Test Employee"))
                .andExpect(jsonPath("$.employee_salary").value(80000));
    }

    @Test
    void deleteEmployeeById_returnsEmployeeName_whenDeleted() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn("Abhishek J");
        mockMvc.perform(delete("/api/v1/employees/" + employeeId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Abhishek J"));
    }

    @Test
    void deleteEmployeeById_returnNotFound_whenEmployeeNotFound() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(null);
        mockMvc.perform(delete("/api/v1/employees/" + employeeId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
