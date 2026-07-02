package com.seatbooking.filter;

import com.seatbooking.context.UserContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        Long userId = (Long) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");
        String role = (String) httpRequest.getAttribute("role");

        if (userId != null) {
            UserContext.setCurrentUserId(userId);
            UserContext.setCurrentUsername(username);
            UserContext.setCurrentRole(role);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}