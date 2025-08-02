import requests
import uuid
from datetime import datetime, timedelta

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_create_new_booking():
    headers = {
        "Content-Type": "application/json"
    }

    # Step 1: Create a hotel (required resource for room)
    hotel_payload = {
        "name": "Test Hotel " + str(uuid.uuid4()),
        "address": "123 Test St",
        "district": "Test District",
        "hotelType": "STANDARD",
        "description": "A test hotel for booking creation",
        "phone": "1234567890",
        "email": "testhotel@example.com",
        "website": "http://testhotel.example.com",
        "amenities": ["WiFi", "Parking"],
        "photos": []
    }
    # Using a mock userId for hotel creation (assuming 1)
    user_id = 1
    hotel = None
    room = None
    booking = None

    try:
        hotel_resp = requests.post(
            f"{BASE_URL}/api/hotels/{user_id}",
            json=hotel_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert hotel_resp.status_code == 200, f"Hotel creation failed: {hotel_resp.text}"
        hotel = hotel_resp.json()
        hotel_id = hotel.get("id")
        assert hotel_id is not None, "Hotel ID not returned"

        # Step 2: Create a room for the hotel
        room_payload = {
            "hotelId": hotel_id,
            "roomType": "DELUXE",
            "price": 150.0,
            "description": "Deluxe room for testing booking",
            "maxGuests": 2,
            "photos": []
        }
        room_resp = requests.post(
            f"{BASE_URL}/api/rooms",
            json=room_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert room_resp.status_code == 200, f"Room creation failed: {room_resp.text}"
        room = room_resp.json()
        room_id = room.get("id")
        assert room_id is not None, "Room ID not returned"

        # Step 3: Create a booking for the room
        check_in_date = (datetime.now() + timedelta(days=1)).strftime("%Y-%m-%d")
        check_out_date = (datetime.now() + timedelta(days=3)).strftime("%Y-%m-%d")

        booking_payload = {
            "roomId": room_id,
            "userId": user_id,
            "checkIn": check_in_date,
            "checkOut": check_out_date,
            "guests": 2,
            "specialRequests": "None"
        }
        booking_resp = requests.post(
            f"{BASE_URL}/api/bookings",
            json=booking_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert booking_resp.status_code == 200, f"Booking creation failed: {booking_resp.text}"
        booking = booking_resp.json()
        booking_id = booking.get("id")
        assert booking_id is not None, "Booking ID not returned"
        assert booking.get("roomId") == room_id, "Booking roomId mismatch"
        assert booking.get("userId") == user_id, "Booking userId mismatch"
        assert booking.get("checkIn") == check_in_date, "Booking checkIn date mismatch"
        assert booking.get("checkOut") == check_out_date, "Booking checkOut date mismatch"
        assert booking.get("guests") == 2, "Booking guests count mismatch"

    finally:
        # Cleanup: Delete booking if created
        if booking and booking.get("id"):
            try:
                requests.delete(
                    f"{BASE_URL}/api/bookings/{booking['id']}",
                    headers=headers,
                    timeout=TIMEOUT
                )
            except Exception:
                pass

        # Cleanup: Delete room if created
        if room and room.get("id"):
            try:
                requests.delete(
                    f"{BASE_URL}/api/rooms/{room['id']}",
                    headers=headers,
                    timeout=TIMEOUT
                )
            except Exception:
                pass

        # Cleanup: Delete hotel if created
        if hotel and hotel.get("id"):
            try:
                requests.delete(
                    f"{BASE_URL}/api/hotels/{hotel['id']}",
                    headers=headers,
                    timeout=TIMEOUT
                )
            except Exception:
                pass

test_create_new_booking()