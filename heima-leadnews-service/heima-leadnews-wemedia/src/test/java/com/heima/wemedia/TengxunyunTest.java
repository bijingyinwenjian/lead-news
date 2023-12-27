package com.heima.wemedia;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.tengxunyun.ImageScan;
import com.heima.common.tengxunyun.TextScan;
import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class TengxunyunTest {

    @Resource
    private TextScan textScan;

    @Resource
    private ImageScan imageScan;

    @Test
    public void testScanText() throws Exception {
        Map map = textScan.greenTextDetection("我是一个好人,冰毒");
        System.out.println(map);
    }

    @Test
    public void testScanImage() throws Exception {
        Map map = imageScan.asyncImageByUrl("http://111.230.204.58:9000/leadnews/2023/12/26/bf146d370a9f4ed38de7d688cd8bb8d8.png");
        System.out.println(map);
    }

    @Test
    public void testScanImage1() throws Exception {
        Map map = imageScan.greenImageDetection("http://111.230.204.58:9000/leadnews/2023/12/26/bf146d370a9f4ed38de7d688cd8bb8d8.png");
        System.out.println(map);
    }
}