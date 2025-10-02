package kaiquebt.dev.client.config;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import kaiquebt.dev.auth.service.IEmailTemplateBean;
import kaiquebt.dev.client.model.User;

@Service
public class EmailTemplateBean implements IEmailTemplateBean<User> {
    
    @Override
    public String buildEmailConfirm(User user, String confirmationUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Email Confirmation - Auth Demo</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 0;">
                    
                    <!-- Header -->
                    <div style="background-color: #6c5ce7; padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">Auth Module Demo</h1>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding: 40px 30px;">
                        <h2 style="color: #333; margin-bottom: 20px;">Hello, %s! 👋</h2>
                        
                        <p style="color: #666; font-size: 16px; line-height: 1.6; margin-bottom: 30px;">
                            Welcome to the <strong>KaiqueBT Auth Module</strong> demonstration! 
                            To complete your registration, please confirm your email address.
                        </p>
                        
                        <!-- Call to Action Button -->
                        <div style="text-align: center; margin: 40px 0;">
                            <a href="%s" 
                               style="display: inline-block; background-color: #00b894; color: white; 
                                      text-decoration: none; padding: 15px 40px; border-radius: 8px; 
                                      font-weight: bold; font-size: 16px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                ✅ Confirm Email
                            </a>
                        </div>
                        
                        <!-- Important Info -->
                        <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin: 30px 0;">
                            <h3 style="color: #856404; margin: 0 0 15px 0; font-size: 16px;">⚠️ Important Information:</h3>
                            <ul style="color: #856404; margin: 0; padding-left: 20px;">
                                <li>This link expires in <strong>24 hours</strong></li>
                                <li>Can only be used <strong>once</strong></li>
                                <li>If it doesn't work, request a new confirmation email</li>
                            </ul>
                        </div>
                        
                        <p style="color: #666; font-size: 14px; line-height: 1.6;">
                            If you didn't create an account, you can safely ignore this email.
                        </p>
                        
                        <div style="margin-top: 30px; padding: 15px; background-color: #f8f9fa; border-radius: 5px;">
                            <p style="color: #6c757d; font-size: 13px; margin: 0; text-align: center;">
                                <em>This is a demonstration of the KaiqueBT Authentication Module</em>
                            </p>
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #f8f9fa; padding: 20px 30px; border-top: 1px solid #dee2e6;">
                        <p style="color: #6c757d; font-size: 12px; margin: 0 0 10px 0;">
                            <strong>Button not working?</strong> Copy and paste this link in your browser:
                        </p>
                        <p style="color: #6c5ce7; font-size: 12px; word-break: break-all; margin: 0 0 15px 0;">
                            <a href="%s" style="color: #6c5ce7;">%s</a>
                        </p>
                        
                        <hr style="border: none; border-top: 1px solid #dee2e6; margin: 15px 0;">
                        
                        <p style="color: #6c757d; font-size: 12px; margin: 0; text-align: center;">
                            <strong>KaiqueBT Auth Module</strong> - Authentication Library Demo<br>
                            This is an automated email, please do not reply.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, user.getUsername(), confirmationUrl, confirmationUrl, confirmationUrl);
    }
    
    @Override
    public String getEmailConfirmTitle() {
        return "Client of kaiquebt auth module :D";
    }

    @Override
    public String buildRecoverAccount(User user, String token) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String expiresAt = user.getPasswordRecoverExpiration().format(formatter);


        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recover Your Account - Auth Demo</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 0;">
                    
                    <!-- Header -->
                    <div style="background-color: #6c5ce7; padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">Auth Module Demo</h1>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding: 40px 30px;">
                        <h2 style="color: #333; margin-bottom: 20px;">Hello, %s! 👋</h2>
                        
                        <p style="color: #666; font-size: 16px; line-height: 1.6; margin-bottom: 30px;">
                            We received a request to reset your password. Use the token below to reset your password:
                        </p>
                        
                        <!-- Token Display -->
                        <div style="text-align: center; margin: 40px 0; padding: 20px; background-color: #f8f9fa; border-radius: 8px; border: 1px solid #dee2e6;">
                            <p style="color: #333; font-family: monospace; font-size: 18px; word-break: break-all; margin: 0;">
                                %s
                            </p>
                        </div>
                        
                        <!-- Important Info -->
                        <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin: 30px 0;">
                            <h3 style="color: #856404; margin: 0 0 15px 0; font-size: 16px;">⚠️ Important Information:</h3>
                            <ul style="color: #856404; margin: 0; padding-left: 20px;">
                                <li>This token expires <strong>%s</strong></li>
                                <li>Can only be used <strong>once</strong></li>
                                <li>Keep this token secure and do not share it with anyone</li>
                            </ul>
                        </div>
                        <p style="color: #666; font-size: 14px; line-height: 1.6;">
                            If you didn't request a password reset, you can safely ignore this email.
                        </p>
                        <div style="margin-top: 30px; padding: 15px; background-color: #f8f9fa; border-radius: 5px;">
                            <p style="color: #6c757d; font-size: 13px; margin: 0; text-align: center;">
                                <em>This is a demonstration of the KaiqueBT Authentication Module</em>
                            </p>
                        </div>
                    </div>
                </div>
            </body>
        </html>
        """, user.getUsername(), token, expiresAt);
    }

    @Override
    public String getRecoverAccountTitle() {
        return "Recover your account - Auth Demo";
    }

    

}