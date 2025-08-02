import requests
import json

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_verify_hotel_by_admin():
    # Mock admin authentication (assuming mock auth returns a JWT token for admin)
    auth_url = f"{BASE_URL}/auth/firebase"
    mock_id_token = "mock-admin-id-token"
    auth_payload = {"idToken": mock_id_token}
    headers = {"Content-Type": "application/json"}

    try:
        auth_response = requests.post(auth_url, json=auth_payload, headers=headers, timeout=TIMEOUT)
        assert auth_response.status_code == 200, f"Authentication failed: {auth_response.text}"
        jwt_token = auth_response.json().get("token")
        assert jwt_token, "JWT token not found in authentication response"
    except Exception as e:
        raise AssertionError(f"Authentication request failed: {e}")

    auth_headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Content-Type": "application/json"
    }

    # Step 1: Create a new hotel to verify
    # We need a userId for hotel creation; create a mock user or use a fixed userId for admin
    # For this test, assume userId=1 (admin user) is valid for hotel creation
    user_id = 1
    create_hotel_url = f"{BASE_URL}/api/hotels/{user_id}"
    hotel_data = {
        "name": "Test Hotel for Verification",
        "address": "123 Test St",
        "district": "Test District",
        "hotelType": "Test Type",
        "phone": "1234567890",
        "email": "testhotel@example.com",
        "description": "A hotel created for verification test",
        "amenities": ["WiFi", "Parking"],
        "photos": []
    }

    hotel_id = None
    try:
        create_response = requests.post(create_hotel_url, json=hotel_data, headers=auth_headers, timeout=TIMEOUT)
        assert create_response.status_code == 200, f"Hotel creation failed: {create_response.text}"
        created_hotel = create_response.json()
        hotel_id = created_hotel.get("id")
        assert hotel_id is not None, "Created hotel ID not found in response"

        # Step 2: Verify the hotel by admin using the verify endpoint
        verify_url = f"{BASE_URL}/api/hotels/{hotel_id}/verify"
        verify_response = requests.post(verify_url, headers=auth_headers, timeout=TIMEOUT)
        assert verify_response.status_code == 200, f"Hotel verification failed: {verify_response.text}"

        # Optionally check response content if any success message or status
        verify_json = verify_response.json()
        # Assuming response contains a message or status field indicating success
        assert ("success" in verify_json.get("message", "").lower()) or ("verified" in verify_json.get("message", "").lower()) or verify_response.text.lower().find("success") != -1, \
            "Verification success message not found in response"

    finally:
        # Cleanup: Delete the created hotel to keep test environment clean
        if hotel_id is not None:
            delete_url = f"{BASE_URL}/api/hotels/{hotel_id}"
            try:
                requests.delete(delete_url, headers=auth_headers, timeout=TIMEOUT)
            except Exception:
                pass

test_verify_hotel_by_admin()