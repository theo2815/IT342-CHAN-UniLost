package edu.cit.chan.unilost.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final Resend resend;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {
        String subject = "UniLost - Password Reset Code";
        String html = """
                <div style="font-family: 'Inter', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0;">
                    <h2 style="color: #1e293b; margin: 0 0 8px;">Password Reset</h2>
                    <p style="color: #475569; font-size: 15px; line-height: 1.6; margin: 0 0 24px;">
                        You requested a password reset for your UniLost account. Use the code below to verify your identity:
                    </p>
                    <div style="background: #f1f5f9; border-radius: 8px; padding: 20px; text-align: center; margin-bottom: 24px;">
                        <span style="font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #1e293b;">%s</span>
                    </div>
                    <p style="color: #64748b; font-size: 13px; line-height: 1.5; margin: 0;">
                        This code expires in <strong>10 minutes</strong>. If you didn't request this, you can safely ignore this email.
                    </p>
                </div>
                """.formatted(otp);

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(options);
            log.info("Password reset OTP sent to {} (id: {})", toEmail, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }
}
