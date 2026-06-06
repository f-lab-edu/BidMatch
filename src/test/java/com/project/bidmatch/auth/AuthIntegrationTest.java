package com.project.bidmatch.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.bidmatch.domain.user.User;
import com.project.bidmatch.domain.user.UserRole;
import com.project.bidmatch.fixture.UserFixture;
import com.project.bidmatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest       // 실제 스프링 컨테이너 전체를 띄움
@AutoConfigureMockMvc // MockMvc를 자동 구성해서 빈으로 등록
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  UserRepository userRepository;
  @Autowired
  PasswordEncoder passwordEncoder;

  private static final String RAW_PASSWORD = "Password1!";

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    userRepository.save(UserFixture.aUser().build(passwordEncoder));
  }


  @Test
  @DisplayName("올바른 이메일/비밀번호로 로그인하면 200 + 세션이 생성된다")
  void loginSuccess() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .param("email", "test@bidmatch.com")
            .param("password", RAW_PASSWORD))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.message").value("로그인 성공"))
        .andExpect(jsonPath("$.email").value("test@bidmatch.com"))
        .andExpect(jsonPath("$.userId").exists())
        .andReturn();

    MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
    assertThat(session).isNotNull();
    assertThat(
        // SPRING_SECURITY_CONTEXT_KEY 속성이 있으면 -> 인증된 보안 컨텍스트가 세션에 저장되었다는 뜻
        session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY))
        .isNotNull();
  }

  @Test
  @DisplayName("비밀번호가 틀리면 401 + 에러 메시지 반환")
  void loginFail_wrongPassword() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .param("email", "test@bidmatch.com")
            .param("password", "WrongPassword!"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다"));
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 로그인하면 401")
  void loginFail_userNotFound() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .param("email", "nonexistent@bidmatch.com")
            .param("password", RAW_PASSWORD))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다"));
  }

  @Test
  @DisplayName("로그인된 사용자가 로그아웃하면 세션 무효화 + 쿠키 삭제")
  void logoutSuccess() throws Exception {
    // 1. 먼저 로그인
    MvcResult login = mockMvc.perform(post("/api/auth/login")
            .param("email", "test@bidmatch.com")
            .param("password", RAW_PASSWORD))
        .andExpect(status().isOk())
        .andReturn();

    MockHttpSession session = (MockHttpSession) login.getRequest().getSession();
    assertThat(session).isNotNull();

    // 2. 그 세션을 들고 로그아웃 호출
    mockMvc.perform(post("/api/auth/logout").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("로그아웃 성공"));

    // 3. 세션 무효화 확인
    assertThat(session.isInvalid()).isTrue();
  }

  @Test
  @DisplayName("SUSPENDED 상태여도 로그인은 성공한다 (입찰만 제한)")
  void loginSuccess_suspendedUser() throws Exception {
    // given: 정지된 유저 저장
    User suspended = UserFixture.aUser()
        .email("suspended@bidmatch.com")
        .nickname("정지유저")
        .build(passwordEncoder);
    suspended.suspend();
    userRepository.save(suspended);

    // when & then: 로그인 성공 (200) + 세션 생성
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .param("email", "suspended@bidmatch.com")
            .param("password", RAW_PASSWORD))
        .andExpect(status().isOk())
        .andReturn();

    MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
    assertThat(session).isNotNull();
  }
}
