package com.project.bidmatch.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.bidmatch.domain.user.User;
import com.project.bidmatch.fixture.UserFixture;
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
public class UserIntegrationTest {

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

  private MockHttpSession login() throws Exception {
    return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
            .param("email", "test@bidmatch.com")
            .param("password", RAW_PASSWORD))
        .andReturn().getRequest().getSession(false);
  }

  @Test
  @DisplayName("닉네임을 11자 이상으로 변경하면 400 (DB 컬럼 길이 초과 방어)")
  void updateNickname_tooLong() throws Exception {
    MockHttpSession session = login();

    String body = """
        {"nickname": "user1234567"}
        """;

    mockMvc.perform(patch("/api/users/me/nickname")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("닉네임은 2~10자여야 합니다"));
  }

  @Test
  @DisplayName("닉네임 업데이트 성공")
  void updateNickname() throws Exception {
    MockHttpSession session = login();

    String body = """
            {"nickname": "modified"}
        """;

    mockMvc.perform(patch("/api/users/me/nickname")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value("modified"));
  }

  @Test
  @DisplayName("중복된 닉네임으로 닉네임 업데이트시 HttpStatus = 409")
  void updateNickname_duplicate() throws Exception {
    MockHttpSession session = login();

    userRepository.save(UserFixture.aUser().email("tester@google.com").nickname("duplicated")
        .build(passwordEncoder));

    String body = """
            {"nickname": "duplicated"}
        """;

    mockMvc.perform(patch("/api/users/me/nickname")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다"));
  }

  @Test
  @DisplayName("현재 닉네임으로 닉네임 업데이트시 HttpStatus = 400")
  void updateNickname_sameNickname() throws Exception {
    MockHttpSession session = login();

    String body = """
            {"nickname": "테스트"}
        """;

    mockMvc.perform(patch("/api/users/me/nickname")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("기존 닉네임과 동일합니다"));
  }

  @Test
  @DisplayName("세션없이 닉네임 업데이트시 HttpStatus = 401")
  void updateNickname_withoutSession() throws Exception {
    String body = """
        {"nickname": "user1234567"}
        """;

    mockMvc.perform(patch("/api/users/me/nickname")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("로그인이 필요합니다"));
  }

  @Test
  @DisplayName("비밀번호 변경 성공")
  void updatePassword() throws Exception {
    MockHttpSession session = login();

    String body = """
        { 
          "currentPassword": "Password1!",
          "newPassword": "modified"
        }
        """;

    mockMvc.perform(patch("/api/users/me/password")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isNoContent());

    User updated = userRepository.findByEmail("test@bidmatch.com").orElseThrow();
    assertThat(passwordEncoder.matches("modified", updated.getPasswordHash())).isTrue();
  }

  @Test
  @DisplayName("getMyInfo_success")
  void getMyInfo_success() throws Exception {
    MockHttpSession session = login();

    mockMvc.perform(get("/api/users/me")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@bidmatch.com"))
        .andExpect(jsonPath("$.nickname").value("테스트"))
        .andExpect(jsonPath("$.penaltyCount").value(0));

  }
}


