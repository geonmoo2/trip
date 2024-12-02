package com.example.demo.Service;


import com.example.demo.Entity.User;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserProfileUpdateRequest;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.secret}")
    private String secretKey;
    private final Long expiredMs = 1000 * 60 * 60L;

    public TokenInfo login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        return jwtTokenProvider.generateToken(authentication);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean userJoin(UserDTO userDTO) {
        if (userRepository.existsByUserName(userDTO.getUserName())) {
            log.warn("중복된 사용자 아이디: {}", userDTO.getUserName());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "사용자 아이디가 이미 존재합니다.");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            log.warn("중복된 이메일: {}", userDTO.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이메일이 이미 존재합니다.");
        }

        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setPassword(encoder.encode(userDTO.getPassword()));
        user.setRealname(userDTO.getRealname());
        user.setBirth(userDTO.getBirth());
        user.setPhoneNumber(userDTO.getPhone());
        user.setAddr(userDTO.getAddr());
        user.setEmail(userDTO.getEmail());
        user.setGender(userDTO.getGender());
        user.setRole("ROLE_USER");
        user.setLocked(false);

        userRepository.save(user);
        log.info("회원가입 성공: {}", userDTO.getUserName());
        return true;
    }

    public User getUserInfo(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("이메일 조회 실패: email={}", email, e);
            throw new RuntimeException("이메일 조회 중 오류가 발생했습니다.");
        }
    }

    public String findUsername(String realname, LocalDate birth, String email) {
        Optional<User> user = userRepository.findByRealnameAndBirthAndEmail(realname, birth, email);
        return user.map(User::getUserName).orElse(null);
    }

    public void resetPassword(String userName, String email) {
        Optional<User> userOptional = userRepository.findByUserNameAndEmail(userName, email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String tempPassword = generateTempPassword();
            updatePassword(user, tempPassword);
            emailService.sendEmail(user.getEmail(), "임시 비밀번호", "임시 비밀번호는: " + tempPassword + " 입니다.");
        } else {
            throw new UsernameNotFoundException("해당 사용자 아이디와 이메일로 등록된 사용자를 찾을 수 없습니다 : " + email);
        }
    }

    public boolean checkUsernameDuplicate(String username) {
        return userRepository.existsByUserName(username);
    }

    private String generateTempPassword() {
        Random random = new Random();
        int length = 10;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char)(random.nextInt(26) + 'a'));
        }
        return sb.toString();
    }

    @Transactional(rollbackFor=Exception.class)
    public void updatePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 아이디를 찾을 수 없습니다 " + username));

        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(String username, UserProfileUpdateRequest request) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if the email is being updated and if it's already in use by another user
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이메일이 이미 사용 중입니다.");
        }

        // Update user profile fields
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddr(request.getAddress());
        user.setRealname(request.getRealName());
        user.setBirth(request.getBirthDate());
        user.setGender(request.getGender());

        userRepository.save(user);
    }

}
