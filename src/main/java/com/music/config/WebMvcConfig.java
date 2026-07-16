package com.music.config;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration

public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private FileConfig fileConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:"+fileConfig.getPath()+"avatar/");

        registry.addResourceHandler("/song/**")
                .addResourceLocations("file:"+fileConfig.getPath()+"song/");

        registry.addResourceHandler("/songPic/**")
                .addResourceLocations("file:"+fileConfig.getPath()+"songPic/");

        registry.addResourceHandler("/post/**")
                .addResourceLocations("file:"+fileConfig.getPath()+"post/");
    }
}
