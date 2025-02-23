package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public abstract class ControllerBase {

    protected void saveUserInSession(HttpSession session, UserSession userSession) {
        session.setAttribute("userSession", userSession);
    }
    public UserSession getUserSessionSecurity(){
        return (UserSession) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    protected void removeSession(HttpSession session) {
        session.removeAttribute("userSession");
    }
}
