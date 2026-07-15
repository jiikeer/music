package com.music.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;


@Component
public class FileConfig {

    @Autowired
    private FileUploadConfig uploadConfig;

    public String saveAvatar(MultipartFile file) throws IOException {
        File dir = new File(uploadConfig.getPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        File targetFile = new File(dir, fileName);
        file.transferTo(targetFile);

        return uploadConfig.getServerAddress() + ":" + uploadConfig.getServerPort()
                + uploadConfig.getPathMapping() + fileName;
    }
}

