package com.attendance.authService.config;


import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import java.beans.PropertyEditorSupport;

@ControllerAdvice
public class GlobalBinderConfig {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text != null ? text.trim() : null); // ✅
            }
        });
    }
}