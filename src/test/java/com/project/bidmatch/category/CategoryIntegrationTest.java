package com.project.bidmatch.category;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.bidmatch.domain.category.Category;
import com.project.bidmatch.domain.user.UserRole;
import com.project.bidmatch.fixture.UserFixture;
import com.project.bidmatch.repository.CategoryRepository;
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
public class CategoryIntegrationTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  UserRepository userRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  PasswordEncoder passwordEncoder;

  private static final String RAW_PASSWORD = "Password1!";

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    categoryRepository.deleteAll();

    userRepository.save(UserFixture.aUser()
        .email("admin@bidmatch.com").nickname("관리자").role(UserRole.ADMIN)
        .build(passwordEncoder));

    userRepository.save(UserFixture.aUser()
        .email("user@bidmatch.com")
        .nickname("유저")
        .role(UserRole.USER)
        .build(passwordEncoder));
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

  private Category saveRoot(String name) {
    return categoryRepository.save(Category.createRoot(name));
  }

  private Category saveChild(String name, Category parent) {
    return categoryRepository.save(Category.createChild(name, parent));
  }

  @Test
  @DisplayName("ADMIN이 루트 카테고리 등록 시 201")
  void createRoot_asAdmin() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
        {"name": "신발", "parentId": null}
        """;

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("신발"))
        .andExpect(jsonPath("$.parentId").doesNotExist());
  }

  @Test
  @DisplayName("ADMIN이 자식 카테고리 등록 시 201 + parentId 반환")
  void createChild_asAdmin() throws Exception {
    Category shoes = saveRoot("신발");
    MockHttpSession session = loginAsAdmin();

    String body = """
        {"name": "스니커즈", "parentId": %d}
        """.formatted(shoes.getId());

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("스니커즈"))
        .andExpect(jsonPath("$.parentId").value(shoes.getId()));
  }

  @Test
  @DisplayName("3단계 이상 등록 가능 (무제한 깊이)")
  void createThirdLevel() throws Exception {
    Category shoes = saveRoot("신발");
    Category sneakers = saveChild("스니커즈", shoes);
    MockHttpSession session = loginAsAdmin();

    String body = """
        {"name": "한정판", "parentId": %d}
        """.formatted(sneakers.getId());

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.parentId").value(sneakers.getId()));
  }

  @Test
  @DisplayName("일반 USER가 등록 시 403")
  void create_asUser_forbidden() throws Exception {
    MockHttpSession session = loginAsUser();

    String body = """
        {"name": "신발", "parentId": null}
        """;

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("비로그인으로 등록 시 401")
  void create_withoutSession_unauthorized() throws Exception {
    String body = """
        {"name": "신발", "parentId": null}
        """;

    mockMvc.perform(post("/api/admin/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("로그인이 필요합니다"));
  }

  @Test
  @DisplayName("이름이 빈 값이면 400")
  void create_blankName_badRequest() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
        {"name": "", "parentId": null}
        """;

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이름은 필수입니다"));
  }

  @Test
  @DisplayName("존재하지 않는 parentId로 등록 시 404")
  void create_parentNotFound() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
        {"name": "스니커즈", "parentId": 99999}
        """;

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("부모 카테고리를 찾을 수 없습니다"));
  }

  @Test
  @DisplayName("같은 부모 아래 같은 이름 등록 시 409")
  void create_dupicateSibling_conflict() throws Exception {
    Category shoes = saveRoot("신발");
    saveChild("스니커즈", shoes);
    MockHttpSession session = loginAsAdmin();

    String body = """
        {"name": "스니커즈", "parentId": %d}
        """.formatted(shoes.getId());

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("같은 부모 아래 동일한 이름의 카테고리가 있습니다"));
  }

  @Test
  @DisplayName("다른 부모 아래 같은 이름은 등록 가능 (201)")
  void create_sameNameDifferentParent() throws Exception {
    Category shoes = saveRoot("신발");
    Category clothes = saveRoot("의류");
    saveChild("스니커즈", shoes);
    MockHttpSession session = loginAsAdmin();

    String body = """
        {"name": "스니커즈", "parentId": %d}
        """.formatted(clothes.getId());

    mockMvc.perform(post("/api/admin/categories")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("카테고리 이름 수정 성공 200")
  void updateName_success() throws Exception {
    Category shoes = saveRoot("신발");
    MockHttpSession session = loginAsAdmin();

    String body = """
          {"name": "신발류"}
          """;
    mockMvc.perform(put("/api/admin/categories/" + shoes.getId())
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("신발류"));
  }

  @Test
  @DisplayName("존재하지 않는 카테고리 수정 시 404")
  void update_notFound() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
          {"name": "신발류"}
          """;

    mockMvc.perform(put("/api/admin/categories/99999")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다"));
  }

  @Test
  @DisplayName("일반 USER도 목록 조회 가능 200")
  void getAll_asUser() throws Exception {
    Category shoes = saveRoot("신발");
    saveChild("스니커즈", shoes);
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/categories").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("단건 조회 성공 200")
  void getById_success() throws Exception {
    Category shoes = saveRoot("신발");
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/categories/" + shoes.getId()).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("신발"));
  }

  @Test
  @DisplayName("존재하지 않는 단건 조회 시 404")
  void getById_notFound() throws Exception {
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/categories/99999").session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다"));
  }

}
