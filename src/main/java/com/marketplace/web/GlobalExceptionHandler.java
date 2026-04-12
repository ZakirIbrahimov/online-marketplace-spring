package com.marketplace.web;

import com.marketplace.service.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String business(BusinessException ex, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        String ref = req.getHeader("Referer");
        String base = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        if (ref != null && ref.startsWith(base)) {
            return "redirect:" + ref;
        }
        return "redirect:/";
    }
}
