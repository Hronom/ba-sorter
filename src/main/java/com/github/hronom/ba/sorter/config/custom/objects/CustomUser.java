package com.github.hronom.ba.sorter.config.custom.objects;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomUser extends UsernamePasswordAuthenticationToken {
    private final String username;
    private final String password;

    public CustomUser(
        String usernameArg, String passwordArg, Collection<? extends GrantedAuthority> authoritiesArg
    ) {
        super(usernameArg, passwordArg, authoritiesArg);
        username = usernameArg;
        password = passwordArg;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
