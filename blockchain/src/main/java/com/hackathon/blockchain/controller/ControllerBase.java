package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.UserSession;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ControllerBase {

    private static final Logger log = LoggerFactory.getLogger(ControllerBase.class);

    protected void saveUserInSession(HttpSession session, UserSession userSession) {
        session.setAttribute("userSession", userSession);
    }
    public UserSession getUserSessionSecurity() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return (UserSession) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return null; // O lanzar una excepción si prefieres
    }
    protected void removeSession(HttpSession session) {
        session.removeAttribute("userSession");
    }
}
