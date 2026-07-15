package com.music.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "picture-file")
@Data
public class FileUploadConfig {

    private String path;

    private String pathMapping;

    private String serverAddress;

    private String serverPort;

}
