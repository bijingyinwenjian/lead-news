package com.heima.common.tengxunyun;


import com.alibaba.fastjson.JSON;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tms.v20201229.TmsClient;
import com.tencentcloudapi.tms.v20201229.models.TextModerationRequest;
import com.tencentcloudapi.tms.v20201229.models.TextModerationResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tengxunyun")
public class TextScan {

    private String secretId;
    private String secretKey;

    public Map greenTextDetection(String text) throws TencentCloudSDKException {
        // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
        Credential cred = new Credential(secretId, secretKey);

        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("tms.tencentcloudapi.com");

        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        // 实例化要请求产品的client对象,clientProfile是可选的
        TmsClient client = new TmsClient(cred, "ap-guangzhou", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        TextModerationRequest req = new TextModerationRequest();

        //Base64加密
        String encryptionText = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        //设置内容参数
        req.setContent(encryptionText);

        // 返回的resp是一个TextModerationResponse的实例，与请求对象对应
        TextModerationResponse resp = client.TextModeration(req);

        // 输出json格式的字符串回包
        String result = TextModerationResponse.toJsonString(resp);
        return JSON.parseObject(result,Map.class);
    }

}