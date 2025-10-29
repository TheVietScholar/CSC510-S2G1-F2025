package com.boozebuddies.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.boozebuddies.dto.AuthenticationRequest;
import com.boozebuddies.dto.AuthenticationResponse;
import com.boozebuddies.dto.RefreshTokenRequest;
import com.boozebuddies.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

  @Autowired private MockMvc mvc;
  @MockBean private AuthenticationService authenticationService;
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void loginReturnsToken() throws Exception {
    AuthenticationRequest req =
        AuthenticationRequest.builder().email("test@example.com").password("secret").build();

    AuthenticationResponse resp =
        AuthenticationResponse.builder().token("fake-token").message("ok").build();

    when(authenticationService.login(req)).thenReturn(resp);

    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("fake-token"));
  }

  @Test
  void refreshReturnsToken() throws Exception {
    RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("r-token").build();

    AuthenticationResponse resp =
        AuthenticationResponse.builder().token("new-token").message("refreshed").build();

    when(authenticationService.refreshToken(req)).thenReturn(resp);

    mvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("new-token"));
  }
}
