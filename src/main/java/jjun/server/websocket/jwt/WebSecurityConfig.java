package jjun.server.websocket.jwt;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers()
                .frameOptions().sameOrigin()

                .and()
                .formLogin()

                .and()
                .authorizeHttpRequests()
                .antMatchers("/chat/**").hasRole("USER")
                .anyRequest().permitAll();
    }

    /**
     * 테스트를 위해 내장 메모리에 임의로 여러 개의 계정 생성
     * -> // TODO 실제 서비스에 구현 시에는 DB의 User/Profile 데이터를 이용하도록 수정하기
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()

                .withUser("jjuni")
                .password("{noop}1234")
                .roles("USER")

                .and()
                .withUser("channi")
                .password("{noop}1234")
                .roles("USER")

                .and()
                .withUser("wak")
                .password("{noop}1234")
                .roles("GUEST");
    }
}
