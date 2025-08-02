import requests
import uuid

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_create_new_room():
    # Sample valid room data based on typical RoomRequest schema inferred from PRD
    room_data = {
        "roomType": "DELUXE",
        "roomNumber": str(uuid.uuid4())[:8],  # Unique room number
        "price": 150.0,
        "description": "A deluxe room with sea view",
        "capacity": 2,
        "amenities": ["WiFi", "Air Conditioning", "TV"],
        "photos": [
            "https://example.com/photos/room1.jpg",
            "https://example.com/photos/room2.jpg"
        ]
    }

    created_room_id = None
    try:
        # Create a new room
        response = requests.post(
            f"{BASE_URL}/api/rooms",
            json=room_data,
            timeout=TIMEOUT
        )
        assert response.status_code == 200, f"Expected status code 200, got {response.status_code}"
        response_json = response.json()
        # Validate response contains an ID
        assert "id" in response_json, "Response JSON does not contain room ID"
        created_room_id = response_json["id"]
        assert created_room_id is not None, "Room ID is None"

        # Optionally, get the room by ID to verify creation
        get_response = requests.get(
            f"{BASE_URL}/api/rooms/{created_room_id}",
            timeout=TIMEOUT
        )
        assert get_response.status_code == 200, f"Expected status code 200 on GET, got {get_response.status_code}"
        room_info = get_response.json()
        assert room_info.get("id") == created_room_id, "Room ID mismatch in GET response"
        assert room_info.get("roomNumber") == room_data["roomNumber"], "Room number mismatch"
        assert room_info.get("price") == room_data["price"], "Room price mismatch"
        assert room_info.get("capacity") == room_data["capacity"], "Room capacity mismatch"

    finally:
        # Cleanup: delete the created room if it was created
        if created_room_id is not None:
            delete_response = requests.delete(
                f"{BASE_URL}/api/rooms/{created_room_id}",
                timeout=TIMEOUT
            )
            assert delete_response.status_code == 200, f"Failed to delete room with ID {created_room_id}"

test_create_new_room()
