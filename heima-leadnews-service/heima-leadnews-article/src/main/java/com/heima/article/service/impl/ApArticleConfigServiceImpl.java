package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.article.pojos.ApArticleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {

    @Override
    public void updateByMap(Map<String, Object> map) {
        Long articleId = (Long) map.get("articleId");
        Short enable = Short.parseShort(map.get("enable").toString());
        update(Wrappers.<ApArticleConfig>lambdaUpdate().set(ApArticleConfig::getIsDown, enable == 0).eq(ApArticleConfig::getArticleId, articleId));
    }
}
