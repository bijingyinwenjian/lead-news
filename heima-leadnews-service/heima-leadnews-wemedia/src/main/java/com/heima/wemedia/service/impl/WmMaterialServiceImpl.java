package com.heima.wemedia.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 图片上传
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {

        //1.检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.上传图片到minIO中
        String url;
        String prefixName = UUID.randomUUID().toString().replace("-", "");
        String suffixName = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        try {
            url = fileStorageService.uploadImgFile("", prefixName + suffixName, multipartFile.getInputStream());
            log.info("上传图片到MinIO中，url:{}", url);
        } catch (IOException e) {
            log.error("WmMaterialServiceImpl-上传文件失败");
            throw new RuntimeException();
        }

        //3.保存到数据库中
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setUrl(url);
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);
        //4.返回结果
        return ResponseResult.okResult(wmMaterial);

    }

    @Override
    public PageResponseResult findList(WmMaterialDto wmMaterialDto) {
        wmMaterialDto.checkParam();
        Page<WmMaterial> page = new Page(wmMaterialDto.getPage(), wmMaterialDto.getSize());
        Page<WmMaterial> page1 = page(page, Wrappers.<WmMaterial>lambdaQuery()
                .eq(wmMaterialDto.getIsCollection() != null,
                WmMaterial::getIsCollection, wmMaterialDto.getIsCollection())
                .eq(WmMaterial::getUserId,WmThreadLocalUtil.getUser().getId())
                .orderByDesc(WmMaterial::getCreatedTime)

        );
        PageResponseResult pageResponseResult = new PageResponseResult(wmMaterialDto.getPage(), wmMaterialDto.getSize(), (int) page1.getTotal());
        pageResponseResult.setData(page1.getRecords());
        return pageResponseResult;
    }

}