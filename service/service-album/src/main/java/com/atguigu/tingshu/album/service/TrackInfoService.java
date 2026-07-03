package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TrackInfoService extends IService<TrackInfo> {

    //保存声音
    void saveTrackInfo(TrackInfoVo trackInfoVo);

    IPage<TrackListVo> findUserTrackPage(Page<TrackListVo> pageParam, TrackInfoQuery trackInfoQuery);

    //删除声音
    void removeTrackInfo(Long id);
}
