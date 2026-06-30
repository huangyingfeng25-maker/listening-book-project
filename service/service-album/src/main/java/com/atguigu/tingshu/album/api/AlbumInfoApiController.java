package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {

	@Autowired
	private AlbumInfoService albumInfoService;

	//查询专辑列表
	@PostMapping("findUserAlbumPage/{page}/{limit}")
	public Result findUserAlbumPage(@Parameter(name="page",description = "当前页码", required = true)
									@PathVariable Long page,
									@Parameter(name = "limit", description = "每页记录数", required = true)
									@PathVariable Long limit,
									@Parameter(name = "albumInfoQuery", description = "查询对象", required = false)
									@RequestBody AlbumInfoQuery albumInfoQuery){
		albumInfoQuery.setUserId(1L);
		//创建Page对象，传递当前页和每页记录数
		Page<AlbumListVo> pageParam=new Page<>(page,limit);
		//调用service方法
		IPage<AlbumListVo> pageModel=albumInfoService.selectAlbumPage(pageParam,albumInfoQuery);
		return Result.ok(pageModel);

	}

	/**
	 * 新增专辑方法
	 * @param albumInfoVo
	 * @return
	 */
	//保存专辑
	@PostMapping("saveAlbumInfo")
	public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo){
		albumInfoService.saveAlbumInfo(albumInfoVo);
		return Result.ok();
	}


}

