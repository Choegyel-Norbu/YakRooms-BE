import requests

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_verify_checkin_passcode():
    url = f"{BASE_URL}/api/passcode/verify"
    headers = {
        "Content-Type": "application/json"
    }

    # Since no passcode or booking ID is provided, we test with a sample passcode.
    # In a real scenario, we might need to create a booking and get a valid passcode.
    # Here, we test both a valid and invalid passcode scenario.

    # Example payload for passcode verification (assuming passcode and bookingId fields)
    # The PRD does not specify exact fields, so we assume typical fields:
    # {
    #   "bookingId": int,
    #   "passcode": str
    # }
    # We'll test with an invalid passcode first.

    invalid_payload = {
        "bookingId": 999999,  # Assuming this booking does not exist
        "passcode": "INVALIDPASS"
    }

    try:
        response = requests.post(url, json=invalid_payload, headers=headers, timeout=TIMEOUT)
        assert response.status_code == 200, f"Expected status code 200, got {response.status_code}"
        json_response = response.json()
        # Expecting a verification result, likely a boolean or status field
        assert "verified" in json_response or "success" in json_response or "result" in json_response, \
            "Response JSON does not contain expected verification result field"
        # The invalid passcode should not verify successfully
        verified = json_response.get("verified", json_response.get("success", json_response.get("result", None)))
        assert verified is False or verified == "false" or verified == 0, "Invalid passcode should not verify"
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    # For a valid passcode test, we need to create a booking and get a valid passcode.
    # Since no booking creation details are provided here, we skip that part.
    # If available, implement booking creation and passcode retrieval here.

test_verify_checkin_passcode()