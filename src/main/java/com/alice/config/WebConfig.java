package com.alice.project.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import lombok.RequiredArgsConstructor;

//외부경로에 있는 리소스를 접근할 수 있는 방법
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	
//	private final NotificationInterceptor notificationInterceptor;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/upload/**") // 리소스와 연결될 URL path를 지정
				.addResourceLocations("file:///C:/Temp/upload/"); // 실제 리소스가 존재하는 외부 경로를 지정
	}

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        List<String> staticResourcesPath = Arrays.stream(StaticResourceLocation.values())
//                .flatMap(StaticResourceLocation::getPatterns)
//                .collect(Collectors.toList());
//        staticResourcesPath.add("/node_modules/**");
//
//        registry.addInterceptor(notificationInterceptor)
//            .excludePathPatterns(staticResourcesPath);
//    }
}
