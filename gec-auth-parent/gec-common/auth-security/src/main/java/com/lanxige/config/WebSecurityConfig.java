package com.lanxige.config;

import com.lanxige.Filter.TokenAuthenticationFliter;
import com.lanxige.Filter.TokenLoginFliter;
import com.lanxige.service.SysLoginService;
import com.lanxige.util.CustomMd5PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomMd5PasswordEncoder customMd5PasswordEncoder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SysLoginService sysLoginService;

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public TokenLoginFliter tokenLoginFliter() throws Exception {
        TokenLoginFliter filter = new TokenLoginFliter(
                authenticationManager(),
                redisTemplate,
                sysLoginService
        );
        // 这里可以设置过滤器的其他属性（如拦截路径）
        filter.setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/admin/system/index/login", "POST")
        );
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 这是配置的关键，决定哪些接口开启防护，哪些接口绕过防护
        http
                //关闭csrf
                .csrf().disable()

                // 开启跨域以便前端调用接口
                .cors().and()

                .authorizeRequests()
                .antMatchers("/admin/system/upload/uploadImage").permitAll()
                .antMatchers("/admin/system/upload/uploadVideo").permitAll()
                .antMatchers("/admin/system/sysCategory/findAll").permitAll()
                // 指定某些接口不需要通过验证即可访问。登陆接口肯定是不需要认证的
                .antMatchers("/admin/system/index/login").permitAll()
                .antMatchers("/admin/system/index/changePwd").permitAll()
                .antMatchers("/admin/system/sysMovie/getAllVideoInfo").permitAll()
                .antMatchers("/admin/system/sysMovie/getHotWatchVideoInfo").permitAll()
                .antMatchers("/admin/system/sysMovie/getHotVideoInfo").permitAll()
                .antMatchers("/admin/system/sysDate/singelMovieDetail").permitAll()
                .antMatchers("/admin/system/sysUser/registerUser").permitAll()

                // 这里意思是其它所有接口需要认证才能访问
                .anyRequest().authenticated()

                .and()
                //TokenAuthenticationFilter放到UsernamePasswordAuthenticationFilter的前面，这样做就是为了除了登录的时候去查询数据库外，其他时候都用token进行认证。
                .addFilterBefore(new TokenAuthenticationFliter(redisTemplate), UsernamePasswordAuthenticationFilter.class)
                .addFilter(tokenLoginFliter());
        //禁用session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 指定UserDetailService和加密器
        auth.userDetailsService(userDetailsService).passwordEncoder(customMd5PasswordEncoder);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/favicon.ico", "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**", "/doc.html");
    }
}
