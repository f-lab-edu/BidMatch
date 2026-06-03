package com.project.bidmatch.auth.service;

import com.project.bidmatch.auth.dto.SignupRequest;
import com.project.bidmatch.auth.dto.SignupResponse;
import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.user.User;
import com.project.bidmatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public SignupResponse signup(SignupRequest signupRequest) {
    // 1. 중복 검사
    if (userRepository.existsByEmail(signupRequest.email())) {
      throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
    }
    if(userRepository.existsByNickname(signupRequest.nickname())) {
      throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
    }

    // 2. 비밀번호 해싱
    String encoded = passwordEncoder.encode(signupRequest.password());

    // 3. 도메인 생성 + 저장
    User user = User.signup(signupRequest.email(), signupRequest.nickname(), encoded);
    User saved = userRepository.save(user);

    return SignupResponse.from(saved);
  }

}
