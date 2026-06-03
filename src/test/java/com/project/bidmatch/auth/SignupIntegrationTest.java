package com.project.bidmatch.auth;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class SignupIntegrationTest {

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
  @DisplayName("이미 가입된 이메일로 가입 시도하면 409")
  void signupFail_duplicateEmail() throws Exception {
    String body = """
        {
          "email": "test@bidmatch.com",
          "nickname": "다른닉네임",
          "password": "Password1!"
        }
        """;

    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다"));
  }

  @Test
  @DisplayName("비밀번호가 8자 미만이면 400")
  void signupFail_shortPassword() throws Exception {
    String body = """
        {
          "email": "newbie@bidmatch.com",
          "nickname": "신규",
          "password": "Pass1!"
        }
        """;

    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상이어야 합니다"));
  }

  @Test
  @DisplayName("정상 입력으로 회원가입하면 201 + 사용자 정보 반환")
  void signupSuccess() throws Exception {
    String body = """
        {
          "email": "newbie@bidmatch.com",
          "nickname": "신규유저",
          "password": "Password1!"
        }
        """;

    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("newbie@bidmatch.com"))
        .andExpect(jsonPath("$.nickname").value("신규유저"));

    User saved = userRepository.findByEmail("newbie@bidmatch.com").orElseThrow();
    assertThat(saved.getPasswordHash()).isNotEqualTo("Password1!");
  }
}
