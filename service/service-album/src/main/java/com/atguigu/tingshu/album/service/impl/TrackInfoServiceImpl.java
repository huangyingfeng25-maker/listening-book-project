package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;
	@Autowired
	private VodService vodService;
	@Autowired
	private AlbumInfoMapper albumInfoMapper;
	@Autowired
	private TrackStatMapper trackStatMapper;

	//保存声音
	@Override
	public void saveTrackInfo(TrackInfoVo trackInfoVo) {
		//1.添加声音的基本信息track_info
		TrackInfo trackInfo=new TrackInfo();
		BeanUtils.copyProperties(trackInfoVo,trackInfo);

		//需要手动设置
		// 用户id
		trackInfo.setUserId(1L);
		//声音在专辑中的排序值
		//获取专辑下面最大order_num
		//select order_num
		//from track_info it
		//where album_id=1
		//order by id desc
		//limit 1
		LambdaQueryWrapper<TrackInfo>wrapper=new LambdaQueryWrapper<>();
		//设置查询字段order_num
		wrapper.select(TrackInfo::getOrderNum);
		//专辑id查询
		wrapper.eq(TrackInfo::getAlbumId,trackInfoVo.getAlbumId());
		//根据声音id降序
		wrapper.orderByDesc(TrackInfo::getId);
		//获取第一条记录
		wrapper.last(" limit 1");
		//调用
		TrackInfo trackInfo_ordernum = trackInfoMapper.selectOne(wrapper);
		int orderNum=1;
		if(null!=trackInfo_ordernum){
			orderNum=trackInfo_ordernum.getOrderNum()+1;
		}
		//最大值+1
		trackInfo.setOrderNum(orderNum);

		 //声音其他信息，比如时长，大小，类型等
		 //这些信息到腾讯云查询
		 //根据声音的mediaFileId调用腾讯云方法获取
			TrackMediaInfoVo trackMediaInfoVo =
					vodService.getmediaaInfoByFileId(trackInfoVo.getMediaFileId());
		//设置到trackInfo
		trackInfo.setMediaDuration(trackMediaInfoVo.getDuration());
		trackInfo.setMediaSize(trackMediaInfoVo.getSize());
		trackInfo.setMediaUrl(trackMediaInfoVo.getMediaUrl());
		trackInfo.setMediaType(trackMediaInfoVo.getType());
		//调用方法添加
		trackInfoMapper.insert(trackInfo);

		//2.操作专辑表，修改专辑里面声音数量值，+1
		 //根据专辑id查询专辑原始数据
		AlbumInfo albumInfo = albumInfoMapper.selectById(trackInfoVo.getAlbumId());
		Integer includeTrackCount = albumInfo.getIncludeTrackCount();
		//把原始数量+1
		albumInfo.setIncludeTrackCount(includeTrackCount+1);
		//调用方法更新
		albumInfoMapper.updateById(albumInfo);

		//3 添加声音四个统计数据，初始值 track_stat
		this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_PLAY);
		this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_COLLECT);
		this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_PRAISE);
		this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_COMMENT);
	}
		/**
		 * 初始化统计数量
		 * @param trackId
		 * @param trackType
		 */
		private void saveTrackStat(Long trackId, String trackType) {
			TrackStat trackStat = new TrackStat();
			trackStat.setTrackId(trackId);
			trackStat.setStatType(trackType);
			trackStat.setStatNum(0);
			this.trackStatMapper.insert(trackStat);
		}
}
