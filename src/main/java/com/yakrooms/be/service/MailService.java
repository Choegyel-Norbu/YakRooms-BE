package com.yakrooms.be.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;

	@Autowired
	public MailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
		super();
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	public void sendHotelVerificationEmail(String toEmail, String hotelName) {
		Context context = new Context();
		context.setVariable("hotelName", hotelName);

		String htmlContent = templateEngine.process("verify-hotel.html", context);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject("YakRooms: Your Hotel Listing is Verified");
			helper.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send verification email", e);
		}
	}

	public void sendBookingNotificationEmail(String toEmail, String hotelName, String roomNumber, String guestName) {
		Context context = new Context();
		context.setVariable("hotelName", hotelName);
		context.setVariable("roomNumber", roomNumber);
		context.setVariable("guestName", guestName);

		String htmlContent = templateEngine.process("booking-notification.html", context);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject("YakRooms: New Booking Alert!");
			helper.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send booking notification email", e);
		}
	}

	public void sendPasscodeEmailToGuest(String toEmail, String guestName, String passcode, String hotelName, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
		Context context = new Context();
		context.setVariable("guestName", guestName);
		context.setVariable("passcode", passcode);
		context.setVariable("hotelName", hotelName);
		context.setVariable("roomNumber", roomNumber);
		context.setVariable("checkInDate", checkInDate);
		context.setVariable("checkOutDate", checkOutDate);

		String htmlContent = templateEngine.process("booking-passcode.html", context);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject("YakRooms: Your Booking Passcode");
			helper.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send passcode email", e);
		}
	}
}
