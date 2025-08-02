import requests
import uuid

BASE_URL = "http://localhost:8080"
TIMEOUT = 30
HEADERS = {"Content-Type": "application/json"}

def test_get_hotel_by_user_id():
    # Since userId is required and not provided, create a new hotel with a new userId, then get it by userId
    user_id = int(uuid.uuid4().int % 100000)  # Generate a pseudo-random userId integer

    hotel_request_payload = {
        "name": "Test Hotel for User {}".format(user_id),
        "address": "123 Test St",
        "district": "Test District",
        "hotelType": "Test Type",
        "phone": "1234567890",
        "email": "testhotel{}@example.com".format(user_id),
        "description": "A test hotel created for testing.",
        "amenities": ["Free WiFi", "Pool"],
        "photos": []
    }

    hotel_id = None
    try:
        # Create hotel for the userId
        create_response = requests.post(
            f"{BASE_URL}/api/hotels/{user_id}",
            json=hotel_request_payload,
            headers=HEADERS,
            timeout=TIMEOUT
        )
        assert create_response.status_code == 200, f"Hotel creation failed: {create_response.text}"

        # Get hotel by userId
        get_response = requests.get(
            f"{BASE_URL}/api/hotels/{user_id}",
            headers=HEADERS,
            timeout=TIMEOUT
        )
        assert get_response.status_code == 200, f"Get hotel by userId failed: {get_response.text}"

        hotel_data = get_response.json()
        # Validate that the returned hotel data matches the created hotel details
        assert hotel_data.get("name") == hotel_request_payload["name"], "Hotel name mismatch"
        assert hotel_data.get("address") == hotel_request_payload["address"], "Hotel address mismatch"
        assert hotel_data.get("district") == hotel_request_payload["district"], "Hotel district mismatch"
        assert hotel_data.get("hotelType") == hotel_request_payload["hotelType"], "Hotel type mismatch"
        assert hotel_data.get("phone") == hotel_request_payload["phone"], "Hotel phone mismatch"
        assert hotel_data.get("email") == hotel_request_payload["email"], "Hotel email mismatch"
        assert hotel_data.get("description") == hotel_request_payload["description"], "Hotel description mismatch"
        assert set(hotel_data.get("amenities", [])) == set(hotel_request_payload["amenities"]), "Hotel amenities mismatch"
        assert isinstance(hotel_data.get("photos"), list), "Hotel photos should be a list"

        # Save hotel id for cleanup if available
        hotel_id = hotel_data.get("id")
    finally:
        # Cleanup: delete the created hotel if hotel_id is available
        if hotel_id is not None:
            try:
                requests.delete(
                    f"{BASE_URL}/api/hotels/{hotel_id}",
                    headers=HEADERS,
                    timeout=TIMEOUT
                )
            except Exception:
                pass

test_get_hotel_by_user_id()