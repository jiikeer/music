package com.music.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileUploadConfig {

    private final FileConfig fileConfig;

    public String upload(MultipartFile file,String folder) throws IOException {

        if(file==null||file.isEmpty()){
            throw new RuntimeException("文件不能为空");
        }

        String suffix = "";

        String filename=file.getOriginalFilename();

        if(filename!=null&&filename.contains(".")){
            suffix=filename.substring(filename.lastIndexOf("."));
        }

        String newName= UUID.randomUUID()+suffix;

        String dir=fileConfig.getPath()+File.separator+folder;

        File uploadDir=new File(dir);

        if(!uploadDir.exists()){
            uploadDir.mkdirs();
        }

        File dest=new File(uploadDir,newName);

        java.io.InputStream is = file.getInputStream();
        java.nio.file.Files.copy(is, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        is.close();

        return "/"+folder+"/"+newName;
    }

}
