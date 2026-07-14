package com.atguigu.tingshu.user.api;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.service.RabbitService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitService rabbitService;

    /**
     * 更新用户信息
     * @param userInfoVo
     * @return
     */
    @GuiguLogin
    @Operation(summary = "更新用户信息")
    @PostMapping("updateUser")
    public Result updateUser(@RequestBody UserInfoVo userInfoVo){
        // 1. 获取当前登录用户的 ID（从上下文）
        Long userId = AuthContextHolder.getUserId();

        // 2. 创建 UserInfo 实体对象（DO/Entity）
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);                     // 设置主键 ID
        userInfo.setNickname(userInfoVo.getNickname());   // 更新昵称
        userInfo.setAvatarUrl(userInfoVo.getAvatarUrl()); // 更新头像 URL

        // 3. 调用 Service 层执行更新
        userInfoService.updateById(userInfo);  // 假设是 MyBatis-Plus 的 updateById
        // 4. 返回成功响应
        return Result.ok();
    }

    @GuiguLogin
    //获取当前用户的登录信息
    @GetMapping("getUserInfo")
    public Result getUserInfo(){
        //获取当前登录用户的id
        Long userId = AuthContextHolder.getUserId();
        UserInfo userInfo = userInfoService.getById(userId);
        //创建UserInfoVo对象
        UserInfoVo userInfoVo=new UserInfoVo();
        //属性拷贝
        BeanUtils.copyProperties(userInfo,userInfoVo);
        return Result.ok(userInfoVo);
    }


    //http://localhost/api/user/wxLogin/wxLogin/0a1NLdHa1Gbn2M0HYiIa1RXlxx1NLdH4
    //Request Method: GET
    //code前端传递过来
    @GetMapping("/wxLogin/{code}")
    public Result wxLogin(@PathVariable("code")String code) throws WxErrorException {
        //1 拿着code+ 微信公众平台id + 秘钥 请求微信服务器接口，返回openid
        WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
        String openid = sessionInfo.getOpenid();

        //2 根据openid判断是否第一次登录
        LambdaQueryWrapper<UserInfo>wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getWxOpenId,openid);
        UserInfo userInfo = userInfoService.getOne(wrapper);
        //如果第一次登录，添加用户信息，发送mq消息初始化账户
        if(userInfo==null){
            //添加用户信息
            userInfo=new UserInfo();
            //赋值用户昵称
            userInfo.setNickname("听友"+System.currentTimeMillis());
            //赋值用户头像图片
            userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            //openId
            userInfo.setWxOpenId(openid);
            //调用方法添加
            userInfoService.save(userInfo);

            //发送mq异步消息初始化账户
            rabbitService.sendMessage(MqConst.EXCHANGE_USER,
                                     MqConst.ROUTING_USER_REGISTER,
                                    userInfo.getId());

        }
        //3 生成token，把数据放到redis里面，
        // redis的key是token，value是用户信息，设置redis过期时间
        String token= UUID.randomUUID().toString().replaceAll("-","");
            //把数据放到redis里面，
         redisTemplate.opsForValue().set(token,userInfo,30, TimeUnit.MINUTES);

        //4 返回token
        HashMap<String,Object> map = new HashMap<>();
        map.put("token",token);
        //返回数据
        return Result.ok(map);
    }
}
