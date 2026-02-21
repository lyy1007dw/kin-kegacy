# Day 8 - 单元测试编写

**开发日期**：第 8 天
**优先级**：P2（中等）
**所属阶段**：质量提升

---

## 一、功能需求描述

### 1.1 背景

项目需要完善的单元测试覆盖，确保核心业务逻辑的正确性，为后续迭代提供安全保障。

### 1.2 目标

- Service 层业务逻辑测试覆盖
- Controller 层接口测试
- 测试覆盖率目标：60%+
- 使用 JUnit 5 + Mockito

---

## 二、测试框架配置

### 2.1 依赖引入

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 2.2 测试配置

```yaml
# test/resources/application.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
```

---

## 三、Service 层测试

### 3.1 用户服务测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordService passwordService;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("测试用户");
        
        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(passwordService.encode("password123")).thenReturn("$2a$10$xxx");
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        // Act
        Result<?> result = userService.register(request);
        
        // Assert
        assertEquals(200, result.getCode());
        verify(userMapper).insert(any(User.class));
    }
    
    @Test
    void testRegister_UsernameExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        when(userMapper.selectByUsername("existinguser")).thenReturn(existingUser);
        
        // Act
        Result<?> result = userService.register(request);
        
        // Assert
        assertEquals(400, result.getCode());
        assertEquals("用户名已存在", result.getMessage());
    }
    
    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("$2a$10$xxx");
        user.setGlobalRole("SUPER_ADMIN");
        
        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordService.matches("123456", "$2a$10$xxx")).thenReturn(true);
        
        // Act
        Result<?> result = userService.login(request);
        
        // Assert
        assertEquals(200, result.getCode());
    }
    
    @Test
    void testLogin_WrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");
        
        User user = new User();
        user.setPassword("$2a$10$xxx");
        
        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordService.matches("wrong", "$2a$10$xxx")).thenReturn(false);
        
        // Act
        Result<?> result = userService.login(request);
        
        // Assert
        assertEquals(401, result.getCode());
    }
}
```

### 3.2 家谱服务测试

```java
@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {
    
    @Mock
    private FamilyMapper familyMapper;
    
    @Mock
    private UserGenealogyMapper userGenealogyMapper;
    
    @InjectMocks
    private FamilyServiceImpl familyService;
    
    @Test
    void testCreateFamily_Success() {
        // Arrange
        CreateFamilyRequest request = new CreateFamilyRequest();
        request.setName("张氏家谱");
        request.setDescription("张氏家族");
        
        Long userId = 1L;
        
        when(familyMapper.insert(any(Family.class))).thenReturn(1);
        when(userGenealogyMapper.insert(any(UserGenealogy.class))).thenReturn(1);
        
        // Act
        Result<?> result = familyService.createFamily(request, userId);
        
        // Assert
        assertEquals(200, result.getCode());
        verify(familyMapper).insert(any(Family.class));
        verify(userGenealogyMapper).insert(any(UserGenealogy.class));
    }
    
    @Test
    void testDeleteFamily_OnlyAdmin() {
        // Arrange
        Long familyId = 1L;
        Long userId = 1L;
        
        when(userGenealogyMapper.countAdmins(familyId)).thenReturn(1);
        when(userGenealogyMapper.selectByUserAndGenealogy(userId, familyId))
            .thenReturn(buildUserGenealogy(userId, familyId, "ADMIN"));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> familyService.deleteFamily(familyId, userId));
        assertEquals("该用户是该家谱的唯一管理员，无法删除", exception.getMessage());
    }
}
```

### 3.3 成员服务测试

```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    
    @Mock
    private FamilyMemberMapper memberMapper;
    
    @Mock
    private MemberRelationMapper relationMapper;
    
    @InjectMocks
    private MemberServiceImpl memberService;
    
    @Test
    void testAddMember_Success() {
        // Arrange
        AddMemberRequest request = new AddMemberRequest();
        request.setName("张三");
        request.setGender("男");
        request.setBirthDate("1990-01-01");
        
        Long familyId = 1L;
        Long userId = 1L;
        
        when(memberMapper.insert(any(FamilyMember.class))).thenReturn(1);
        
        // Act
        Result<?> result = memberService.addMember(request, familyId, userId);
        
        // Assert
        assertEquals(200, result.getCode());
    }
    
    @Test
    void testUpdateMember_WithTransfer() {
        // Arrange
        Long memberId = 1L;
        Long sourceFamilyId = 1L;
        Long targetFamilyId = 2L;
        
        FamilyMember member = new FamilyMember();
        member.setId(memberId);
        member.setGenealogyId(sourceFamilyId);
        
        when(memberMapper.selectById(memberId)).thenReturn(member);
        when(relationMapper.countByMemberId(memberId)).thenReturn(3);
        
        // Act
        TransferCheckVO result = memberService.checkTransfer(memberId, targetFamilyId);
        
        // Assert
        assertTrue(result.getCanTransfer());
        assertEquals(3, result.getAffectedRelations());
    }
}
```

---

## 四、Controller 层测试

### 4.1 认证控制器测试

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginResponse response = LoginResponse.builder()
            .accessToken("test-token")
            .refreshToken("refresh-token")
            .userInfo(buildUserInfo())
            .build();
        
        when(authService.login(any(LoginRequest.class))).thenReturn(Result.success(response));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("test-token"));
    }
    
    @Test
    void testLogin_ValidationError() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testRefreshToken_Success() throws Exception {
        // Arrange
        when(authService.refreshToken(anyString()))
            .thenReturn(Result.success(buildTokenResponse()));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"test-refresh-token\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists());
    }
}
```

