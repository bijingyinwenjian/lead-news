package com.heima.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.search.pojos.ApUserSearch;

public interface ApUserSearchService extends IService<ApUserSearch> {
    /**
     * 保存用户搜索历史记录
     * @param keyword
     * @param userId
     */
    public void insert(String keyword,Integer userId);
}
