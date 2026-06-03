package com.project.bidmatch.service;

import com.project.bidmatch.common.exception.BusinessException;
import com.project.bidmatch.common.exception.ErrorCode;
import com.project.bidmatch.domain.user.User;
import com.project.bidmatch.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

    if(!user.isActive()) {
      throw new BusinessException(ErrorCode.USER_SUSPENDED);
    }

    return new CustomUserPrincipal(
        user.getId(),
        user.getEmail(),
        user.getPasswordHash(),
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
        user.isActive()
    );
  }
}
