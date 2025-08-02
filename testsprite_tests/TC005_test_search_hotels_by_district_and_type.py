import requests
import uuid

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_search_hotels_by_district_and_type():
    # Since we need verified hotels, create a hotel, verify it, then search
    # Mock userId for hotel creation (assuming 1 for test)
    user_id = 1

    # Sample hotel data for creation
    hotel_data = {
        "name": f"Test Hotel {uuid.uuid4()}",
        "address": "123 Test St",
        "district": "TestDistrict",
        "hotelType": "TestType",
        "description": "A hotel for testing",
        "phone": "1234567890",
        "email": "testhotel@example.com",
        "amenities": ["WiFi", "Pool"],
        "photos": []
    }

    headers = {"Content-Type": "application/json"}

    hotel_id = None
    try:
        # Create hotel
        create_resp = requests.post(
            f"{BASE_URL}/api/hotels/{user_id}",
            json=hotel_data,
            headers=headers,
            timeout=TIMEOUT
        )
        assert create_resp.status_code == 200, f"Hotel creation failed: {create_resp.text}"
        created_hotel = create_resp.json()
        # The API doc does not specify response schema for creation, assume it returns hotel with id
        # If not, we need to get hotel list or details to find id
        # Try to get hotel id from response or fallback to search by name
        if isinstance(created_hotel, dict) and "id" in created_hotel:
            hotel_id = created_hotel["id"]
        else:
            # fallback: get all hotels and find by name
            list_resp = requests.get(f"{BASE_URL}/api/hotels", timeout=TIMEOUT)
            assert list_resp.status_code == 200
            hotels = list_resp.json()
            for h in hotels:
                if h.get("name") == hotel_data["name"]:
                    hotel_id = h.get("id")
                    break
            assert hotel_id is not None, "Created hotel ID not found"

        # Verify the hotel by admin
        verify_resp = requests.post(
            f"{BASE_URL}/api/hotels/{hotel_id}/verify",
            timeout=TIMEOUT
        )
        assert verify_resp.status_code == 200, f"Hotel verification failed: {verify_resp.text}"

        # Now search hotels by district and hotelType with pagination
        params = {
            "district": hotel_data["district"],
            "hotelType": hotel_data["hotelType"],
            "page": 0,
            "size": 10
        }
        search_resp = requests.get(
            f"{BASE_URL}/api/hotels/search",
            params=params,
            timeout=TIMEOUT
        )
        assert search_resp.status_code == 200, f"Hotel search failed: {search_resp.text}"
        search_results = search_resp.json()
        assert isinstance(search_results, list), "Search results should be a list"

        # Validate that all returned hotels match district, hotelType and are verified
        for hotel in search_results:
            assert hotel.get("district") == hotel_data["district"], "Hotel district mismatch"
            assert hotel.get("hotelType") == hotel_data["hotelType"], "Hotel type mismatch"
            # Assuming 'verified' field indicates verification status
            assert hotel.get("verified") is True, "Hotel is not verified"

        # Validate pagination: size should not exceed requested size
        assert len(search_results) <= params["size"], "Returned more hotels than page size"

    finally:
        # Cleanup: delete the created hotel if possible
        if hotel_id is not None:
            # No delete endpoint specified in PRD for hotels, so skip deletion
            # If delete endpoint existed, we would call it here
            pass

test_search_hotels_by_district_and_type()