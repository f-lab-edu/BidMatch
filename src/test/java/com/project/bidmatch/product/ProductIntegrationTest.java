package com.project.bidmatch.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.bidmatch.domain.brand.Brand;
import com.project.bidmatch.domain.category.Category;
import com.project.bidmatch.domain.product.Product;
import com.project.bidmatch.domain.product.ProductSize;
import com.project.bidmatch.domain.user.UserRole;
import com.project.bidmatch.fixture.UserFixture;
import com.project.bidmatch.repository.BrandRepository;
import com.project.bidmatch.repository.CategoryRepository;
import com.project.bidmatch.repository.ProductRepository;
import com.project.bidmatch.repository.ProductSizeRepository;
import com.project.bidmatch.repository.UserRepository;
import java.math.BigDecimal;
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
public class ProductIntegrationTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  UserRepository userRepository;
  @Autowired
  BrandRepository brandRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  ProductRepository productRepository;
  @Autowired
  ProductSizeRepository productSizeRepository;
  @Autowired
  PasswordEncoder passwordEncoder;

  private static final String RAW_PASSWORD = "Password1!";

  private Brand brand;
  private Category leafCategory;  // 스니커즈 (자식 없음)
  private Category rootCategory;  // 신발 (스니커즈를 자식으로 가짐)
  private Product product;        // 출시가 139000 시드

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    productSizeRepository.deleteAll();
    productRepository.deleteAll();
    categoryRepository.deleteAll();
    brandRepository.deleteAll();

    userRepository.save(UserFixture.aUser()
        .email("admin@bidmatch.com").nickname("관리자").role(UserRole.ADMIN)
        .build(passwordEncoder));
    userRepository.save(UserFixture.aUser()
        .email("user@bidmatch.com").nickname("유저").role(UserRole.USER)
        .build(passwordEncoder));

    brand = brandRepository.save(Brand.createBrand("나이키", "Nike",
        "https://x.com/n.png"));
    rootCategory = categoryRepository.save(Category.createRoot("신발"));
    leafCategory = categoryRepository.save(Category.createChild("스니커즈",
        rootCategory));
    product = productRepository.save(
        Product.create("에어포스1", brand, leafCategory, "AF1-001", new
            BigDecimal("139000"), null));
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

  private String createBody(String modelNumber, Long brandId, Long categoryId) {
    return """
        {
          "name": "에어포스1",
          "brandId": %d,
          "categoryId": %d,
          "modelNumber": "%s",
          "releasePrice": 139000,
          "imageUrl": "https://x.com/af1.png"
        }
        """.formatted(brandId, categoryId, modelNumber);
  }

  // ---------- 등록 ----------

  @Test
  @DisplayName("ADMIN이 리프 카테고리에 상품 등록 시 201")
  void create_asAdmin() throws Exception {
    MockHttpSession session = loginAsAdmin();

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("DZ5485-612", brand.getId(),
                leafCategory.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.modelNumber").value("DZ5485-612"))
        .andExpect(jsonPath("$.brandName").value("나이키"))
        .andExpect(jsonPath("$.categoryName").value("스니커즈"));
  }

  @Test
  @DisplayName("이미지 없이도 등록 가능 201")
  void create_withoutImage() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
        {
          "name": "에어포스1",
          "brandId": %d,
          "categoryId": %d,
          "modelNumber": "NO-IMG-1",
          "releasePrice": 139000 
        }
        """.formatted(brand.getId(), leafCategory.getId());

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("일반 USER가 등록 시 403")
  void create_asUser_forbidden() throws Exception {
    MockHttpSession session = loginAsUser();

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("M1", brand.getId(), leafCategory.getId())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("비로그인으로 등록 시 401")
  void create_withoutSession_unauthorized() throws Exception {
    mockMvc.perform(post("/api/admin/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("M1", brand.getId(), leafCategory.getId())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("로그인이 필요합니다"));
  }

  @Test
  @DisplayName("이름이 빈 값이면 400")
  void create_blankName_badRequest() throws Exception {
    MockHttpSession session = loginAsAdmin();

    String body = """
        {
          "name": "",
          "brandId": %d,
          "categoryId": %d,
          "modelNumber": "M1",
          "releasePrice": 139000
        }
        """.formatted(brand.getId(), leafCategory.getId());

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이름은 필수입니다"));
  }

  @Test
  @DisplayName("중복 모델번호 등록 시 409")
  void create_duplicateModelNumber_conflict() throws Exception {
    MockHttpSession session = loginAsAdmin();
    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("DUP-1", brand.getId(), leafCategory.getId())))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("DUP-1", brand.getId(), leafCategory.getId())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("이미 존재하는 모델번호입니다"));
  }

  @Test
  @DisplayName("존재하지 않는 브랜드로 등록 시 404")
  void create_brandNotFound() throws Exception {
    MockHttpSession session = loginAsAdmin();

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("M1", 99999L, leafCategory.getId())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("브랜드를 찾을 수 없습니다"));
  }

  @Test
  @DisplayName("존재하지 않는 카테고리로 등록 시 404")
  void create_categoryNotFound() throws Exception {
    MockHttpSession session = loginAsAdmin();

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("M1", brand.getId(), 99999L)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다"));
  }

  @Test
  @DisplayName("리프가 아닌 카테고리에 등록 시 400")
  void create_notLeafCategory_badRequest() throws Exception {
    MockHttpSession session = loginAsAdmin();

    mockMvc.perform(post("/api/admin/products")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("M1", brand.getId(), rootCategory.getId())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("최하위(리프) 카테고리에만 상품을 등록할 수 있습니다"));
  }

  // ---------- 조회 ----------

  @Test
  @DisplayName("일반 USER도 상품 목록 조회 가능 200")
  void getAll_asUser() throws Exception {
    MockHttpSession adminSession = loginAsAdmin();
    mockMvc.perform(post("/api/admin/products")
            .session(adminSession)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody("LIST-1", brand.getId(), leafCategory.getId())))
        .andExpect(status().isCreated());

    MockHttpSession userSession = loginAsUser();
    mockMvc.perform(get("/api/products").session(userSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2)); // setUp 시드 1 + 방금 등록 1
  }

  @Test
  @DisplayName("존재하지 않는 상품 단건 조회 시 404")
  void getById_notFound() throws Exception {
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/products/99999").session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다"));
  }

  @Test
  @DisplayName("상품 수정 시 출시가는 변경되지 않는다")
  void update_releasePrice_ignored() throws Exception {
    MockHttpSession session = loginAsAdmin();
    String body = """
                {"name": "수정된이름", "imageUrl": "https://x.com/new.png",
        "releasePrice": 999999}
        """;

    mockMvc.perform(put("/api/admin/products/" + product.getId())
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("수정된이름"));

    assertThat(productRepository.findById(product.getId()).orElseThrow().getReleasePrice())
        .isEqualByComparingTo(new BigDecimal("139000"));
  }

  @Test
  @DisplayName("상품 삭제 후 단건 조회는 404, 목록엔 안 나온다")
  void softDelete_thenHidden() throws Exception {
    MockHttpSession session = loginAsAdmin();
    mockMvc.perform(delete("/api/admin/products/" +
            product.getId()).session(session))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/products/" + product.getId()).session(session))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/api/products").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("사이즈 삭제 후 사이즈 목록에서 제외된다")
  void deleteSize_thenExcluded() throws Exception {
    MockHttpSession session = loginAsAdmin();
    ProductSize saved = productSizeRepository.save(ProductSize.create(product,
        "270"));

    mockMvc.perform(delete("/api/admin/products/" + product.getId() + "/sizes/" +
            saved.getId())
            .session(session))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/products/" + product.getId() +
            "/sizes").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}