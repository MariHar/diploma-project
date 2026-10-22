//package com.example.diplomathesisfinalcollection.user;
//
//
////import com.example.medicalweb.team.TeamRepository;
//import com.example.diplomathesisfinalcollection.team.TeamRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/profile")
//
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private TeamRepository teamRepository;
//
//
//    @GetMapping("/register")
//    public String signUpPage() {
//        return "register";
//    }
//
//
//    public UserController(UserService userService) {
//        this.userService = userService;
//    }
//
//    @PostMapping("/signup")
//    public String signUp(
//            @RequestParam String firstName,
//            @RequestParam String lastName,
//            @RequestParam String email,
//            @RequestParam String username,
//            @RequestParam String password
//    ) {
//        User user = new User();
//        user.setEmail(email);
//        user.setUsername(username);
//        user.setPasswordHash(password);
//
//        UserProfile profile = new UserProfile();
//        profile.setFirstName(firstName);
//        profile.setLastName(lastName);
//
//        user.setProfile(profile);
//
//        User saveduser = userService.register(user);
//
//        return "redirect:/profile/" + saveduser.getId();
//    }
//
//
//    @GetMapping("/login")
//    public String loginPage() {
//        return "login";
//    }
//
//    @PostMapping("/login")
//    public String login(
//            @RequestParam String username,
//            @RequestParam String password,
//            Model model
//    ) {
//        User user = userService.authenticate(username, password);
//
//        if (user != null) {
//            String userRole = user.getRole();
//
//            return "redirect:/profile/" + user.getId();
//        } else {
//            model.addAttribute("error", "Սխալ մուտքանուն կամ գաղտնաբառ։");
//            return "user/login";
//        }
//    }
//
//
//    @PostMapping("/update")
//    public String updateProfile(@ModelAttribute User user) {
//        userService.updateProfile(user);
//        return "redirect:/profile/" + user.getUsername();
//    }
//
//
//    @GetMapping("/{id}")
//    public String viewUserProfile(@PathVariable String id, Model model) {
//        User user = userRepository.findById(id).orElse(null);
//        if (user == null) {
//            model.addAttribute("error", "Օգտատերը չի գտնվել ID: " + id);
//            return "error-page";
//        }
//        if (user.getProfile() == null) user.setProfile(new UserProfile());
//        model.addAttribute("user", user);
//        return "profile";
//    }
//
//
//}
