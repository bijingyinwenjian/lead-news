package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.common.tengxunyun.ImageScan;
import com.heima.common.tengxunyun.TextScan;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Resource
    private WmNewsMapper wmNewsMapper;

    @Resource
    private TextScan textScan;

    @Resource
    private ImageScan imageScan;

    @Resource
    private IArticleClient iArticleClient;

    @Resource
    private WmChannelMapper wmChannelMapper;

    @Resource
    private WmUserMapper wmUserMapper;

    @Override
    @Async
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }

        // 只有文章状态 ！= 9的才需要审核，发布了的不需要审核
        if (!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            //从内容中提取纯文本内容和图片
            Map<String, Object> resultMap = handleTextAndImages(wmNews);

            //2.审核文本内容
            String contents = resultMap.get("content").toString();

            if (!handleTextScan(contents, wmNews)) {
                return;
            }

            //3.审核图片
            Set<String> images = (Set<String>) resultMap.get("images");
            if (!handleImageScan(images, wmNews)) {
                return;
            }

            //4.审核成功，保存app端的相关的文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }
            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,(short) 9,"审核成功");
        }

        return;
    }

    /**
     * 保存app端相关的文章数据
     *
     * @param wmNews
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,articleDto);
        // 文章布局
        articleDto.setLayout(wmNews.getType());
        // 频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null){
            articleDto.setChannelName(wmChannel.getName());
        }
        // 设置作者
        articleDto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser !=null) {
            articleDto.setAuthorName(wmUser.getName());
        }
        // 文章id
        if (wmNews.getArticleId() != null){
            articleDto.setId(wmNews.getArticleId());
        }
        articleDto.setCreatedTime(new Date());
        return iArticleClient.saveArticle(articleDto);
    }

    /**
     * 审核图片
     *
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImageScan(Set<String> images, WmNews wmNews) {
        for (String image : images) {
            Map<String, Object> map = new HashMap<>();
            try {
                //审核图片
                map = imageScan.greenImageDetection(image);
                if (map == null){
                    return false;
                }
                if (map.get("Suggestion").equals("Block")) {
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                    return false;
                }
                //不确定信息  需要人工审核
                if (map.get("Suggestion").equals("review")) {
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
                    return false;
                }
            } catch (TencentCloudSDKException e) {
                e.printStackTrace();
            }
        }


        return true;
    }

    /**
     * 审核纯文本内容
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        Map<String, Object> map = new HashMap<>();
        try {
            map = textScan.greenTextDetection(wmNews.getTitle() + "-" + content);
            if (map == null){
                return false;
            }
            if (map.get("Suggestion").equals("Block")) {
                updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                return false;
            }
            //不确定信息  需要人工审核
            if (map.get("Suggestion").equals("review")) {
                updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
                return false;
            }
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云审核文本报错:{}", e);
            throw new RuntimeException("腾讯云审核文本报错");
        }
        return true;
    }

    /**
     * 修改文章内容
     *
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 1。从自媒体文章的内容中提取文本和图片
     * 2.提取文章的封面图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        //存储纯文本内容
        StringBuilder stringBuilder = new StringBuilder();

        Set<String> images = new HashSet<>();
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            //1。从自媒体文章的内容中提取文本和图片
            List<Map> maps = JSON.parseArray(wmNews.getContent(), Map.class);
            for (Map<String, String> map : maps) {
                if (map.get("type").equals("text")) {
                    stringBuilder.append(map.get("value"));
                }
                if (map.get("type").equals("image")) {
                    images.add(map.get("value"));
                }
            }
            //2.提取文章的封面图片
            if (StringUtils.isNotBlank(wmNews.getImages())) {
                String[] split = wmNews.getImages().split(",");
                images.addAll(Arrays.asList(split));
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder.toString());
        resultMap.put("images", images);
        return resultMap;
    }
}
