package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDownOrUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "文章管理")
@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Resource
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto dto){
        return  wmNewsService.findAll(dto);
    }

    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        return  wmNewsService.submitNews(dto);
    }


    @GetMapping("/one/{id}")
    public ResponseResult one(@PathVariable("id") Integer id){
        return wmNewsService.one(id);
    }

    @GetMapping("/del_news/{id}")
    public ResponseResult delNews(@PathVariable("id")Integer id){
        return wmNewsService.delNews(id);
    }

    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDownOrUpDto wmNewsDownOrUpDto){
        return wmNewsService.downOrUp(wmNewsDownOrUpDto);
    }
}
