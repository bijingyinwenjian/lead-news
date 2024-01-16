package com.heima.search.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.search.mapper.ApUserSearchMapper;
import com.heima.search.service.ApUserSearchService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service
@Transactional
@Log4j2
public class ApUserSearchServiceImpl extends ServiceImpl<ApUserSearchMapper, ApUserSearch>  implements ApUserSearchService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void insert(String keyword, Integer userId) {


    }
}
