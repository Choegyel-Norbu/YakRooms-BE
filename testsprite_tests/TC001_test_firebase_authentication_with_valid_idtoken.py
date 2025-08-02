import requests

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_firebase_authentication_with_valid_idtoken():
    url = f"{BASE_URL}/auth/firebase"
    headers = {
        "Content-Type": "application/json"
    }
    # Since Firebase auth is mocked, we can use any valid-looking idToken string
    payload = {
        "idToken": "mock-valid-id-token"
    }
    try:
        response = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
        response.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    assert response.status_code == 200, f"Expected status code 200, got {response.status_code}"
    json_response = response.json()
    assert isinstance(json_response, dict), "Response is not a JSON object"
    # The response should contain a JWT token, typically under a key like 'token' or 'jwt'
    # Since the schema references JwtLoginResponse, we expect a JWT token string in response
    # We check for presence of a token key and that it is a non-empty string
    token = json_response.get("token") or json_response.get("jwt") or json_response.get("accessToken")
    assert token is not None, "JWT token not found in response"
    assert isinstance(token, str) and len(token) > 0, "JWT token is empty or not a string"

test_firebase_authentication_with_valid_idtoken()