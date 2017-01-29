package com.github.hronom.ba.sorter.config.custom.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hronom.ba.sorter.config.custom.objects.config.RouteConfig;
import com.github.hronom.ba.sorter.config.custom.objects.config.UserRoute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class JsonConfigAuthenticationProvider implements AuthenticationProvider {
    private static final Logger logger = LogManager.getLogger();

    private final Path configPath = Paths.get("configs", "RouteConfig.json");

    private final ConcurrentHashMap<String, UserRoute> userRoutesByLogin =
        new ConcurrentHashMap<>();

    @Autowired
    public JsonConfigAuthenticationProvider() {
        try (BufferedReader br = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            ObjectMapper objectMapper = new ObjectMapper();
            RouteConfig routeConfig = objectMapper.readValue(br, RouteConfig.class);
            for (UserRoute userRoute : routeConfig.userRoutes) {
                userRoutesByLogin.put(userRoute.login, userRoute);
            }
        } catch (IOException exception) {
            logger.error("Error", exception);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        UsernamePasswordAuthenticationToken auth =
            (UsernamePasswordAuthenticationToken) authentication;
        String username = String.valueOf(auth.getPrincipal());
        String password = String.valueOf(auth.getCredentials());

        UserRoute userRoute = userRoutesByLogin.get(username);
        if (userRoute != null) {
            if (Objects.equals(userRoute.login, username) &&
                Objects.equals(userRoute.password, password)) {
                ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
                return new CustomUser(username, password, authorities, userRoute.url);
            }
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
