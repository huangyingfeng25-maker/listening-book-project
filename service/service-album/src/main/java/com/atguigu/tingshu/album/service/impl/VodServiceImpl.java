package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@Service
public class VodServiceImpl implements VodService {

    @Autowired
    private VodConstantProperties vodConstantProperties;

    //上传声音
    @Override
    public Map<String, Object> uploadTrack(MultipartFile file) {

        String tempPath= UploadFileUtil.uploadTempPath(vodConstantProperties.getTempPath(),file);
        //创建VodUploadClient  传递腾讯云的账户id和密钥
        VodUploadClient client=
                new VodUploadClient(vodConstantProperties.getSecretId(),
                                    vodConstantProperties.getSecretKey());
        //  构建上传请求对象
        VodUploadRequest request = new VodUploadRequest();
        //  设置视频本地地址
        request.setMediaFilePath(tempPath);
        //  指定任务流
        request.setProcedure(vodConstantProperties.getProcedure());
        try {
            //  调用上传方法
            VodUploadResponse response = client.upload(vodConstantProperties.getRegion(), request);
            //  创建map 对象
            HashMap<String, Object> map = new HashMap<>();
            map.put("mediaFileId",response.getFileId());
            map.put("mediaUrl",response.getMediaUrl());
            //  返回map 数据
            return Map.of();
        }catch (Exception e){
            //业务方法进行异常处理
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }
}
