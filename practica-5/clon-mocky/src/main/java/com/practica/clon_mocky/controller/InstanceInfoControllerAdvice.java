package com.practica.clon_mocky.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class InstanceInfoControllerAdvice {

    @Value("${server.port}")
    private String serverPort;

    @ModelAttribute("instancePort")
    public String instancePort() {
        return serverPort;
    }
}
