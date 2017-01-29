package com.github.hronom.ba.sorter.config.custom.objects.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class RouteConfig {
    @JsonProperty
    public final ArrayList<UserRoute> userRoutes = new ArrayList<>();
}
