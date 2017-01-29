package com.github.hronom.ba.sorter.config.custom.objects.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRoute {
    @JsonProperty
    public String login;
    @JsonProperty
    public String password;
    @JsonProperty
    public String url;
}
