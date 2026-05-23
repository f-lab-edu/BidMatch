package com.project.bidmatch;

import com.project.bidmatch.domain.user.User;
import com.project.bidmatch.domain.user.UserRole;
import com.project.bidmatch.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class BidmatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidmatchApplication.class, args);
	}

  // 서버 시작 시 테스트 계정 자동 생성
  @Bean
  public CommandLineRunner initData(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder
  ) {
    return args -> {
      User user = User.builder()
          .email("test@bidmatch.com")
          .password(passwordEncoder.encode("Password1!"))
          .nickname("테스트")
          .role(UserRole.USER)
          .build();
      userRepository.save(user);
      log.info("테스트 계정 생성: test@bidmatch.com / Password1!");
    };
  }

}
