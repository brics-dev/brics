package gov.nih.tbi.api.query.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gov.nih.tbi.api.query.security.jwt.JWTFilter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("gov.nih.tbi.api.query.controller"))
				.paths(PathSelectors.any()).build().apiInfo(apiEndPointsInfo()).securitySchemes(securitySchemes());
	}
	
	private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Query Tool Rest API")
            .description("This is an API to access data from query tool.")
            .contact(new Contact("Francis Chen", "", "fchen@sapient.com"))
            .license("Apache 2.0")
            .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
            .version("1.0.0")
            .build();
    }
	
	private List<SecurityScheme> securitySchemes() {
		List<SecurityScheme> schemes = new ArrayList<> ();
	    schemes.add(new ApiKey("bearerAuth", JWTFilter.AUTHORIZATION_HEADER, "header"));
	    return schemes;
	}
}
