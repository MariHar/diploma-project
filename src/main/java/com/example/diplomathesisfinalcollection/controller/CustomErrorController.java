package com.example.diplomathesisfinalcollection.controller; // Համոզվիր որ package-ը ճիշտ է

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Ստանում ենք սխալի կոդը և տեքստը Spring Boot-ից
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = 500;
        String errorMsg = "Անհայտ սխալ է տեղի ունեցել:";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());

            // Թարգմանում ենք ստանդարտ սխալները մարդկային լեզվով
            if (statusCode == 404) {
                errorMsg = "Էջը չի գտնվել (404): Հնարավոր է հղումը սխալ է կամ էջը ջնջվել է:";
            } else if (statusCode == 403) {
                errorMsg = "Մուտքն արգելված է (403): Դուք չունեք բավարար իրավունքներ այս էջը տեսնելու համար:";
            } else if (statusCode == 500) {
                errorMsg = "Սերվերի ներքին սխալ (500): Մեր թիմն արդեն տեղեկացված է և աշխատում է խնդրի շուրջ:";
            }
        }

        // Եթե Spring-ը փոխանցել է կոնկրետ տեքստ, ավելացնում ենք այն
        if (message != null && !message.toString().isEmpty() && !message.toString().equals("No message available")) {
            errorMsg += " Մանրամասն՝ " + message.toString();
        }

        // Ուղարկում ենք մեր նոր ստեղծվելիք error.html-ին
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMsg);

        return "error";
    }
}