package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.AlbumAttributeValueService;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

	@Autowired
	private AlbumInfoMapper albumInfoMapper;
    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;
	@Autowired
	private AlbumStatMapper albumStatMapper;
	@Autowired
	private AlbumAttributeValueService albumAttributeValueService;
	@Autowired
	private TrackInfoMapper trackInfoMapper;

	//保存专辑
	//当前操作多张表，保证多张表数据一致性，添加事务
	//当前这些表在一个数据库里面，这种事务称为本地事务
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveAlbumInfo(AlbumInfoVo albumInfoVo) {
		//1.添加专辑的基本信息album_info
		AlbumInfo albumInfo=new AlbumInfo();
		//AlbumInfoMapper 值--放到 AlbumInfo
		//String albumTitle = albumInfoVo.getAlbumTitle();
		//albumInfo.setAlbumTitle(albumTitle);
		BeanUtils.copyProperties(albumInfoVo,albumInfo);

		//专辑有几个值需要单独设置，前端没有传递过来的
		//TODO userId用户id，后面完善
		albumInfo.setUserId(1L);

		//专辑状态，设置通过
		albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS);
		//设置收费专辑，免费试听集数，前3集
		String payType = albumInfo.getPayType();
		if(!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(payType)){
			albumInfo.setTracksForFree(3);
		}
		//调用方法保存
		albumInfoMapper.insert(albumInfo);

		//2.添加专辑下面的标签名称和标签值数据album_attribute_value
		List<AlbumAttributeValueVo> albumAttributeValueVoList =
				albumInfoVo.getAlbumAttributeValueVoList();

		//List<AlbumAttributeValueVo> -> List<AlbumAttributeValue>
		if(!CollectionUtils.isEmpty(albumAttributeValueVoList)) {
			List<AlbumAttributeValue> albumAttributeValueList =
					albumAttributeValueVoList.stream().map(albumAttributeValueVo -> {
						AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
						BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);
						albumAttributeValue.setAlbumId(albumInfo.getId());
						return albumAttributeValue;
					}).collect(Collectors.toList());

			albumAttributeValueService.saveBatch(albumAttributeValueList);
		}


		//3 添加专辑四个统计数据 播放量、订阅量等 初始值 0 album_stat
		//播放量
		this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_PLAY);
		//订阅量
		this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_SUBSCRIBE);
		//购买量
		this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_BROWSE);
		//评论数
		this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_COMMENT);

	}

	//查询专辑列表
	@Override
	public IPage<AlbumListVo> selectAlbumPage(Page<AlbumListVo> pageParam, AlbumInfoQuery albumInfoQuery) {
		return albumInfoMapper.selectUserAlbumPage(pageParam,albumInfoQuery);
	}

	//删除专辑信息
	@Override
	public void removeAlbumInfoById(Long albumId) {
		//1.判断当前专辑下面是否包含声音，如果包含不能删除
		//select count(*) from track_info where album_id=?
		LambdaQueryWrapper<TrackInfo> queryWrapper=new LambdaQueryWrapper<>();
		queryWrapper.eq(TrackInfo::getAlbumId,albumId);
		Long count = trackInfoMapper.selectCount(queryWrapper);
		if(count>0){
			throw new GuiguException(400,"该专辑下存在未删除的声音");
		}
		//2.1删除专辑的基本数据
		albumInfoMapper.deleteById(albumId);
		//2.2删除专辑标签名和标签数据
		LambdaQueryWrapper<AlbumAttributeValue>queryWrapper1= new LambdaQueryWrapper<>();
		queryWrapper1.eq(AlbumAttributeValue::getAlbumId,albumId);
		albumAttributeValueMapper.delete(queryWrapper1);

		//2.3删除专辑4个统计数据
		LambdaQueryWrapper<AlbumStat>queryWrapper2=new LambdaQueryWrapper<>();
		queryWrapper2.eq(AlbumStat::getAlbumId,albumId);
		albumStatMapper.delete(queryWrapper2);
	}

	//保存专辑统计数据的方法
	public void saveAlbumStat(Long albumId,String statType){
		AlbumStat albumStat=new AlbumStat();
		albumStat.setAlbumId(albumId);
		albumStat.setStatType(statType);
		albumStat.setStatNum(0);
		albumStatMapper.insert(albumStat);

	}
}
