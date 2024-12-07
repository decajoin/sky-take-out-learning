package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
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
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);

        try {
            // 获得原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取原始文件名后缀
            String suffix = null;
            if (originalFilename != null) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 使用 uuid 生成文件名
            String fileName = UUID.randomUUID() + suffix;
            // 上传文件，得到文件上传后的路径
            String filePath = aliOssUtil.upload(file.getBytes(), fileName);
            // 返回文件上传后的路径
            return Result.success(filePath);

        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
        }

        // 返回文件上传失败信息，前端可以提示用户上传文件失败
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
