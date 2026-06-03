package com.project.bidmatch.fixture;

import com.project.bidmatch.domain.user.User;
import com.project.bidmatch.domain.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserFixture {

  private String email = "test@bidmatch.com";
  private String rawPassword = "Password1!";
  private String nickname = "테스트";
  private UserRole role = UserRole.USER;

  public static UserFixture aUser() { // 기본값 유저
    return new UserFixture();
  }

  public UserFixture email(String email) {
    this.email = email;
    return this;
  }

  public UserFixture nickname(String nickname) {
    this.nickname = nickname;
    return this;
  }

  public UserFixture role(UserRole role) {
    this.role = role;
    return this;
  }

  public User build(PasswordEncoder passwordEncoder) {
    return User.builder()
        .email(email)
        .passwordHash(passwordEncoder.encode(rawPassword))
        .nickname(nickname)
        .role(role)
        .build();
  }

}
