package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {

	@Autowired
	private AlbumInfoService albumInfoService;

	/**
	 * 获取当前用户专辑列表
	 */
	/**
	 * 获取当前用户专辑列表
	 * @return
	 */
	@Operation(summary = "获取当前用户全部专辑列表")
	@GetMapping("findUserAllAlbumList")
	public Result findUserAllAlbumList() {
		Long userId=1L;	//TODO 后续完善
		List<AlbumInfo> list = albumInfoService.findUserAllAlbumList(userId);
		return Result.ok(list);
	}

	//修改接口
	//Request URL：http://Localhost/api/album/albumInfo/updateAlbumInfo/1607
	//Request Method：Put
	@Operation(summary = "修改专辑")
	@PutMapping("/updateAlbumInfo/{albumId}")
	public Result updateById(@PathVariable Long albumId,
							 @RequestBody @Validated AlbumInfoVo albumInfoVo){
		//	调用服务层方法
		albumInfoService.updateAlbumInfo(albumId,albumInfoVo);
		return Result.ok();
	}

	//修改-根据专辑id获取专辑数据
	//Request URL:http://Localhost/api/album/albumInfo/getALlbumInfo/1599
	//Request Method：GET
	@GetMapping("getAlbumInfo/{albumId}")
	public Result<AlbumInfo> getAlbumInfo(@PathVariable Long albumId){
		AlbumInfo albumInfo=albumInfoService.getAlbumInfo(albumId);
		return Result.ok(albumInfo);
	}

	//删除专辑信息
	@Operation(summary = "删除专辑信息")
	@DeleteMapping("removeAlbumInfo/{id}")
	public Result removeAlbumInfoById(@PathVariable Long id) {
		albumInfoService.removeAlbumInfoById(id);
		return Result.ok();
	}

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

