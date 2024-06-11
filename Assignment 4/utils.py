import re
import subprocess
from uuid import UUID, uuid4

class MessageTypes():
    UNDEFINED = 0
    ROUTE_REQUEST = 1
    ROUTE_RESPONSE = 2
    DATA = 3
    ROUTE_ERROR = -1


def get_local_ip() -> str:
    """
    Method created by ChatGPT
    """
    # Execute the ip command to get addresses
    result = subprocess.run(['ip', 'addr', 'show', 'wlan0'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    
    # Regular expression to match IPv4 addresses, excluding the loopback address
    ip_pattern = re.compile(r'inet (\d+\.\d+\.\d+\.\d+)/\d+')
    
    # Search for IP addresses in the command output
    ips : list[str] = ip_pattern.findall(result.stdout)
    
    # Optionally, filter out specific interfaces or address ranges
    for ip in ips:
        if ip.startswith("192.168."):
            return ip
    return "No local IP found"


def generate_message(destination: str, msg_type: int, payload: str, ttl = 5) -> bytes:
    header = generate_msg_header(destination, msg_type, ttl)
    header_json : bytes = json.dumps(header).encode('utf-8')
    return (header_json + b'\n' + payload.encode('utf-8'))

def generate_msg_header(destination: str, msg_type: int, ttl : int) -> dict:
    return {
        'uuid': str(uuid4()),
        'ttl': ttl,
        'source_ip': get_local_ip(),
        'destination_ip': destination,
        'type': msg_type
    }


















































"""
Method created by ChatGPT
"""
def get_local_ip_old():
    # Attempt to find the local IP by connecting to a remote address.
    # The chosen address does not need to be reachable or exist.
    try:
        # Create a temporary socket for this purpose
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as temp_socket:
            # Use Google's public DNS server address for the connection attempt.
            # No actual connection is made.
            temp_socket.connect(('8.8.8.8', 80))
            # Get the socket's own address
            local_ip = temp_socket.getsockname()[0]
            return local_ip
    except Exception as e:
        print(f"Error obtaining local IP: {e}")
        return "Unable to determine local IP"