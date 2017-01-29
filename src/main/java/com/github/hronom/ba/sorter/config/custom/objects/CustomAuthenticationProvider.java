package com.github.hronom.ba.sorter.config.custom.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;

public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    public CustomAuthenticationProvider() {
    }

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        UsernamePasswordAuthenticationToken auth =
            (UsernamePasswordAuthenticationToken) authentication;
        String username = String.valueOf(auth.getPrincipal());
        String password = String.valueOf(auth.getCredentials());

        if ("Hronom".equals(username) && "1".equals(password)) {
            ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
            return new CustomUser(username, password, authorities);
        } else if ("Hronom 2".equals(username) && "1".equals(password)) {
            ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
            return new CustomUser(username, password, authorities);
        }

        throw new BadCredentialsException("Authentication fails!");
    }

    @Override
    public boolean supports(Class aClass) {
        // To indicate that this AuthenticationProvider can handle the auth request. since there's
        // currently only one way of logging in, always return true.
        return true;
    }
}
