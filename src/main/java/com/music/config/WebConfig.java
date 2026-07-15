package com.music.config;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration

public class WebConfig implements WebMvcConfigurer {
    @Autowired

    private FileUploadConfig uploadConfig;

    @Override

    public void addResourceHandlers (ResourceHandlerRegistry registry) {

        registry.addResourceHandler (uploadConfig.getPathMapping () + "**")

                .addResourceLocations ("file:" + uploadConfig.getPath ());

    }
}
