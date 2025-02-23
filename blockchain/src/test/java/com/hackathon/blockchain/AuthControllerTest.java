package com.hackathon.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.blockchain.dto.RequestLoginUser;
import com.hackathon.blockchain.dto.RequestRegisterUserDTO;
import com.hackathon.blockchain.dto.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }
    @Test
    void testCheckSession_UserNotLoggedIn() throws Exception {
        mockMvc.perform(get("/auth/check-session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testNewRegisterUser_Success() throws Exception {
        RequestRegisterUserDTO dto = RequestRegisterUserDTO.builder()
                .email("user@example.com")
                .username("user123")
                .password("securePassword")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseDTO.registerMessage().getMessage()));
    }

    @Test
    void testNewLoginUser_Success() throws Exception {
        RequestLoginUser dto = RequestLoginUser.builder()
                .username("user123")
                .password("securePassword")
                .build();

        // Realiza el inicio de sesión
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseDTO.loginSuccessful().getMessage()));

    }




}
