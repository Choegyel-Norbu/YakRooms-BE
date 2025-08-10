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

		String htmlContent = templateEngine.process("verify-hotel", context);

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

	public void sendBookingNotificationEmail(String toEmail, String hotelName, String roomNumber, String guestName, Long bookingId, LocalDate checkInDate, LocalDate checkOutDate, String guestEmail) {
		Context context = new Context();
		context.setVariable("hotelName", hotelName);
		context.setVariable("roomNumber", roomNumber);
		context.setVariable("guestName", guestName);
		context.setVariable("bookingId", bookingId);
		context.setVariable("checkInDate", checkInDate);
		context.setVariable("checkOutDate", checkOutDate);
		context.setVariable("guestEmail", guestEmail);

		String htmlContent = templateEngine.process("booking-notification", context);

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

	public void sendPasscodeEmailToGuest(String toEmail, String guestName, String passcode, String hotelName, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate, Long bookingId) {
		try {
			Context context = new Context();
			context.setVariable("guestName", guestName);
			context.setVariable("passcode", passcode);
			context.setVariable("hotelName", hotelName);
			context.setVariable("roomNumber", roomNumber);
			context.setVariable("checkInDate", checkInDate);
			context.setVariable("checkOutDate", checkOutDate);
			context.setVariable("bookingId", bookingId);

			String htmlContent = templateEngine.process("booking-passcode", context);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject("YakRooms: Your Booking Passcode");
			helper.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (Exception e) {
			// Fallback: Send simple text email
			try {
				String fallbackContent = String.format(
					"Hello %s,\n\n" +
					"Your booking passcode for %s, Room %s is: %s\n\n" +
					"Check-in: %s\n" +
					"Check-out: %s\n" +
					"Booking ID: %d\n\n" +
					"Best regards,\nYakRooms Team",
					guestName, hotelName, roomNumber, passcode, checkInDate, checkOutDate, bookingId
				);
				
				MimeMessage fallbackMessage = mailSender.createMimeMessage();
				MimeMessageHelper fallbackHelper = new MimeMessageHelper(fallbackMessage, false, "UTF-8");
				
				fallbackHelper.setTo(toEmail);
				fallbackHelper.setSubject("YakRooms: Your Booking Passcode");
				fallbackHelper.setText(fallbackContent, false);
				
				mailSender.send(fallbackMessage);
			} catch (Exception fallbackException) {
				throw new IllegalStateException("Failed to send passcode email (both template and fallback failed)", e);
			}
		}
	}
}
