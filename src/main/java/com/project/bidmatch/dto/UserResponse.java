package com.project.bidmatch.dto;

import com.project.bidmatch.domain.user.User;
import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String email,
    String nickname,
    LocalDateTime joinedAt,
    int penaltyCount
) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getCreatedAt(),
        user.getPenaltyCount()
    );
  }

}
