package com.lanxige.controller;

import com.lanxige.eurm.BusinessType;
import com.lanxige.util.Result;
import com.lanxige.utils.OssTemplate;
import com.lanxige.utils.VodTemplate;
import com.lanxige.utils.aop.OpenLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/admin/system/upload")
@CrossOrigin
public class UploadController {
    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private VodTemplate vodTemplate;

    // 上传图片
    @RequestMapping(value = "/uploadImage")
    public Result uploadImage(MultipartFile uploadImage) throws IOException {
        long size = uploadImage.getSize();
        String contentType = uploadImage.getContentType();

        if (size > 1024 * 1024 * 10) {
            return Result.fail("文件大小不能超过10M");
        }

        if (!contentType.contains("jpeg")
                && !contentType.contains("png")
                && !contentType.contains("jpg")) {
            return Result.fail("文件类型错误，只支持 png、jpg、jpeg");
        }

        String upload = this.ossTemplate.upload(uploadImage.getOriginalFilename(), uploadImage.getInputStream());

        return Result.ok(upload);
    }

    // 上传视频操作
    @RequestMapping(value = "/uploadVideo")
    public String uploadVideo(MultipartFile uploadVideo) throws IOException {
        String videoId = this.vodTemplate.uploadVideo(uploadVideo.getOriginalFilename(), uploadVideo.getInputStream());
        System.out.println("videoId=====" + videoId);

        return videoId;
    }
}
