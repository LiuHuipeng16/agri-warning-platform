package com.zhku.agriwarningplatform.common.util;

import com.aliyun.oss.*;
import com.zhku.agriwarningplatform.module.crop.vo.AliOssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AliyunOSSOperator {

    private final AliOssProperties aliOssProperties;

    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliOssProperties.getEndpoint();
        String bucketName = aliOssProperties.getBucketName();
        String accessKeyId = aliOssProperties.getAccessKeyId();
        String accessKeySecret = aliOssProperties.getAccessKeySecret();


        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String objectName = "agriwarningplatform/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" + UUID.randomUUID().toString() + originalFilename;
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));

            String protocol = endpoint.startsWith("https://") ? "https://" : "http://";
            String domain = endpoint.replace("https://", "").replace("http://", "");
            return protocol + bucketName + "." + domain + "/" + objectName;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
