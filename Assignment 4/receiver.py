import json
import socket
from utils import get_local_ip, event_logger, MessageTypes
from sender import forward_message

def handle_route_request(header: dict, data: bytes):
    header["ttl"] -= 1
    uuid : str = header["uuid"]
    if uuid not in received_message_ids:
        received_message_ids.append(uuid)
        event_logger(f"Received new message: {data} from {addr}")
        if header["destination_ip"] == get_local_ip():
            event_logger(f"Message delivered successfully!")
            # send_route_response()
        else:
            forward_message(data)

if __name__ == "__main__":
    received_message_ids: list = []

    buffer_size: int = 1024
    team_number: int = 6

    sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    receiver_port: int = 5000 + team_number
    sock.bind(("", receiver_port))

    event_logger(f"Receiver is listening on port {receiver_port}")

    while True:
        data, addr = sock.recvfrom(buffer_size)
        header_json, payload = data.split(b"\n", 1)
        header: dict = json.loads(header_json.decode("utf-8"))

        if header["ttl"] < 1:
            event_logger(f"Message dropped due to TTL: {data}")
            continue
        type: int = header["type"]
    
        if type == MessageTypes.ROUTE_REQUEST:
            handle_route_request(header=header, data=data)
        elif type == MessageTypes.ROUTE_RESPONSE:
            pass
        elif type == MessageTypes.DATA:
            pass
        elif type == MessageTypes.ROUTE_ERROR:
            pass
        else:
            event_logger(f"Unknown message type: {type}")
