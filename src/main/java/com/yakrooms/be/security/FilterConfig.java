package com.yakrooms.be.security;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<JwtFilter> jwtFilter() {
		FilterRegistrationBean<JwtFilter> registrationBean= new FilterRegistrationBean();
		registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/*"); // Apply to all URLs
        return registrationBean;
	}
}
