package com.hackathon.blockchain.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.blockchain.dto.ApiResponse;
import com.hackathon.blockchain.dto.UserSession;
import com.hackathon.blockchain.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@Component
@AllArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper; // Agrega ObjectMapper




    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (urisPermitted(request, response, filterChain)) return;

        try {
            // Obtener la sesión del usuario
            Optional<HttpSession> session = Optional.ofNullable(request.getSession(false));

            if (session.isPresent() && existAttribute(session)) {
                UserSession userSession = (UserSession) session.get().getAttribute("userSession");
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userSession, null, userSession.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Si hay una sesión activa, puedes continuar con el filtro
                filterChain.doFilter(request, response);
            } else {
                throw new UnauthorizedException("You do not have an active session, please log in.");
            }
        } catch (UnauthorizedException ex) {
            ApiResponse responseApi = ApiResponse.unauthorized(ex.getMessage()).build();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json"); // Establece el tipo de contenido
            response.getWriter().write(objectMapper.writeValueAsString(responseApi)); // Serializa a JSON
        }
    }

    private static boolean urisPermitted(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request.getRequestURI().startsWith("/auth/register")||
                request.getRequestURI().startsWith("/auth/login")||
                request.getRequestURI().startsWith("/auth/logout") ||
                request.getRequestURI().startsWith("/health") ||
                request.getRequestURI().startsWith("/market/prices")||
                request.getRequestURI().startsWith("/market/price")
        )

        {
            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }

    // Método privado para verificar si el atributo existe
    private boolean existAttribute(Optional<HttpSession> session) {
        return session.isPresent() && session.get().getAttribute("userSession") != null;
    }
}
