package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Resource
    private WmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/list")
    public PageResponseResult findList(@RequestBody WmMaterialDto wmMaterialDto){
        return wmMaterialService.findList(wmMaterialDto);
    }

    @GetMapping("/del_picture/{id}")
    public ResponseResult delPicture(@PathVariable("id") Integer id){
        return wmMaterialService.delPicture(id);
    }

    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancelCollect(@PathVariable("id") Integer id){
        return wmMaterialService.cancelCollect(id);
    }

    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable("id") Integer id){
        return wmMaterialService.collect(id);
    }
}
