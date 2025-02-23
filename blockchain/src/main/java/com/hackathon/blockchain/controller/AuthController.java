package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.RequestLoginUser;
import com.hackathon.blockchain.dto.UserSession;
import com.hackathon.blockchain.dto.RequestRegisterUserDTO;
import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController extends ControllerBase {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> newRegisterUser(
            @RequestBody RequestRegisterUserDTO dto,
            HttpSession session) {

        var response = userService.register(dto);

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
    public ResponseEntity<ResponseDTO> newLoginUser(@RequestBody RequestLoginUser dto, HttpSession session){
        var response = userService.login(dto);
        // Guardar usuario en la sesión usando la función heredada
        saveUserInSession(session, UserSession.builder()
                .userId(response.getId())
                .username(response.getUsername())
                .build());
        return ResponseEntity.ok(ResponseDTO.loginSuccessful());
    }


}