### 4.2 家谱控制器测试

```java
@WebMvcTest(FamilyController.class)
class FamilyControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FamilyService familyService;
    
    @Test
    @WithMockUser(roles = "USER")
    void testGetFamilyList_Success() throws Exception {
        // Arrange
        when(familyService.getFamilyList(any()))
            .thenReturn(Result.success(buildPageResult()));
        
        // Act & Assert
        mockMvc.perform(get("/api/family")
                .param("page", "1")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testCreateFamily_WithoutAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/family")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"测试家谱\"}"))
            .andExpect(status().isForbidden());
    }
}
```

---

## 五、集成测试

### 5.1 数据库集成测试

```java
@SpringBootTest
class FamilyRepositoryTest {
    
    @Autowired
    private FamilyRepository familyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testSaveAndFind() {
        // Create user
        User user = User.builder()
            .username("testuser")
            .password("encrypted")
            .nickname("测试用户")
            .build();
        user = userRepository.save(user);
        
        // Create family
        Family family = Family.builder()
            .name("测试家谱")
            .code("TEST001")
            .createdBy(user.getId())
            .build();
        family = familyRepository.save(family);
        
        // Assert
        Optional<Family> found = familyRepository.findById(family.getId());
        assertTrue(found.isPresent());
        assertEquals("测试家谱", found.get().getName());
    }
}
```

### 5.2 事务测试

```java
@SpringBootTest
class TransactionTest {
    
    @Autowired
    private FamilyService familyService;
    
    @Test
    void testRollbackOnException() {
        // Arrange
        CreateFamilyRequest request = new CreateFamilyRequest();
        request.setName("测试家谱");
        
        Long userId = 999L; // 不存在的用户
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            familyService.createFamily(request, userId);
        });
        
        // 验证家谱未被创建
        assertEquals(0, familyRepository.count());
    }
}
```

---

## 六、测试覆盖率配置

### 6.1 Maven 配置

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>CLASS</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.60</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 6.2 运行测试

```bash
# 运行测试并生成覆盖率报告
mvn clean test

# 生成 Jacoco 报告
mvn jacoco:report

# 查看报告
# target/site/jacoco/index.html
```

---

## 七、测试用例清单

### 7.1 Service 层

| 测试类 | 测试方法 | 覆盖率目标 |
|--------|----------|-------------|
| UserServiceTest | register, login, logout, updateInfo | 80% |
| FamilyServiceTest | create, update, delete, getList | 75% |
| MemberServiceTest | add, update, delete, getTree | 70% |
| AuthServiceTest | login, refresh, logout | 85% |
| ApprovalServiceTest | submit, approve, reject | 75% |

### 7.2 Controller 层

| 测试类 | 测试方法 | 覆盖率目标 |
|--------|----------|-------------|
| AuthControllerTest | login, logout, refresh, userInfo | 70% |
| FamilyControllerTest | CRUD | 65% |
| MemberControllerTest | CRUD | 65% |
| ApprovalControllerTest | list, handle | 70% |

---

## 八、交付物

- [ ] Service 层单元测试（UserService, FamilyService, MemberService）
- [ ] Controller 层测试（AuthController, FamilyController）
- [ ] 集成测试配置
- [ ] Jacoco 覆盖率配置
- [ ] 测试报告

---

## 九、注意事项

1. 测试数据使用 @BeforeEach 初始化
2. 使用 @Transactional 确保测试后数据回滚
3. 避免测试间的数据依赖
4. Mock 外部依赖（Redis, 第三方 API）
