import requests
import uuid
import base64
import json

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def decode_jwt_get_user_id(jwt_token):
    try:
        payload_part = jwt_token.split('.')[1]
        # Pad base64 string if necessary
        rem = len(payload_part) % 4
        if rem > 0:
            payload_part += '=' * (4 - rem)
        decoded_bytes = base64.urlsafe_b64decode(payload_part)
        payload = json.loads(decoded_bytes)
        # Typically user id is in 'sub' or 'userId' claim
        if 'userId' in payload:
            return int(payload['userId'])
        elif 'sub' in payload:
            try:
                return int(payload['sub'])
            except ValueError:
                pass
        return None
    except Exception:
        return None

def test_cancel_booking():
    # Step 1: Authenticate to get a valid userId (mock authentication)
    # Using a mock idToken for authentication as Firebase is disabled
    auth_url = f"{BASE_URL}/auth/firebase"
    mock_id_token = "mock-valid-id-token"
    auth_payload = {"idToken": mock_id_token}
    auth_headers = {"Content-Type": "application/json"}

    auth_response = requests.post(auth_url, json=auth_payload, headers=auth_headers, timeout=TIMEOUT)
    assert auth_response.status_code == 200, f"Authentication failed: {auth_response.text}"
    auth_data = auth_response.json()
    jwt_token = None
    if "jwt" in auth_data:
        jwt_token = auth_data["jwt"]
    elif "token" in auth_data:
        jwt_token = auth_data["token"]
    assert jwt_token is not None, "JWT token not found in authentication response"

    user_id = decode_jwt_get_user_id(jwt_token)
    assert user_id is not None, "Failed to extract userId from JWT token"

    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Content-Type": "application/json"
    }

    # Step 2: Create a hotel for the user (required to create room and booking)
    hotel_url = f"{BASE_URL}/api/hotels/{user_id}"
    hotel_payload = {
        "name": f"Test Hotel {uuid.uuid4()}",
        "address": "123 Test St",
        "district": "Test District",
        "hotelType": "Test Type",
        "phone": "1234567890",
        "email": f"testhotel{uuid.uuid4()}@example.com",
        "description": "Test hotel description",
        "amenities": ["WiFi", "Parking"],
        "photos": []
    }
    hotel_response = requests.post(hotel_url, json=hotel_payload, headers=headers, timeout=TIMEOUT)
    assert hotel_response.status_code == 200, f"Hotel creation failed: {hotel_response.text}"

    # Get created hotel ID by fetching hotel by userId
    get_hotel_response = requests.get(hotel_url, headers=headers, timeout=TIMEOUT)
    assert get_hotel_response.status_code == 200, f"Get hotel failed: {get_hotel_response.text}"
    hotel_data = get_hotel_response.json()
    hotel_id = hotel_data.get("id")
    assert hotel_id is not None, "Hotel ID not found in response"

    # Step 3: Create a room for the hotel
    room_url = f"{BASE_URL}/api/rooms"
    room_payload = {
        "hotelId": hotel_id,
        "name": f"Test Room {uuid.uuid4()}",
        "roomType": "Single",
        "price": 100.0,
        "description": "Test room description",
        "photos": [],
        "capacity": 1
    }
    room_response = requests.post(room_url, json=room_payload, headers=headers, timeout=TIMEOUT)
    assert room_response.status_code == 200, f"Room creation failed: {room_response.text}"

    # Get created room ID by listing rooms for hotel
    get_rooms_url = f"{BASE_URL}/api/rooms/hotel/{hotel_id}"
    get_rooms_response = requests.get(get_rooms_url, headers=headers, timeout=TIMEOUT)
    assert get_rooms_response.status_code == 200, f"Get rooms failed: {get_rooms_response.text}"
    rooms = get_rooms_response.json()
    assert isinstance(rooms, list) and len(rooms) > 0, "No rooms found for hotel"
    room_id = rooms[0].get("id")
    assert room_id is not None, "Room ID not found"

    # Step 4: Create a booking for the room
    booking_url = f"{BASE_URL}/api/bookings"
    booking_payload = {
        "userId": user_id,
        "hotelId": hotel_id,
        "roomId": room_id,
        "checkIn": "2025-08-01",
        "checkOut": "2025-08-05",
        "guests": 1,
        "specialRequests": "None"
    }
    booking_response = requests.post(booking_url, json=booking_payload, headers=headers, timeout=TIMEOUT)
    assert booking_response.status_code == 200, f"Booking creation failed: {booking_response.text}"

    booking_data = booking_response.json()
    booking_id = booking_data.get("id")
    assert booking_id is not None, "Booking ID not found in booking creation response"

    try:
        # Step 5: Cancel the booking using the cancel endpoint
        cancel_url = f"{BASE_URL}/api/bookings/{booking_id}/cancel"
        params = {"userId": user_id}
        cancel_response = requests.post(cancel_url, headers=headers, params=params, timeout=TIMEOUT)
        assert cancel_response.status_code == 200, f"Booking cancellation failed: {cancel_response.text}"

        cancel_data = cancel_response.json()
        # Assuming the response contains a status or message confirming cancellation
        assert "status" in cancel_data or "message" in cancel_data, "Cancellation confirmation missing"
    finally:
        # Cleanup: Delete the booking if still exists (ignore errors)
        try:
            delete_booking_url = f"{BASE_URL}/api/bookings/{booking_id}"
            requests.delete(delete_booking_url, headers=headers, timeout=TIMEOUT)
        except Exception:
            pass

        # Cleanup: Delete the room
        try:
            delete_room_url = f"{BASE_URL}/api/rooms/{room_id}"
            requests.delete(delete_room_url, headers=headers, timeout=TIMEOUT)
        except Exception:
            pass

        # Cleanup: Delete the hotel
        try:
            delete_hotel_url = f"{BASE_URL}/api/hotels/{hotel_id}"
            requests.delete(delete_hotel_url, headers=headers, timeout=TIMEOUT)
        except Exception:
            pass

test_cancel_booking()
