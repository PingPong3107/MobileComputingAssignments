import re
import subprocess
from uuid import uuid4
import json
from datetime import datetime

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
    result = subprocess.run(['ip', 'addr', 'show', 'wlan0'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    ip_pattern = re.compile(r'inet (\d+\.\d+\.\d+\.\d+)/\d+')
    ips : list[str] = ip_pattern.findall(result.stdout)

    for ip in ips:
        if ip.startswith("192.168."):
            return ip
    
    raise Exception("No local IP found")


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

def event_logger(message: str):
    current_time=datetime.now().strftime("%H:%M:%S")
    print(f"{current_time}: {message}")