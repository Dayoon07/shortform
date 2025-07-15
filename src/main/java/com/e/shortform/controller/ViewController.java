package com.e.shortform.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ViewController {

    static class User {
        private int id;
        private String username;

        public void setUsername(String username) {
            this.username = username;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }

    @GetMapping("/")
    public String index(Model m) {

        List<User> users = new ArrayList<>();

        for (int i = 1; i < 11; i++) {
            User user = new User();
            user.id = i;
            user.username = "user" + i;
            users.add(user);
        }

        System.out.println(users);

        m.addAttribute("list", users);
        m.addAttribute("p", "p는 문단");
        return "index";
    }

}
