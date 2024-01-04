package com.heima.common.tengxunyun;

import com.alibaba.fastjson.JSON;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ims.v20201229.ImsClient;
import com.tencentcloudapi.ims.v20201229.models.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tengxunyun")
public class ImageScan {

    private String secretId;
    private String secretKey;

    /**
     * 同步
     *
     * @param args
     */
    public void main(String[] args) {
        try {
            Credential cred = new Credential(secretId, secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ims.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            ImsClient client = new ImsClient(cred, "ap-guangzhou", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ImageModerationRequest req = new ImageModerationRequest();
            req.setFileContent("");
            // 返回的resp是一个ImageModerationResponse的实例，与请求对象对应
            ImageModerationResponse resp = client.ImageModeration(req);
            // 输出json格式的字符串回包
            System.out.println(ImageModerationResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * 异步
     *
     * @param args
     */
    public void main1(String[] args) {
        try {
            Credential cred = new Credential(secretId, secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ims.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            ImsClient client = new ImsClient(cred, "ap-guangzhou", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateImageModerationAsyncTaskRequest req = new CreateImageModerationAsyncTaskRequest();
            req.setFileUrl("http://111.230.204.58:9000/leadnews/2023/12/26/bf146d370a9f4ed38de7d688cd8bb8d8.png");
            // 返回的resp是一个CreateImageModerationAsyncTaskResponse的实例，与请求对象对应
            CreateImageModerationAsyncTaskResponse resp = client.CreateImageModerationAsyncTask(req);
            // 输出json格式的字符串回包
            System.out.println(CreateImageModerationAsyncTaskResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }

    public Map asyncImage(String callbackUrl, byte[] image) throws TencentCloudSDKException {
        // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
        Credential cred = new Credential(secretId, secretKey);

        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ims.tencentcloudapi.com");

        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        // 实例化要请求产品的client对象,clientProfile是可选的
        ImsClient client = new ImsClient(cred, "ap-guangzhou", clientProfile);

        // 实例化一个请求对象,每个接口都会对应一个request对象
        CreateImageModerationAsyncTaskRequest req = new CreateImageModerationAsyncTaskRequest();
        //设置图片url地址

        req.setFileContent(Base64.getEncoder().encodeToString(image));
        req.setCallbackUrl(callbackUrl);

        // 返回的resp是一个CreateImageModerationAsyncTaskResponse的实例，与请求对象对应
        CreateImageModerationAsyncTaskResponse resp = client.CreateImageModerationAsyncTask(req);

        // 输出json格式的字符串回包
        String result = CreateImageModerationAsyncTaskResponse.toJsonString(resp);
        return JSON.parseObject(result, Map.class);
    }

    public Map greenImageDetection(String imageUrl) throws TencentCloudSDKException {
        // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
        Credential cred = new Credential(secretId, secretKey);

        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ims.tencentcloudapi.com");

        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        // 实例化要请求产品的client对象,clientProfile是可选的
        ImsClient client = new ImsClient(cred, "ap-guangzhou", clientProfile);

        // 实例化一个请求对象,每个接口都会对应一个request对象
        ImageModerationRequest req = new ImageModerationRequest();
        //设置图片url地址
        req.setFileUrl(imageUrl);

        // 返回的resp是一个ImageModerationResponse的实例，与请求对象对应
        ImageModerationResponse resp = client.ImageModeration(req);

        // 输出json格式的字符串回包
        String result = ImageModerationResponse.toJsonString(resp);

        return JSON.parseObject(result, Map.class);
    }


}
