import requests

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

def test_get_all_hotels_with_pagination():
    url = f"{BASE_URL}/api/hotels"
    params = {
        "page": 1,
        "size": 5
    }
    headers = {
        "Accept": "application/json"
    }
    try:
        response = requests.get(url, headers=headers, params=params, timeout=TIMEOUT)
        response.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    assert response.status_code == 200, f"Expected status code 200 but got {response.status_code}"
    try:
        data = response.json()
    except ValueError:
        assert False, "Response is not valid JSON"

    assert isinstance(data, dict) or isinstance(data, list), "Response JSON should be a dict or list"

    if isinstance(data, dict):
        # Check if 'content' or 'items' keys exist and are lists
        if 'content' in data:
            hotels_list = data['content']
            assert isinstance(hotels_list, list), "'content' should be a list"
        elif 'items' in data:
            hotels_list = data['items']
            assert isinstance(hotels_list, list), "'items' should be a list"
        else:
            # Neither key present, fallback check if the dict itself represents a single hotel or empty
            assert False, "Paginated response should contain 'content' or 'items' key"

        assert len(hotels_list) <= params["size"], f"Number of hotels returned {len(hotels_list)} exceeds page size {params['size']}"
    else:
        # If response is a list, check length
        assert len(data) <= params["size"], f"Number of hotels returned {len(data)} exceeds page size {params['size']}"

test_get_all_hotels_with_pagination()
