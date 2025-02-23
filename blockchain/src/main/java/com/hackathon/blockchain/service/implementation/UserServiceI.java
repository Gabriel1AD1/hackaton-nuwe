package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.RequestLoginUser;
import com.hackathon.blockchain.dto.RequestRegisterUserDTO;
import com.hackathon.blockchain.exception.AuthRequestFailedException;
import com.hackathon.blockchain.exception.EntityAlreadyException;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceI implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceI.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public User register(RequestRegisterUserDTO dto) {
        log.trace("Intentando guardar usuario: {} - {}", dto.getUsername(), dto.getEmail());

        verifyUsernameAlreadyExist(dto);
        User savedUser = saveUser(dto);

        log.info("Usuario guardado con ID: {}", savedUser.getId());
        return savedUser;
    }

    private void verifyUsernameAlreadyExist(RequestRegisterUserDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new EntityAlreadyException("Username already exists");
        }
    }

    private User saveUser(RequestRegisterUserDTO dto) {
        return userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .username(dto.getUsername())
                .build());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    @Override
    public User login(RequestLoginUser dto) {
        var userDb = userRepository.findByUsername(dto.getUsername());

        varifyPassword(userDb.isEmpty());
        // Obtener el usuario de la envoltura Optional
        User user = userDb.get();

        // Verificar la contraseña
        varifyPassword(!passwordEncoder.matches(dto.getPassword(), user.getPassword()));
        // Aquí puedes agregar la lógica para manejar la sesión del usuario si es necesario
        log.info("Usuario {} ha iniciado sesión correctamente.", user.getUsername());
        return user;
    }

    private void varifyPassword(boolean passwordEncoder) {
        if (passwordEncoder) {
            throw new AuthRequestFailedException();
        }
    }

}