import requests
import uuid

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_create_new_hotel_with_valid_data():
    # Mock userId for testing
    user_id = 12345

    # Sample valid hotel registration data based on typical hotel registration fields
    hotel_data = {
        "name": f"Test Hotel {uuid.uuid4()}",
        "address": "123 Test Street",
        "district": "Test District",
        "hotelType": "Boutique",
        "phone": "+1234567890",
        "email": "contact@testhotel.com",
        "description": "A lovely test hotel for automation testing.",
        "amenities": ["Free WiFi", "Pool", "Gym"],
        "photos": [
            "https://example.com/photo1.jpg",
            "https://example.com/photo2.jpg"
        ]
    }

    headers = {
        "Content-Type": "application/json"
    }

    hotel_id = None
    try:
        # Create new hotel
        response = requests.post(
            f"{BASE_URL}/api/hotels/{user_id}",
            json=hotel_data,
            headers=headers,
            timeout=TIMEOUT
        )
        assert response.status_code == 200, f"Expected status code 200, got {response.status_code}"
        # The response body is not specified, so just check success status

        # Optionally, get all hotels and verify the new hotel is listed (if API supports)
        # Or get hotel by userId and verify details
        get_response = requests.get(
            f"{BASE_URL}/api/hotels/{user_id}",
            headers=headers,
            timeout=TIMEOUT
        )
        assert get_response.status_code == 200, f"Expected status code 200 on get, got {get_response.status_code}"
        hotel_info = get_response.json()
        # Validate that the returned hotel info matches the created data (at least name and email)
        assert hotel_info.get("name") == hotel_data["name"], "Hotel name mismatch"
        assert hotel_info.get("email") == hotel_data["email"], "Hotel email mismatch"

        # If hotel ID is returned in get_response, store it for cleanup
        hotel_id = hotel_info.get("id")

    finally:
        # Cleanup: delete the created hotel if hotel_id is available
        if hotel_id:
            try:
                del_response = requests.delete(
                    f"{BASE_URL}/api/hotels/{hotel_id}",
                    headers=headers,
                    timeout=TIMEOUT
                )
                # Accept 200 or 204 as success for delete
                assert del_response.status_code in (200, 204), f"Failed to delete hotel with id {hotel_id}"
            except Exception:
                pass

test_create_new_hotel_with_valid_data()
