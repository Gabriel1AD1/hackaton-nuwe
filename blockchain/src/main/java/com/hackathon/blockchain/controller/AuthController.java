package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.*;
import com.hackathon.blockchain.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController extends ControllerBase {

    private final UserService userServiceI;

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> newRegisterUser(
            @Valid @RequestBody RequestRegisterUserDTO dto,
            HttpSession session) {

        var response = userServiceI.register(dto);

        // Guardar usuario en la sesión usando la función heredada
        saveUserInSession(session, UserSession.builder()
                .userId(response.getId())
                .username(response.getUsername())
                .build());

        return ResponseEntity.ok(ResponseDTO.registerMessage());
    }
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO> logout(HttpSession session) {
        removeSession(session); // Eliminar la sesión del usuario
        return ResponseEntity.ok(ResponseDTO.logoutMessage());
    }
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> newLoginUser(@Valid @RequestBody RequestLoginUser dto, HttpSession session){
        var response = userServiceI.login(dto);
        // Guardar usuario en la sesión usando la función heredada
        saveUserInSession(session, UserSession.builder()
                .userId(response.getId())
                .username(response.getUsername())
                .build());
        return ResponseEntity.ok(ResponseDTO.loginSuccessful());
    }
    @GetMapping("/check-session")
    public ResponseEntity<CheckSessionDTO> checkSession() {
        var userOptional = Optional.ofNullable(getUserSessionSecurity());
        if (userOptional.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        var user = userOptional.get();
        return ResponseEntity.ok(CheckSessionDTO.messageCheckSession(user.getUsername()));
    }

}
