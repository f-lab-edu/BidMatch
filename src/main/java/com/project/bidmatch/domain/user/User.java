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

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updatePassword(String encodedPassword) {
    this.passwordHash = encodedPassword;
  }

  public boolean isActive() {
    return status == UserStatus.ACTIVE;
  }
}
