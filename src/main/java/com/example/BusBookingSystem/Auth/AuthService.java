package com.example.BusBookingSystem.Auth;


import com.example.BusBookingSystem.Config.Security.JwtService;
import com.example.BusBookingSystem.User.Role;
import com.example.BusBookingSystem.User.User;
import com.example.BusBookingSystem.User.UserMapper;
import com.example.BusBookingSystem.User.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;



    public void verifyDetails(RegisterRequestDTO registerRequestDTO) {


        // Verify if email does not exist
        if (userRepository.findByEmail(registerRequestDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");

        }


        // Verify if password and equal password match
        if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }


        // Check if valid role is choosen
        if (registerRequestDTO.getRole() != Role.USER && registerRequestDTO.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Invalid role selected");
        }
        String email = registerRequestDTO.getEmail();




        String lockKey = "verify:lock:" + email;
        String codeKey = "verify:code:" + email;
        String userKey = "verify:user:" + email;


        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(30));


        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new IllegalStateException(
                    "Verification already in progress. Please wait."
            );
        }

        String code = generateCode();


        // Store verification code (1 min)
        redisTemplate.opsForValue()
                .set(codeKey, code, Duration.ofMinutes(1));


      // Store user temporarily (5 min)
        try {
            String userJson = objectMapper.writeValueAsString(registerRequestDTO);
            redisTemplate.opsForValue()
                    .set(userKey, userJson, Duration.ofMinutes(5));
        } catch (JsonProcessingException e) {
            redisTemplate.delete(lockKey); // cleanup on failure
            throw new RuntimeException("Failed to store user temporarily");
        }


        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(registerRequestDTO.getEmail());
        message.setSubject("Verification Code");
        message.setText(
                "Your verification code is: " + code +
                        "\n\nThis code will expire in a minute."
        );


        // Send mail to user
        mailSender.send(message);

    }


    public String generateCode() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 999999)
        );

    }





    // This function verify if the correct verification code is entered for that email
    public void verifyCode(VerifyCodeRequestDTO dto) {

        String email = dto.getEmail();
        String savedCode = redisTemplate.opsForValue().get("verify:code:" + email);

        // Check if redis has any code is not null and redis saved code is equals to the one user sent
        if (savedCode == null || !savedCode.equals(dto.getCode())) {
            throw new IllegalArgumentException("Invalid or expired code");
        }



        String userJson = redisTemplate.opsForValue()
                .get("verify:user:" + email);


        // Check if  Registration has expired
        if (userJson == null) {
            throw new IllegalArgumentException("Registration expired");
        }

        try {
            RegisterRequestDTO registerDTO =
                    objectMapper.readValue(userJson, RegisterRequestDTO.class);

            User user = userMapper.registerDtoToEntity(registerDTO);
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user");
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }


    // Function or method verify user credentials and provide access ad refresh token
    public LoginResponseDto login(LoginRequestDTO loginRequestDTO) {

        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();


        // Verify provided user credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()
                )
        );

        // Get user and generate access and refresh token
        User user = (User) authentication.getPrincipal();
        String accesstoken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        String role = user.getRole().toString();

        return  LoginResponseDto.builder()
                .accessToken(accesstoken)
                .refreshToken(refreshToken)
                .role(role)
                .build();

    }

    public void updateUserDetails(UserUpdateRequestDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());

        userRepository.save(user);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = getCookieValue(request, "accessToken");

        if (accessToken != null) {
            try {
                // Blacklist the token logic...
                long expiration = jwtService.getJwtExpiration(accessToken).getTime() - System.currentTimeMillis();
                if (expiration > 0) {
                    redisTemplate.opsForValue().set("blacklist:" + accessToken, "true", Duration.ofMillis(expiration));
                }
            } catch (Exception ignored) {}
        }

        // Clear the cookies from the browser
        clearCookie(response, "accessToken");
        clearCookie(response, "refreshToken");
    }


    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public void changePassword(ChangePasswordRequestDTO dto, String currentEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }


        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }


        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the old password");
        }


        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }


    public void ForgotPassword(ForgotPasswordDto dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }


        String email = dto.getEmail();
        String lockKey = "verify:lock:" + email;
        String codeKey = "verify:code:" + email;
        String resetKey = "verify:reset:" + email;

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(30));
        if(Boolean.FALSE.equals(lockAcquired)) {
            throw new IllegalStateException("Verification already in progress");

        }

        try{
            String code = generateCode();
            redisTemplate.opsForValue().set(codeKey, code, Duration.ofSeconds(30));


            PasswordResetTemp temp = new PasswordResetTemp(
                    email,
                    passwordEncoder.encode(dto.getPassword())
            );

            String tempJson = objectMapper.writeValueAsString(temp);

            redisTemplate.opsForValue()
                    .set(resetKey, tempJson, Duration.ofMinutes(5));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(dto.getEmail());
            message.setSubject("Verification Code");
            message.setText(
                    "Your verification code is: " + code +
                            "\n\nThis code will expire in a minute."
            );

            mailSender.send(message);





        }catch (Exception e) {
            throw new RuntimeException("Failed to start password reset");
        } finally {
            redisTemplate.delete(lockKey);
        }



    }

    public void verifyForgotPasswordCode(VerifyCodeRequestDTO dto) {

        String email = dto.getEmail();

        String codeKey = "verify:code:" + email;
        String resetKey = "verify:reset:" + email;

        String savedCode = redisTemplate.opsForValue().get(codeKey);

        if (savedCode == null || !savedCode.equals(dto.getCode())) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        String resetJson = redisTemplate.opsForValue().get(resetKey);

        if (resetJson == null) {
            throw new IllegalStateException("Password reset request expired");
        }

        try {
            PasswordResetTemp temp =
                    objectMapper.readValue(resetJson, PasswordResetTemp.class);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setPassword(temp.getHashedPassword());
            userRepository.save(user);


            redisTemplate.delete(codeKey);
            redisTemplate.delete(resetKey);

        } catch (Exception e) {
            throw new RuntimeException("Failed to reset password");
        }
    }





}
