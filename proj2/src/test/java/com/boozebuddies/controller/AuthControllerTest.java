package com.boozebuddies.controller;

import com.boozebuddies.dto.*;
import com.boozebuddies.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private final AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
    private final AuthController authController = new AuthController(authenticationService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginReturnsToken() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken("fake-jwt-token");

        Mockito.when(authenticationService.login(Mockito.any(AuthenticationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }
}
