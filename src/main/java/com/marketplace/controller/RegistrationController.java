package com.marketplace.controller;

import com.marketplace.service.BusinessException;
import com.marketplace.service.RegistrationService;
import com.marketplace.web.dto.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String form(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String submit(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        if ("MERCHANT".equalsIgnoreCase(form.getAccountType())
                && (form.getBusinessName() == null || form.getBusinessName().isBlank())) {
            bindingResult.rejectValue("businessName", "required", "Business name is required");
            return "register";
        }
        try {
            if ("MERCHANT".equalsIgnoreCase(form.getAccountType())) {
                registrationService.registerMerchant(
                        form.getEmail(), form.getPassword(), form.getBusinessName(), form.getDescription());
                ra.addFlashAttribute(
                        "message", "Account created. An admin must approve your merchant profile.");
            } else {
                registrationService.registerShopper(form.getEmail(), form.getPassword());
                ra.addFlashAttribute("message", "Account created. You can sign in.");
            }
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
        return "redirect:/login";
    }
}
