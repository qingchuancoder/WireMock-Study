## Background

I developed a microservice that calls another unfinished microservice via OpenFeign, so I can't do integration testing. However, a colleague suggested that WireMock could be used to simulate this microservice so that we could do integration testing during the development phase, so let's take a look at WireMock.

我开发的一个微服务，它通过OpenFeign调用了另一个尚未完成微服务，因此我就无法进行集成测试。不过，有一个同事提出可以使用WireMock来模拟这个微服务，这样我们就可以在开发阶段进行集成测试了，那么让我们来看看WireMock吧。

## What is WireMock

WireMock is a popular open-source tool for API mock testing. Stubbing involves creating stubs or set responses to API calls that mimic real API behavior without the need for actual server communication.

WireMock 是一种流行的 API 模拟测试开源工具。模拟涉及创建存根或设置 API 调用的响应，以模仿真实的 API 行为，而无需实际的服务器通信。

### WireMock Benefits for Developers

- Simulate the APIs of microservices and apps
  模拟微服务和应用程序的 API

- Create stable test and dev environments
  创建稳定的测试和开发环境

- Isolate development from flakey 3rd parties
  将开发与不稳定的第三方隔离开来

- Mock APIs that don’t exist (or aren’t ready)
  不存在（或尚未准备好）的模拟 API

