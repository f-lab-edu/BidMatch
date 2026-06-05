package com.project.bidmatch.service;

import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.user.User;
import com.project.bidmatch.dto.NicknameUpdateRequest;
import com.project.bidmatch.dto.PasswordChangeRequest;
import com.project.bidmatch.dto.UserResponse;
import com.project.bidmatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public UserResponse getMyInfo(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateNickname(Long userId, NicknameUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 동일 닉네임 차단
    if (user.getNickname().equals(request.nickname())) {
      throw new BusinessException(ErrorCode.SAME_AS_CURRENT_NICKNAME);
    }

    // 중복 차단
    if (userRepository.existsByNickname(request.nickname())) {
      throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
    }

    user.updateNickname(request.nickname());  // 도메인 메서드
    return UserResponse.from(user);
  }

  @Transactional
  public void changePassword(Long userId, PasswordChangeRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 현재 비밀번호 검증
    if (!user.matchesPassword(request.currentPassword(), passwordEncoder)) {
      throw new BusinessException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
    }
    // 같은 비밀번호 차단
    if(user.matchesPassword(request.newPassword(), passwordEncoder)) {
      throw new BusinessException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
    }

    user.updatePassword(passwordEncoder.encode(request.newPassword()));
  }
}
