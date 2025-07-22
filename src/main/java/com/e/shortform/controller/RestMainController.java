package com.e.shortform.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class RestMainController {

    static class staticTestObj {
        private String message;

        public staticTestObj(String message) {
            this.message = message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @GetMapping
    public ResponseEntity<?> hello() {
        staticTestObj m = new staticTestObj("hello");
        return ResponseEntity.ok(m);
    }

}
