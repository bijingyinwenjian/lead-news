package com.heima.search.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.mapper.ApUserSearchMapper;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@Service
@Transactional
@Log4j2
public class ApUserSearchServiceImpl extends ServiceImpl<ApUserSearchMapper, ApUserSearch>  implements ApUserSearchService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Async
    @Override
    public void insert(String keyword, Integer userId) {
        // 判断这个搜索关键字是否存在
        Query query = Query.query(Criteria.where("keyword").is(keyword).and("userId").is(userId));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        // 存在更新创建时间
        if (apUserSearch != null) {
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }

        // 不存在，判断搜索是否 超过10
        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());

        List<ApUserSearch> apUserSearches = mongoTemplate.find(Query.query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC,"createTime")), ApUserSearch.class);
        if (apUserSearches == null || apUserSearches.size() < 10){
            mongoTemplate.save(apUserSearch);
            return;
        }
        // 超过10 删除最后一个
        ApUserSearch search = apUserSearches.get(apUserSearches.size() - 1);
        mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(search.getId())),apUserSearch);
        return;
    }

    @Override
    public ResponseResult findUserSearch() {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user != null){
            List<ApUserSearch> list = mongoTemplate.find(Query.query(Criteria.where("userId").is(user.getId() + "")).with(Sort.by(Sort.Direction.DESC, "createTime")), ApUserSearch.class);
            return ResponseResult.okResult(list);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
    }

    @Override
    public ResponseResult delUserSearch(HistorySearchDto historySearchDto) {
        if (historySearchDto == null || historySearchDto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        mongoTemplate.remove(Query.query(Criteria.where("id").is(historySearchDto.getId()).and("userId").is(user.getId())),ApUserSearch.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
