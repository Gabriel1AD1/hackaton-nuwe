package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckSessionDTO {

    @JsonProperty("user")
    private UserDTO user;

    // Clase interna para representar el usuario
    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserDTO {
        @JsonProperty("username")
        private String username;

    }
    public static CheckSessionDTO messageCheckSession(String username){
        return new CheckSessionDTOBuilder().user(UserDTO.builder().username(username).build()).build();
    }
}
