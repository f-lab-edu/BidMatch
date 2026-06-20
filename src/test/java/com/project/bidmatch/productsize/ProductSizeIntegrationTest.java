package com.project.bidmatch.productsize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class ProductSizeIntegrationTest {

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

  private Product product;

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

    Brand brand = brandRepository.save(Brand.createBrand("나이키", "Nike", "https://x.com/n.png"));
    Category root = categoryRepository.save(Category.createRoot("신발"));
    Category leaf = categoryRepository.save(Category.createChild("스니커즈", root));
    product = productRepository.save(
        Product.create("에어포스1", brand, leaf, "AF1-001", new BigDecimal("139000"), null)
    );
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

  private String sizeBody(String size) {
    return """
        {"size": "%s"}
        """.formatted(size);
  }

  @Test
  @DisplayName("ADMIN이 사이즈 등록 시 201")
  void addSize_asAdmin() throws Exception {
    MockHttpSession session = loginAsAdmin();

    mockMvc.perform(post("/api/admin/products/" + product.getId() + "/sizes")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sizeBody("270")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.size").value("270"))
        .andExpect(jsonPath("$.productId").value(product.getId()));
  }

  @Test
  @DisplayName("동일 (상품, 사이즈) 중복 등록 시 409")
  void addSize_duplicate_conflict() throws Exception {
    MockHttpSession session = loginAsAdmin();
    mockMvc.perform(post("/api/admin/products/" + product.getId() + "/sizes")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sizeBody("270")))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/admin/products/" + product.getId() + "/sizes")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sizeBody("270")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("이미 등록된 사이즈입니다"));
  }

  @Test
  @DisplayName("일반 USER가 사이즈 등록 시 403")
  void addSize_asUser_forbidden() throws Exception {
    MockHttpSession session = loginAsUser();

    mockMvc.perform(post("/api/admin/products/" + product.getId() + "/sizes")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sizeBody("270")))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("비로그인으로 사이즈 등록 시 401")
  void addSize_withoutSession_unauthorized() throws Exception {
    mockMvc.perform(post("/api/admin/products/" + product.getId() + "/sizes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(sizeBody("270")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("로그인이 필요합니다"));
  }

  @Test
  @DisplayName("사이즈가 빈 값이면 400")
  void addSize_blank_badRequest() throws Exception {
    MockHttpSession session = loginAsAdmin();

    mockMvc.perform(post("/api/admin/products/" + product.getId() + "/sizes")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sizeBody("")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("사이즈는 필수입니다"));
  }

  @Test
  @DisplayName("존재하지 않는 상품에 사이즈 등록 시 404")
  void addSize_productNotFound() throws Exception {
    MockHttpSession session = loginAsAdmin();

    mockMvc.perform(post("/api/admin/products/99999/sizes")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(sizeBody("270")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다"));
  }

  @Test
  @DisplayName("일반 USER도 상품 사이즈 목록 조회 가능 200")
  void getSizes_asUser() throws Exception {
    productSizeRepository.save(ProductSize.create(product, "270"));
    productSizeRepository.save(ProductSize.create(product, "275"));

    MockHttpSession session = loginAsUser();
    mockMvc.perform(get("/api/products/" + product.getId() + "/sizes").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("존재하지 않는 상품의 사이즈 조회 시 404")
  void getSizes_productNotFound() throws Exception {
    MockHttpSession session = loginAsUser();

    mockMvc.perform(get("/api/products/99999/sizes").session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다"));
  }
}
