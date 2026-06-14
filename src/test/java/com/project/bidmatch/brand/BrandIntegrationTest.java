package com.project.bidmatch.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.bidmatch.domain.brand.Brand;
import com.project.bidmatch.domain.user.UserRole;
import com.project.bidmatch.fixture.UserFixture;
import com.project.bidmatch.repository.BrandRepository;
import com.project.bidmatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BrandIntegrationTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  UserRepository userRepository;
  @Autowired
  BrandRepository brandRepository;
  @Autowired
  PasswordEncoder passwordEncoder;

  private static final String RAW_PASSWORD = "Password1!";

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    brandRepository.deleteAll();

    userRepository.save(UserFixture.aUser()
        .email("admin@bidmatch.com").nickname("관리자").role(UserRole.ADMIN).build(passwordEncoder));
    userRepository.save(UserFixture.aUser()
        .email("user@bidmatch.com").nickname("유저").role(UserRole.USER).build(passwordEncoder));
  }

  private MockHttpSession login(String email) throws Exception {
    return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
            .param("email", email)
            .param("password", RAW_PASSWORD))
        .andReturn().getRequest().getSession(false);
  }

  private MockHttpSession loginAsAdmin() throws Exception {
    return login("admin@bidmatch.com");
  }

  private MockHttpSession loginAsUser() throws Exception {
    return login("user@bidmatch.com");
  }

  private Brand saveBrand(String name, String englishName, String logoUrl) {
    return brandRepository.save(Brand.createBrand(name, englishName, logoUrl));
  }

  @Test
  @DisplayName("ADMIN이 브랜드 등록 시 201 + 등록 정보 반환")
  void createBrand_asAdmin() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
        {
          "name": "나이키",
          "englishName": "Nike",
          "logoUrl": "https://cdn.bidmatch.com/nike.png"
        }
        """;

    mockMvc.perform(post("/api/admin/brands")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("나이키"))
        .andExpect(jsonPath("$.englishName").value("Nike"));

    assertThat(brandRepository.existsByName("나이키")).isTrue();
  }

  @Test
  @DisplayName("일반 USER가 브랜드 등록 시 403")
  void createBrand_asUser_forbidden() throws Exception {
    MockHttpSession session = loginAsUser();

    String body = """
        {
          "name": "나이키",
          "englishName": "Nike",
          "logoUrl": "https://cdn.bidmatch.com/nike.png"
        }
        """;

    mockMvc.perform(post("/api/admin/brands")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("비로그인으로 브랜드 등록 시 401")
  void createBrand_withoutSession_unauthorized() throws Exception {
    String body = """
        {
          "name": "나이키",
          "englishName": "Nike",
          "logoUrl": "https://cdn.bidmatch.com/nike.png"
        }
        """;

    mockMvc.perform(post("/api/admin/brands")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("로그인이 필요합니다"));
  }

  @Test
  @DisplayName("이름이 빈 값이면 400")
  void createBrand_blankName_badRequest() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
        {
          "name": "",
          "englishName": "Nike",
          "logoUrl": "https://x.com/n.png"
        }
        """;

    mockMvc.perform(post("/api/admin/brands")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이름은 필수입니다"));
  }

  @Test
  @DisplayName("중복된 이름으로 등록 시 409")
  void createBrand_duplicateName_conflict() throws Exception {
    saveBrand("나이키", "Nike", "https://x.com/n.png");
    MockHttpSession session = loginAsAdmin();

    String body = """
        {
          "name": "나이키",
          "englishName": "Nike",
          "logoUrl": "https://x.com/n2.png"
        }
        """;

    mockMvc.perform(post("/api/admin/brands")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("이미 존재하는 브랜드 이름입니다"));
  }

  @Test
  @DisplayName("이름은 그대로 두고 로고만 수정하면 200 (자기 자신 중복 검사 제외)")
  void updateBrand_sameNameOnlyLogo() throws Exception {
    Brand brand = saveBrand("나이키", "Nike", "https://x.com/old.png");
    MockHttpSession session = loginAsAdmin();

    String body = """
                {"name": "나이키", "englishName": "Nike", "logoUrl":
        "https://x.com/new.png"}
        """;

    mockMvc.perform(put("/api/admin/brands/" + brand.getId())
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.logoUrl").value("https://x.com/new.png"));
  }

  @Test
  @DisplayName("이미 존재하는 다른 브랜드의 이름으로 수정 시 409")
  void updateBrand_toExistingName_conflict() throws Exception {
    saveBrand("나이키", "Nike", "https://x.com/n.png");
    Brand adidas = saveBrand("아디다스", "Adidas", "https://x.com/a.png");
    MockHttpSession session = loginAsAdmin();

    String body = """
                {"name": "나이키", "englishName": "Adidas", "logoUrl":
        "https://x.com/a.png"}
        """;

    mockMvc.perform(put("/api/admin/brands/" + adidas.getId())
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("이미 존재하는 브랜드 이름입니다"));
  }

  @Test
  @DisplayName("존재하지 않는 id 수정 시 404")
  void updateBrand_notFound() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
          {"name": "나이키", "englishName": "Nike", "logoUrl":
  "https://x.com/n.png"}
          """;

    mockMvc.perform(put("/api/admin/brands/99999")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("브랜드를 찾을 수 없습니다"));
  }

  @Test
  @DisplayName("일반 USER가 브랜드 수정 시 403")
  void updateBrand_asUser_forbidden() throws Exception {
    Brand brand = saveBrand("나이키", "Nike", "https://x.com/n.png");
    MockHttpSession session = loginAsUser();

    String body = """
          {"name": "나이키", "englishName": "Nike", "logoUrl":
  "https://x.com/new.png"}
          """;

    mockMvc.perform(put("/api/admin/brands/" + brand.getId())
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("일반 USER도 브랜드 목록 조회 가능 (200)")
  void getAllBrands_asUser() throws Exception {
    saveBrand("나이키", "Nike", "https://x.com/n.png");
    saveBrand("아디다스", "Adidas", "https://x.com/a.png");
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/brands").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("브랜드 단건 조회 성공 (200)")
  void getBrandById_success() throws Exception {
    Brand brand = saveBrand("나이키", "Nike", "https://x.com/n.png");
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/brands/" + brand.getId()).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("나이키"));
  }

  @Test
  @DisplayName("존재하지 않는 브랜드 단건 조회 시 404")
  void getBrandById_notFound() throws Exception {
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/brands/99999").session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("브랜드를 찾을 수 없습니다"));
  }

}
