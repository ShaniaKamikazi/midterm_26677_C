package auca.ac.rw.FinanceTracker.controller;

import auca.ac.rw.FinanceTracker.DTO.ApiResponse;
import auca.ac.rw.FinanceTracker.DTO.EmailDetails;
import auca.ac.rw.FinanceTracker.repository.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/sendMail")
    public ResponseEntity<ApiResponse<String>> sendMail(@RequestBody EmailDetails details) {
        String status = emailService.sendSimpleMail(details);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/sendMailWithAttachment")
    public ResponseEntity<ApiResponse<String>> sendMailWithAttachment(@RequestBody EmailDetails details) {
        String status = emailService.sendMailWithAttachment(details);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}