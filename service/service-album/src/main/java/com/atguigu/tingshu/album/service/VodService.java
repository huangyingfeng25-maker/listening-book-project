package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VodService {

    Map<String, Object> uploadTrack(MultipartFile file);

    //根据声音的mediaFileId调用腾讯云方法获取
    TrackMediaInfoVo getmediaaInfoByFileId(@NotEmpty(message = "媒体文件Id不能为空") String mediaFileId);
}
