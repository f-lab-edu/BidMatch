package com.project.bidmatch.domain.user;

import com.project.bidmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, unique = true, length = 10)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status;

  @Column(name = "penalty_count", nullable = false)
  private int penaltyCount;

  @Column(name="suspended_until")
  private LocalDateTime suspendedUntil;

  @Builder
  private User(String email, String passwordHash, String nickname, UserRole role) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.nickname = nickname;
    this.role = role != null ? role : UserRole.USER;
    this.status = UserStatus.ACTIVE;
    this.penaltyCount = 0;
  }

  public static User signup(String email, String nickname, String encodedPassword) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .passwordHash(encodedPassword)
        .role(UserRole.USER)
        .build();
  }

  private static final int SUSPENSION_DAYS = 7;

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updatePassword(String encodedPassword) {
    this.passwordHash = encodedPassword;
  }

  public boolean isActive() {
    return status == UserStatus.ACTIVE;
  }

  public boolean matchesPassword(String rawPassword, PasswordEncoder encoder) {
    return encoder.matches(rawPassword, this.passwordHash);
  }

  public void suspend(LocalDateTime now) {
    this.status = UserStatus.SUSPENDED;
    this.suspendedUntil = now.plusDays(SUSPENSION_DAYS);
  }
}
