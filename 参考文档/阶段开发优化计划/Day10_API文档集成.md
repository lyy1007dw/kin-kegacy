# Day 10 - API 文档集成

**开发日期**：第 10 天
**优先级**：P2（中等）
**所属阶段**：质量提升

---

## 一、功能需求描述

### 1.1 背景

系统需要集成 Swagger/Knife4j API 文档，生成可交互的接口文档，方便前后端开发和调试。

### 1.2 目标

- 集成 Knife4j 文档
- 配置 API 分组
- 添加认证支持
- 完善接口注释

---

## 二、Knife4j 集成

### 2.1 依赖引入

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi2-spring-boot-starter</artifactId>
    <version>4.3.0</version>
</dependency>
```

### 2.2 配置

```yaml
# application.yml
knife4j:
  enable: true
  setting:
    language: zh_CN
    swagger-model-name: 实体类
  basic:
    enable: true
    username: admin
    password: admin123

springfox:
  documentation:
    enabled: true
    swagger-v2:
      path: /api-docs
    group-name: kin-legacy
    info:
      title: 传承谱 API 文档
      description: 家谱管理系统后端接口文档
      version: 1.0.0
      contact:
        name: lyy1007dw
        email: example@email.com
```

### 2.3 配置类

```java
@Configuration
@EnableOpenApi
public class Knife4jConfig {
    
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .groupName("传承谱 API")
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.kin.family.controller"))
            .paths(PathSelectors.any())
            .build()
            .securitySchemes(securitySchemes())
            .securityContexts(securityContexts());
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("传承谱 - API 文档")
            .description("家谱管理系统后端接口文档")
            .contact(new Contact("lyy1007dw", "", "example@email.com"))
            .version("1.0.0")
            .build();
    }
    
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> list = new ArrayList<>();
        // Bearer Token
        list.add(new ApiKey("Authorization", "Authorization", "header"));
        return list;
    }
    
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> list = new ArrayList<>();
        list.add(SecurityContext.builder()
            .securityReferences(defaultAuth())
            .build());
        return list;
    }
    
    private List<SecurityReference> defaultAuth() {
        List<SecurityReference> list = new ArrayList<>();
        list.add(new SecurityReference("Authorization", 
            new AuthorizationScope[] {new AuthorizationScope("global", "全局访问")}));
        return list;
    }
}
```

---

## 三、接口注解

### 3.1 Controller 注解

```java
@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    // ...
}
```

### 3.2 接口注解

```java
@ApiOperation("用户登录")
@PostMapping("/login")
public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    // ...
}

@ApiOperation("获取当前用户信息")
@ApiImplicitParam(name = "Authorization", value = "Token", 
    required = true, paramType = "header", dataType = "String")
@GetMapping("/user-info")
@RequireLogin
public Result<UserInfoResponse> getUserInfo() {
    // ...
}
```

### 3.3 参数注解

```java
@ApiModel(description = "登录请求")
@Data
public class LoginRequest implements Serializable {
    
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @ApiModelProperty(value = "验证码")
    private String captcha;
    
    @ApiModelProperty(value = "验证码Key")
    private String captchaKey;
}
```

---

## 四、分组配置

### 4.1 多分组

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("管理员接口")
            .apiInfo(apiInfo("管理员接口", "后台管理系统相关接口"))
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.kin.family.controller.admin"))
            .paths(PathSelectors.ant("/api/admin/**"))
            .build();
    }
    
    @Bean
    public Docket familyApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("家谱接口")
            .apiInfo(apiInfo("家谱接口", "家谱管理相关接口"))
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.kin.family.controller"))
            .paths(PathSelectors.ant("/api/family/**"))
            .build();
    }
    
    @Bean
    public Docket authApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("认证接口")
            .apiInfo(apiInfo("认证接口", "登录认证相关接口"))
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.kin.family.controller"))
            .paths(PathSelectors.ant("/api/auth/**"))
            .build();
    }
}
```

### 4.2 访问地址

- Knife4j UI: `/doc.html`
- Swagger UI: `/swagger-ui.html`
- API JSON: `/v2/api-docs`

---

## 五、安全配置

### 5.1 放行文档相关路径

```java
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/login",
                "/api/auth/wx-login",
                "/api/auth/refresh",
                "/doc.html",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/v3/api-docs/**",
                "/webjars/**"
            );
    }
}
```

---

## 六、常用注解参考

### 6.1 常用注解

| 注解 | 说明 |
|------|------|
| @Api | 标记Controller类 |
| @ApiOperation | 标记接口方法 |
| @ApiParam | 标记参数 |
| @ApiModel | 标记实体类 |
| @ApiModelProperty | 标记实体属性 |
| @ApiImplicitParam | 标记隐式参数 |
| @ApiResponse | 标记响应 |

### 6.2 完整示例

```java
@Api(tags = "成员管理")
@RestController
@RequestMapping("/api/family/{familyId}/member")
@RequiredArgsConstructor
public class MemberController {
    
    @ApiOperation("获取成员列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "familyId", value = "家谱ID", 
            required = true, paramType = "path", dataType = "Long"),
        @ApiImplicitParam(name = "page", value = "页码", 
            defaultValue = "1", paramType = "query", dataType = "Integer"),
        @ApiImplicitParam(name = "size", value = "每页条数", 
            defaultValue = "20", paramType = "query", dataType = "Integer")
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未登录"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "家谱不存在")
    })
    @GetMapping
    public Result<PageResult<MemberResponse>> getMembers(
            @PathVariable Long familyId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // ...
        return Result.success(result);
    }
    
    @ApiOperation("新增成员")
    @PostMapping
    @RequireAdmin
    public Result<MemberResponse> addMember(
            @PathVariable Long familyId,
            @Valid @RequestBody AddMemberRequest request) {
        // ...
        return Result.success(member);
    }
}
```

---

## 七、交付物

- [ ] Knife4j 依赖引入
- [ ] Swagger 配置类
- [ ] 各 Controller 接口注解添加
- [ ] 各 Request/Response 类注解添加
- [ ] 安全配置放行

---

## 八、注意事项

1. 生产环境禁用文档或添加访问密码
2. 敏感接口文档中
3不暴露在. 定期更新接口注释
4. 保持与代码同步
