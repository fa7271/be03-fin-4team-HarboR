package com.example.harbor_login.Login.service;

import com.example.harbor_login.Login.domain.Login;
import com.example.harbor_login.Login.dto.LoginSignInReqDto;
import com.example.harbor_login.Login.dto.LoginSignUpReqDto;
import com.example.harbor_login.Login.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    public void signup(LoginSignUpReqDto loginCreateReqDto) {
        Optional<Login> findMember = loginRepository.findByEmail(loginCreateReqDto.getEmail());
        if (findMember.isPresent()) {
            throw new EntityNotFoundException("이미 존재합니다.");
        }
        Login login = Login.createLogin(
                loginCreateReqDto.getEmail(),
                passwordEncoder.encode(loginCreateReqDto.getPassword()),
                loginCreateReqDto.getName(),
                loginCreateReqDto.getBirth()
        );

        loginRepository.save(login);

    }

    public Login signin(LoginSignInReqDto loginSignInReqDto) {
        // 이메일을 기준으로 회원을 찾음
        Login member = loginRepository.findByEmail(loginSignInReqDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 이메일입니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginSignInReqDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 사원 번호를 기준으로 회원을 찾음
        Login employee = loginRepository.findByEmployeeId(loginSignInReqDto.getEmployeeId())
                .orElse(null);

        // 사원 번호가 존재하고, 회원과 일치하지 않는 경우
        if (employee != null && !member.getEmail().equals(employee.getEmail())) {
            throw new IllegalArgumentException("사원 번호가 올바르지 않습니다.");
        }
        // 회원 혹은 사원 번호가 존재하지 않는 경우
        if (employee == null) {
            throw new IllegalArgumentException("존재하지 않는 사원 번호입니다.");
        }
        // 다 통과
        return member;
    }

    public Login getUserInfo(String email) {
        return loginRepository.findByEmail(email).orElseThrow(IllegalArgumentException::new);
    }
}
