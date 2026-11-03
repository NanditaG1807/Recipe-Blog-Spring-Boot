package com.main.recipeblog.controller;

import com.main.recipeblog.entity.Role;
import com.main.recipeblog.entity.User;
import com.main.recipeblog.repository.UserRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor
    public LoginController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =========================
    // LOGIN
    // =========================

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }


    // =========================
    // REGISTER
    // =========================

    // Show register page
    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }


    // Save registered user
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") User user,
            Model model) {

        // Admin email वापरून registration करू देऊ नका
        if ("admin@gmail.com".equalsIgnoreCase(user.getEmail())) {

            model.addAttribute(
                    "error",
                    "This email is reserved for Admin!"
            );

            return "register";
        }

        // Already registered email
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {

            model.addAttribute(
                    "error",
                    "Email is already registered!"
            );

            return "register";
        }

        // Password encode
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // Always USER
        user.setRole(Role.USER);

        userRepository.save(user);

        return "redirect:/login";
    }



    // =========================
    // FORGOT PASSWORD
    // =========================

    // Show forgot password page
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot";
    }



    // Reset password
    @PostMapping("/forgot-password")
    public String resetPassword(

            @RequestParam("email") String email,

            @RequestParam("password") String password,

            @RequestParam("confirmPassword") String confirmPassword,

            RedirectAttributes redirectAttributes) {

        // 1. Check passwords
        if (!password.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Passwords do not match!"
            );

            return "redirect:/forgot-password";
        }

        // 2. Find user by email
        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        // 3. Email not found
        if (user == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email not found!"
            );

            return "redirect:/forgot-password";
        }

        // 4. ADMIN cannot reset password
        if (user.getRole() == Role.ADMIN) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Admin password can only be changed by the administrator!"
            );

            return "redirect:/login";
        }


        // 5. Change password only for USER
        user.setPassword(
                passwordEncoder.encode(password)
        );

        userRepository.save(user);

        // 6. Success message
        redirectAttributes.addFlashAttribute(
                "success",
                "Password changed successfully!"
        );

        return "redirect:/login";
    }

//user-info
@GetMapping("/user-info")
public String userInfo(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {

    User user = userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    model.addAttribute("user", user);

    return "user-info";
}


}
