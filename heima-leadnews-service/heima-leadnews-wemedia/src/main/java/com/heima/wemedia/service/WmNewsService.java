package com.heima.wemedia.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDownOrUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 查询文章
     * @param dto
     * @return
     */
    public ResponseResult findAll(WmNewsPageReqDto dto);

    ResponseResult submitNews(WmNewsDto dto);

    /**
     * 文章详情
     * @param id
     * @return
     */
    ResponseResult one(Integer id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    ResponseResult delNews(Integer id);

    /**
     * 发布或者下架文章
     * @param wmNewsDownOrUpDto
     * @return
     */
    ResponseResult downOrUp(WmNewsDownOrUpDto wmNewsDownOrUpDto);
}
