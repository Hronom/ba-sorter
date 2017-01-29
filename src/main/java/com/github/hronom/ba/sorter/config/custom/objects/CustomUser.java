package com.github.hronom.ba.sorter.config.custom.objects;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomUser extends UsernamePasswordAuthenticationToken {
    private final String username;
    private final String password;
    private final String url;

    public CustomUser(
        String usernameArg,
        String passwordArg,
        Collection<? extends GrantedAuthority> authoritiesArg,
        String urlArg
    ) {
        super(usernameArg, passwordArg, authoritiesArg);
        username = usernameArg;
        password = passwordArg;
        url = urlArg;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }
}
