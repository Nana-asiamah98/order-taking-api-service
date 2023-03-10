package com.ml.ordermicroservice.restcontrollertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.ordermicroservice.dto.CustomerDTO;
import com.ml.ordermicroservice.dto.OrderDTO;
import com.ml.ordermicroservice.dto.PaginatedCustomerResponse;
import com.ml.ordermicroservice.dto.PaginatedProductResponse;
import com.ml.ordermicroservice.model.Customer;
import com.ml.ordermicroservice.repository.OrderRepository;
import com.ml.ordermicroservice.restcontroller.CustomerRestController;
import com.ml.ordermicroservice.service.CustomerService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class CustomerRestControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should SEARCH A CUSTOMER With A Phone Number AND Return OK When Making A GET Request To Endpoint - /api/v1/customer?phoneNumber=")
    public void fetchCustomerUsingPhoneNumber_andReturnOK() throws Exception {
        Customer mockResponseData = entityResponseData();
        String phoneNumber = "0201111223";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("orderNumber", phoneNumber);

        Mockito.when(customerService.fetchByPhoneNumber(phoneNumber)).thenReturn(java.util.Optional.of(mockResponseData));

        mockMvc.perform(get("/api/v1/customer").params(paramsMap)
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should CREATE a Customer AND Return OK when making a POST Request To Endpoint -  /api/v1/customer")
    public void createACustomer_andReturnOK() throws Exception {
        CustomerDTO mockRequestDTO = entityRequestData();
        Customer mockResponseData = entityResponseData();

        Mockito.when(customerService.save(mockRequestDTO)).thenReturn(mockResponseData);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequestDTO))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data..firstName").value("TestName"));
    }

    @Test
    @DisplayName("Should FAIL TO CREATE a Customer AND Return BAD REQUEST when making a POST Request To Endpoint -  /api/v1/customer")
    public void failToCreateACustomer_andReturnABadRequest() throws Exception {
        CustomerDTO mockWrongRequestDTO = wrongEntityRequestData();
        Customer mockResponseData = entityResponseData();

        Mockito.when(customerService.save(mockWrongRequestDTO)).thenReturn(mockResponseData);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockWrongRequestDTO))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.dataList[0].message").value("Customer first name cannot be empty"))
                .andExpect(jsonPath("$.dataList[0].field").value("customer.firstName"));
    }



    @Test
    @DisplayName("Should  UPDATE a Customer AND Return OK when making a PUT Request To Endpoint -  /{id}/edit")
    public void updateACustomer_andReturnOK() throws Exception {
        UUID customersID = UUID.randomUUID();
        CustomerDTO mockRequestDTO = entityRequestData();
        mockRequestDTO.setId(customersID);
        Customer mockResponseData = entityResponseData();
        mockResponseData.setId(customersID);

        Mockito.when(customerService.update(customersID,mockRequestDTO)).thenReturn(mockResponseData);
        mockMvc.perform(MockMvcRequestBuilders
                .put("/api/v1/customer/{id}/edit", customersID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequestDTO))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.firstName").value("TestName"))
                .andExpect(jsonPath("$.data.email").value("testName@testMail.com"));
    }

    @Test
    @DisplayName("Should FAIL To UPDATE A Customer AND Return BAD REQUEST  when making a PUT Request To Endpoint -  /{id}/edit")
    public void failToUpdateCustomer_andReturnABadRequest() throws Exception {
        UUID customersID = UUID.randomUUID();
        CustomerDTO mockRequestDTO = wrongEntityRequestData();
        mockRequestDTO.setId(customersID);
        Customer mockResponseData = entityResponseData();
        mockResponseData.setId(customersID);

        Mockito.when(customerService.update(customersID, mockRequestDTO)).thenReturn(mockResponseData);
        mockMvc.perform(MockMvcRequestBuilders
                .put("/api/v1/customer/{id}/edit", customersID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequestDTO))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print());
    }



    @Test
    @DisplayName("Should DELETE A Customer AND Return NO CONTENT when making a DELETE Request To Endpoint -  /{id}")
    public void shouldDeleteACustomer_andReturnNoContent() throws Exception {
        UUID customersID = UUID.randomUUID();

        Mockito.when(customerService.delete(customersID)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/customer/{id}", customersID)
        )
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("Should FETCH Paginated Customers Data Using Request Parameters(size,page,sortDirection,sortBy) AND Return OK Using A GET Method To The Endpoint - /api/v1/customer")
    public void shouldFetchPaginatedOrders_andReturnOK() throws Exception{
        PaginatedCustomerResponse mockResponseData = paginatedCustomerResponse();
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("page", "0");
        paramsMap.add("size", "10");
        paramsMap.add("sortBy", "id");
        paramsMap.add("sortDir", "ASC");

        Mockito.when(customerService.fetchPaginatedCustomer(page,size,sortBy,sortDir)).thenReturn(mockResponseData);

        mockMvc.perform(get("/api/v1/customer").params(paramsMap))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content.size()").value(mockResponseData.getContent().size()));

    }


    private Customer entityResponseData(){
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("TestName");
        customer.setLastName("TestName");
        customer.setAddress("Test Address");
        customer.setPhoneNumber("0201111223");
        customer.setEmail("testName@testMail.com");
        return customer;
    }

    private CustomerDTO entityRequestData(){
        return CustomerDTO
                .builder()
                .id(UUID.randomUUID())
                .address("Test Address")
                .email("testName@testMail.com")
                .lastName("TestName")
                .firstName("TestName")
                .phoneNumber("0201111223")
                .build();
    }

    private CustomerDTO wrongEntityRequestData() {
        return CustomerDTO
                .builder()
                .id(UUID.randomUUID())
                .address("Test Address")
                .email("testName@testMail.com")
                .lastName("TestName")
                .firstName("")
                .phoneNumber("0201111223")
                .build();
    }

    private PaginatedCustomerResponse paginatedCustomerResponse(){
        List<CustomerDTO> customerDTOS = Arrays.asList(entityRequestData(),entityRequestData());
        return PaginatedCustomerResponse.builder()
                .content(customerDTOS)
                .pageNo(0)
                .pageSize(10)
                .totalElements(3)
                .totalPages(1)
                .last(true).build();
    }


}