[What is WireMock?](https://www.wiremock.io/what-is-wiremock)

## QuickStart

Since my microservice is a Spring Boot project, I will only discuss WireMock Spring Boot Integration here.

因为我的微服务是一个SpringBoot项目，所以这里我只讲WireMock Spring Boot Integration.

1. Installation

```xml
<dependency>
    <groupId>org.wiremock.integrations</groupId>
    <artifactId>wiremock-spring-boot</artifactId>
    <version>3.10.0</version>
</dependency>
```

2. Add configurations

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          user-service:
            url: ${wiremock.server.baseUrl}
```

3. Basic usage

```
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock
class UserControllerWireMockProgrammingTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @InjectWireMock
    private WireMockServer wireMock;
    @Value("${wiremock.server.baseUrl}")
    private String wiremockBaseUrl;

    @Test
    public void createUserShouldSuccess() throws Exception {
        User user = User.builder().name("test").age(18).build();
        wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": true,
                                    "code": 200,
                                    "msg": "Success",
                                    "data": {
                                        "id": 1,
                                        "name": "test",
                                        "age": 18
                                    }
                                }
                                """)));
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("test"))
                .andExpect(jsonPath("$.data.age").value(18));
        wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
    }
}
```

Yes, it's that simple. All you need to do is add @EnableWireMock annotations and use the stubFor method to simulate an API.

是的，就是这么简单。只需要加上@EnableWireMock注解，并使用stubFor方法就模拟一个API了。

However, in our actual development, a service API may have multiple RequestMethods, and I will list some common Stub Cases with RequestMethods.

不过，在我们实际的开发中，一个service的API可能会有多种RequestMethod，接下来我列举了一些常见RequestMethod的Stub Case。

## Stub Case

### RemoteService

```java
// RemoteService
@RestController
@RequestMapping("/v1")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final ObjectMapper objectMapper;

    @PostMapping(value = "user", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> create(@RequestBody @Validated User user) {
        log.info("create user: {}", user);
        var data = user.setId(1L);
        return ResponseEntity.ok(ResultResponse.success(data));
    }

    @GetMapping(value = "user", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> retrieve(@RequestParam Long id) {
        log.info("get user info, id: {}", id);
        var user = User.builder().id(1L).name("test").age(18).build();
        return ResponseEntity.ok(ResultResponse.success(user));
    }

    @PutMapping(value = "user", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> update(@RequestBody @Validated User user) {
        log.info("update user info, user: {}", user);
        return ResponseEntity.ok(ResultResponse.success(user.setId(1L).setName("updated")));
    }

    @DeleteMapping(value = "user", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<Void>> delete(@RequestParam Long id) {
        log.info("delete user, id: {}", id);
        return ResponseEntity.ok(ResultResponse.success());
    }
}
```

### FeignClient

```java
// FeignClient
@Service
@FeignClient(name = "user-service", path = "/v1")
public interface UserRemoteService {

    @PostMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<User>> createUser(@RequestBody User user);

    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<User>> retrieveUser(@RequestParam Long id);

    @PutMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<User>> updateUser(@RequestBody User user);

    @DeleteMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<Void>> deleteUser(@RequestParam Long id);
}
```

### POST SUCCESS

1. STUB

   ```java
   wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
           .willReturn(WireMock.aResponse()
                   .withStatus(200)
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("""
                           {
                               "success": true,
                               "code": 200,
                               "msg": "Success",
                               "data": {
                                   "id": 1,
                                   "name": "test",
                                   "age": 18
                               }
                           }
                           """)));
   ```

2. VERIFY

   ```JAVA
   wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
   ```

### POST FAILED

1. STUB

   ```java
   wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
           .willReturn(WireMock.aResponse()
                   .withStatus(500)
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("""
                           {
                                "success": false,
                                "code": 500,
                                "msg": "Connection refused: getsockopt executing POST ${wiremockBaseUrl}/v1/user",
                                "data": null
                            }
                           """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
   ```

2. VERIFY

   ```java
   wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
   ```

### GET SUCCESS

1. STUB

   ```java
   wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1"))
           .willReturn(WireMock.ok()
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("""
                           {
                               "success": true,
                               "code": 200,
                               "msg": "Success",
                               "data": {
                                   "id": 1,
                                   "name": "test",
                                   "age": 18
                               }
                           }
                           """)));
   ```

2. VERIFY

   ```
   wireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1")));
   ```

### GET FAILED

1. STUB

   ```java
   wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1"))
           .willReturn(WireMock.serverError()
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("""
                           {
                               "success": false,
                               "code": 500,
                               "msg": "Connection refused: getsockopt executing GET ${wiremockBaseUrl}/v1/user?id=1",
                               "data": null
                           }
                           """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
   ```

2. VERIFY

   ```java
   wireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1")));
   ```

### PUT SUCCESS

1. STUB

   ```java
   wireMock.stubFor(WireMock.put(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
           .willReturn(WireMock.ok("""
                           {
                               "success": true,
                               "code": 200,
                               "msg": "Success",
                               "data": {
                                   "id": 1,
                                   "name": "updated",
                                   "age": null
                               }
                           }
                           """)
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
   ```

2. VERIFY

   ```java
   wireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
   ```

### PUT FAILED

1. STUB

   ```java
   wireMock.stubFor(WireMock.put(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
           .willReturn(WireMock.serverError()
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("""
                           {
                               "success": false,
                               "code": 500,
                               "msg": "Connection refused: getsockopt executing PUT ${wiremockBaseUrl}/v1/user",
                               "data": null
                           }
                           """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
   ```

2. VERIFY

   ```
   wireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/v1/user"))
           .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
   ```

### DELETE SUCCESS

1. STUB

   ```java
   wireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1"))
           .willReturn(WireMock.okJson("""
                   {
                       "success": true,
                       "code": 200,
                       "msg": "Success",
                       "data": null
                   }
                   """)));
   ```

2. VERIFY

   ```java
   wireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1")));
   ```

### DELETE FAILED

1. STUB

   ```java
   wireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1"))
           .willReturn(WireMock.serverError()
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("""
                           {
                                "success": false,
                                "code": 500,
                                "msg": "Connection refused: getsockopt executing DELETE ${wiremockBaseUrl}/v1/user?id=1",
                                "data": null
                            }
                           """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
   ```

2. VERIFY

   ```java
   wireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/v1/user"))
           .withQueryParam("id", WireMock.equalTo("1"))
           .willReturn(WireMock.serverError()
                   .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .withBody("""
                           {
                               "success": false,
                               "code": 500,
                               "msg": "[500] during [DELETE] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#deleteUser(Long)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                               "data": null
                           }
                           """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
   ```

This is what I do with WireMock in my current development, but there is more to WireMock than that, if you are interested, please visit [WireMock User Documentation | WireMock](https://wiremock.org/docs/)

这就是我目前开发中有关于WireMock的实践，当然WireMock的功能不止这些，如果你感兴趣的话，请访问 [WireMock User Documentation | WireMock](https://wiremock.org/docs/)