package com.yakrooms.be.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	private final JavaMailSender mailSender;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

	@Value("${spring.mail.username}")
	private String fromEmail;

	public MailService(JavaMailSender mailSender) {
		super();
		this.mailSender = mailSender;
	}

	public void sendHotelVerificationEmail(String toEmail, String hotelName) {
		String htmlContent = generateHotelVerificationHtml(hotelName);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(toEmail);
			helper.setSubject("YakRooms: Your Hotel Listing is Verified");
			helper.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send verification email", e);
		}
	}

	public void sendBookingNotificationEmail(String toEmail, String hotelName, String roomNumber, String guestName, Long bookingId, LocalDate checkInDate, LocalDate checkOutDate, String guestEmail) {
		String htmlContent = generateBookingNotificationHtml(hotelName, roomNumber, guestName, bookingId, checkInDate, checkOutDate, guestEmail);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(toEmail);
			helper.setSubject("YakRooms: New Booking Alert!");
			helper.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send booking notification email", e);
		}
	}

	public void sendHotelDeletionRequestEmail(String toEmail, String hotelName, String hotelOwnerName, String deletionReason) {
		String htmlContent = generateHotelDeletionRequestHtml(hotelName, hotelOwnerName, deletionReason);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(toEmail);
			helper.setSubject("YakRooms: Hotel Deletion Request - " + hotelName);
			helper.setText(htmlContent, true); // true = isHtml

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send hotel deletion request email", e);
		}
	}

	public void sendPasscodeEmailToGuest(String toEmail, String guestName, String passcode, String hotelName, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate, Long bookingId) {
		try {
			String htmlContent = generatePasscodeEmailHtml(guestName, passcode, hotelName, roomNumber, checkInDate, checkOutDate, bookingId);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromEmail);
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
				
				fallbackHelper.setFrom(fromEmail);
				fallbackHelper.setTo(toEmail);
				fallbackHelper.setSubject("YakRooms: Your Booking Passcode");
				fallbackHelper.setText(fallbackContent, false);
				
				mailSender.send(fallbackMessage);
			} catch (Exception fallbackException) {
				throw new IllegalStateException("Failed to send passcode email (both HTML and fallback failed)", e);
			}
		}
	}

	private String generatePasscodeEmailHtml(String guestName, String passcode, String hotelName, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate, Long bookingId) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>Booking Passcode - YakRooms</title>
				<style>
					body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					.container { max-width: 600px; margin: 0 auto; padding: 20px; }
					.header { text-align: center; margin-bottom: 30px; }
					.logo { font-size: 24px; font-weight: bold; }
					.logo-yak { color: #667eea; }
					.logo-rooms { color: #EAB308; }
					.passcode-box { background-color: #fef3c7; border: 2px solid #EAB308; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
					.passcode { font-size: 32px; font-weight: bold; color: #EAB308; letter-spacing: 4px; }
					.details { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
					.detail-row { display: flex; justify-content: space-between; margin: 10px 0; }
					.label { font-weight: bold; color: #666; }
					.value { color: #333; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<div class="logo">
							<span class="logo-yak">Yak</span><span class="logo-rooms">Rooms</span>
						</div>
						<h1>Your Booking Passcode</h1>
					</div>
					
					<p>Hello <strong>%s</strong>,</p>
					
					<p>Here's your passcode for your upcoming stay:</p>
					
					<div class="passcode-box">
						<div class="passcode">%s</div>
						<p>Use this passcode to check in at the hotel</p>
					</div>
					
					<div class="details">
						<div class="detail-row">
							<span class="label">Hotel:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Room Number:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Check-in:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Check-out:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Booking ID:</span>
							<span class="value">%d</span>
						</div>
					</div>
					
					<p>Have a wonderful stay!</p>
					
					<p>Best regards,<br>YakRooms Team</p>
				</div>
			</body>
			</html>
			""", guestName, passcode, hotelName, roomNumber, 
			checkInDate.format(DATE_FORMATTER), checkOutDate.format(DATE_FORMATTER), bookingId);
	}

	private String generateHotelVerificationHtml(String hotelName) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>Hotel Verified - YakRooms</title>
				<style>
					body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					.container { max-width: 600px; margin: 0 auto; padding: 20px; }
					.header { text-align: center; margin-bottom: 30px; }
					.logo { font-size: 24px; font-weight: bold; }
					.logo-yak { color: #667eea; }
					.logo-rooms { color: #EAB308; }
					.success-box { background-color: #d1fae5; border: 2px solid #10b981; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<div class="logo">
							<span class="logo-yak">Yak</span><span class="logo-rooms">Rooms</span>
						</div>
						<h1>Hotel Verification Successful!</h1>
					</div>
					
					<div class="success-box">
						<h2>Congratulations!</h2>
						<p>Your hotel listing <strong>%s</strong> has been successfully verified and is now live on YakRooms.</p>
					</div>
					
					<p>Your hotel is now visible to potential guests and ready to receive bookings.</p>
					
					<p>Best regards,<br>YakRooms Team</p>
				</div>
			</body>
			</html>
			""", hotelName);
	}

	private String generateBookingNotificationHtml(String hotelName, String roomNumber, String guestName, Long bookingId, LocalDate checkInDate, LocalDate checkOutDate, String guestEmail) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>New Booking Alert - YakRooms</title>
				<style>
					body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					.container { max-width: 600px; margin: 0 auto; padding: 20px; }
					.header { text-align: center; margin-bottom: 30px; }
					.logo { font-size: 24px; font-weight: bold; }
					.logo-yak { color: #667eea; }
					.logo-rooms { color: #EAB308; }
					.alert-box { background-color: #fef3c7; border: 2px solid #EAB308; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
					.details { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
					.detail-row { display: flex; justify-content: space-between; margin: 10px 0; }
					.label { font-weight: bold; color: #666; }
					.value { color: #333; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<div class="logo">
							<span class="logo-yak">Yak</span><span class="logo-rooms">Rooms</span>
						</div>
						<h1>New Booking Alert!</h1>
					</div>
					
					<div class="alert-box">
						<h2>You have a new booking!</h2>
						<p>A guest has just booked a room at your hotel.</p>
					</div>
					
					<div class="details">
						<div class="detail-row">
							<span class="label">Hotel:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Room Number:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Guest Name:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Guest Email:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Check-in:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Check-out:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Booking ID:</span>
							<span class="value">%d</span>
						</div>
					</div>
					
					<p>Please prepare the room for your guest's arrival.</p>
					
					<p>Best regards,<br>YakRooms Team</p>
				</div>
			</body>
			</html>
			""", hotelName, roomNumber, guestName, guestEmail, 
			checkInDate.format(DATE_FORMATTER), checkOutDate.format(DATE_FORMATTER), bookingId);
	}

	private String generateHotelDeletionRequestHtml(String hotelName, String hotelOwnerName, String deletionReason) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>Hotel Deletion Request - YakRooms</title>
				<style>
					body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					.container { max-width: 600px; margin: 0 auto; padding: 20px; }
					.header { text-align: center; margin-bottom: 30px; }
					.logo { font-size: 24px; font-weight: bold; }
					.logo-yak { color: #667eea; }
					.logo-rooms { color: #EAB308; }
					.alert-box { background-color: #fef2f2; border: 2px solid #ef4444; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
					.details { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
					.detail-row { display: flex; justify-content: space-between; margin: 10px 0; }
					.label { font-weight: bold; color: #666; }
					.value { color: #333; }
					.reason-box { background-color: #fef3c7; border: 1px solid #EAB308; border-radius: 8px; padding: 15px; margin: 15px 0; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<div class="logo">
							<span class="logo-yak">Yak</span><span class="logo-rooms">Rooms</span>
						</div>
						<h1>Hotel Deletion Request</h1>
					</div>
					
					<div class="alert-box">
						<h2>Action Required!</h2>
						<p>A hotel owner has requested to delete their hotel listing.</p>
					</div>
					
					<div class="details">
						<div class="detail-row">
							<span class="label">Hotel Name:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Hotel Owner:</span>
							<span class="value">%s</span>
						</div>
						<div class="detail-row">
							<span class="label">Request Date:</span>
							<span class="value">%s</span>
						</div>
					</div>
					
					<div class="reason-box">
						<h3>Deletion Reason:</h3>
						<p>%s</p>
					</div>
					
					<p><strong>Please review this request and take appropriate action through the admin panel.</strong></p>
					
					<p>Best regards,<br>YakRooms System</p>
				</div>
			</body>
			</html>
			""", hotelName, hotelOwnerName, 
			java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm")),
			deletionReason);
	}
}
