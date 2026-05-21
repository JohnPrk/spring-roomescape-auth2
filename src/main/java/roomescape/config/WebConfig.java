package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AdminInterceptor;
import roomescape.auth.AuthInterceptor;
import roomescape.auth.LoginMemberArgumentResolver;
import roomescape.auth.ManagerInterceptor;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final ManagerInterceptor managerInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    public WebConfig(
            AuthInterceptor authInterceptor,
            AdminInterceptor adminInterceptor,
            ManagerInterceptor managerInterceptor,
            LoginMemberArgumentResolver loginMemberArgumentResolver
    ) {
        this.authInterceptor = authInterceptor;
        this.adminInterceptor = adminInterceptor;
        this.managerInterceptor = managerInterceptor;
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/my").setViewName("my-reservation");
        registry.addViewController("/popular").setViewName("popular");
        registry.addViewController("/admin").setViewName("admin/index");
        registry.addViewController("/admin/").setViewName("admin/index");
        registry.addViewController("/admin/reservation").setViewName("admin/reservation");
        registry.addViewController("/admin/theme").setViewName("admin/theme");
        registry.addViewController("/admin/time").setViewName("admin/time");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/members/me",
                        "/reservations",
                        "/reservations/me/**"
                );

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");

        registry.addInterceptor(managerInterceptor)
                .addPathPatterns("/manager/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
