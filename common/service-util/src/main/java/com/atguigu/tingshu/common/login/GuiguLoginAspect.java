package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class GuiguLoginAspect {
    @Autowired
    private RedisTemplate redisTemplate;
        //ProceedingJoinPoint:获取被增强方法信息和被增强方法执行
        //使用环绕通知 @Around(切入点表达式)
        @Around("execution(* com.atguigu.tingshu.*.api.*.*(..)) && @annotation(guiguLogin)")
        public Object login(ProceedingJoinPoint joinPoint,
                GuiguLogin guiguLogin)throws Throwable{
            //RequestContextHolder 上下文对象 获取request对象获取
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes attributes=(ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = attributes.getRequest();
        //1、从请求头获取token(前端传递过来的)
        String token = request.getHeader("token");

        //2、根据token查询redis(redis的key是token)
        // 如果可以查询到是登录，查询不到不是登录
        //因为GuiguLogin注解有属性 required
        //这个属性 required==true表示必须登录，required==false可以登录可以不登录(比如首页面)
        boolean isRequired = guiguLogin.required();
        if(isRequired) {//表示必须登录
            //判断请求头token是否为空，如果为空，返回登录提示信息
            if (StringUtils.isEmpty(token)) {
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
            //如果token不为空，根据token查询redis，判断查询数据是否为空，如果为空，返回登录提示信息
            UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(token);
            //如果为空，返回登录提示信息
            if (userInfo == null) {
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
        }
        if (!StringUtils.isEmpty(token)){
            //组成缓存key
            //String loginKey=RedisConstant.USER_LOGIN_KEY_PREFIX+token;
            //获取缓存中用户数据
            UserInfo userInfo=(UserInfo) this.redisTemplate.opsForValue().get(token);
            if (null !=userInfo){
                AuthContextHolder.setUserId(userInfo.getId());
            }
        }
            //登录成功,执行被增强的方法
        Object obj=joinPoint.proceed();
        return obj;
    }
}
