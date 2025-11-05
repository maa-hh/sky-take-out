package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api("通用接口")
@Slf4j
public class CommonController {
    @Autowired
    AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    @ApiOperation("上传至阿里云OSS")
    public Result<String> upload(MultipartFile file)  {
        log.info("文件上传：{}",file);
        String ordinalname= file.getName().substring(file.getName().lastIndexOf("."));
        String name= UUID.randomUUID()+ordinalname;
        try {
            aliOssUtil.upload(file.getBytes(), name);
            return Result.success(name);
        } catch (IOException e) {
            log.info("上传失败");
            return Result.error("error");
        }
    }
}
