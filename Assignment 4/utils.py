import re
import subprocess
import json
from datetime import datetime


class MessageTypes:
    UNDEFINED = 0
    ROUTE_REQUEST = 1
    ROUTE_RESPONSE = 2
    DATA = 3
    SEND_REQUEST = 4
    FLOODING = 5
    FLOOD_REQUEST = 6


def get_local_ip() -> str:
    """
    Find the local IP of this device.
    Method created by ChatGPT.
    """
    result = subprocess.run(
        ["ip", "addr", "show", "wlan0"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    ip_pattern = re.compile(r"inet (\d+\.\d+\.\d+\.\d+)/\d+")
    ips: list[str] = ip_pattern.findall(result.stdout)

    for ip in ips:
        if ip.startswith("192.168."):
            return ip

    raise Exception("No local IP found")


def generate_message(
    destination: str, msg_type: int, payload: str, uuid, ttl=10, path: list = None
) -> bytes:
    """
    Generate a message by creating header and encoding in utf-8.
    """
    header: dict = generate_msg_header(destination, msg_type, ttl, uuid, path)
    header_json: bytes = json.dumps(header).encode("utf-8")
    return header_json + b"\n" + payload.encode("utf-8")


def generate_msg_header(
    destination: str, msg_type: int, ttl: int, uuid, path: list = None
) -> dict:
    """
    Generate a message header.
    If no path is given, set a list containing only the local IP as path.
    """
    local_ip = get_local_ip()
    if path == None:
        path = [local_ip]
    return {
        "uuid": uuid,
        "ttl": ttl,
        "source_ip": local_ip,
        "destination_ip": destination,
        "type": msg_type,
        "path": path,
    }


def event_logger(message: str):
    """
    Log a message together with the current time.
    """
    current_time = datetime.now().strftime("%H:%M:%S:%f")
    print(f"{current_time}: {message}")
