package com.example.BusBookingSystem.Auth;

import com.example.BusBookingSystem.User.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/api/v1/auth")
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Get register page
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("register")) {
            model.addAttribute("register", new RegisterRequestDTO());
        }
        return "register";
    }


    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("register") RegisterRequestDTO registerRequestDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();

            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("register", registerRequestDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.register", bindingResult);
            return "redirect:/api/v1/auth/register";
        }

        try {
            authService.verifyDetails(registerRequestDTO);
            return "redirect:/api/v1/auth/verify?email=" + registerRequestDTO.getEmail();

        } catch (IllegalStateException ex) {
            return "redirect:/api/v1/auth/verify?email=" + registerRequestDTO.getEmail();

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("register", registerRequestDTO);
            return "redirect:/api/v1/auth/register";
        }
    }

    @GetMapping("/verify")
    public String verifyPage(
            Model model,
            @RequestParam(required = false) String email
    ) {
        if (!model.containsAttribute("verify")) {
            VerifyCodeRequestDTO dto = new VerifyCodeRequestDTO();
            dto.setEmail(email);
            model.addAttribute("verify", dto);
        }
        return "verify";
    }

    @PostMapping("/verify")
    public String verify(
            @Valid @ModelAttribute("verify") VerifyCodeRequestDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid code");
            redirectAttributes.addFlashAttribute("verify", dto);
            return "redirect:/api/v1/auth/verify?email=" + dto.getEmail();
        }

        try {
            authService.verifyCode(dto);
            return "redirect:/api/v1/auth/login?verified=true";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("verify", dto);
            return "redirect:/api/v1/auth/verify?email=" + dto.getEmail();
        }
    }

    @GetMapping("/login")
    public String loginPage(
            Model model,
            @RequestParam(required = false) String verified
    ) {

        if ("true".equals(verified)) {
            model.addAttribute("success", "Account verified successfully. Please login.");
        }

        if (!model.containsAttribute("login")) {
            model.addAttribute("login", new LoginRequestDTO());
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("login") LoginRequestDTO loginRequestDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid input");
            return "redirect:/api/v1/auth/login";
        }

        try {
            LoginResponseDto loginResponse = authService.login(loginRequestDTO);

            Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60 * 30);

            Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            return "ADMIN".equals(loginResponse.getRole())
                    ? "redirect:/admin-dashboard"
                    : "redirect:/user-dashboard";

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/api/v1/auth/login";
        }
    }




    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {

        User user = authService.getUserByEmail(authentication.getName());

        UserUpdateRequestDTO updateDTO = new UserUpdateRequestDTO();
        updateDTO.setFirstName(user.getFirstName());
        updateDTO.setLastName(user.getLastName());
        updateDTO.setPhone(user.getPhone());

        model.addAttribute("userUpdate", updateDTO);
        model.addAttribute("passwordChange", new ChangePasswordRequestDTO());
        return "profile";
    }


    @PostMapping("/update-details")
    public String updateDetails(
            @Valid @ModelAttribute("userUpdate") UserUpdateRequestDTO dto,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "profile";
        }
        authService.updateUserDetails(dto, authentication.getName());
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/api/v1/auth/profile";
    }


    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordChange") ChangePasswordRequestDTO dto,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "profile";
        }
        try {
            authService.changePassword(dto, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/api/v1/auth/profile";
    }



    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return "redirect:/api/v1/auth/login?logout=true";
    }



    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("forgot")) {
            model.addAttribute("forgot", new ForgotPasswordDto());
        }
        return "forgot-password";
    }



    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute("forgot") ForgotPasswordDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid input");
            redirectAttributes.addFlashAttribute("forgot", dto);
            return "redirect:/api/v1/auth/forgot-password";
        }

        try {
            authService.ForgotPassword(dto);
            return "redirect:/api/v1/auth/forgot-verify?email=" + dto.getEmail();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/api/v1/auth/forgot-password";
        }
    }

    @GetMapping("/forgot-verify")
    public String forgotVerifyPage(
            @RequestParam String email,
            Model model
    ) {
        VerifyCodeRequestDTO dto = new VerifyCodeRequestDTO();
        dto.setEmail(email);
        model.addAttribute("verify", dto);
        return "verify-forgot";
    }


    @PostMapping("/forgot-verify")
    public String forgotVerify(
            @Valid @ModelAttribute("verify") VerifyCodeRequestDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid code");
            return "redirect:/api/v1/auth/forgot-verify?email=" + dto.getEmail();
        }

        try {
            authService.verifyForgotPasswordCode(dto);
            return "redirect:/api/v1/auth/login?reset=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/api/v1/auth/forgot-verify?email=" + dto.getEmail();
        }
    }


}
