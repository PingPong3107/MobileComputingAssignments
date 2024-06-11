import json
import socket

from utils import get_local_ip
from sender import (
    send_message,
    forward_message,
    MessageTypes
)

received_message_ids: list = []

buffer_size: int = 1024
team_number: int = 6

zuccstnso: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

receiver_port: int = 5000 + team_number
zuccstnso.bind(("", receiver_port))

while True:
    data, addr = zuccstnso.recvfrom(buffer_size)
    header_json, payload = data.split(b"\n", 1)
    header: dict = json.loads(header_json.decode("utf-8"))
    new_header: dict = header.copy()
    if new_header["ttl"] < 1:
        continue
    
    new_header["ttl"] -= 1
    uuid : str = header["uuid"]
    if uuid not in received_message_ids:
        received_message_ids.append(uuid)
        print(f"Received new message: {data} from {addr}")
        if new_header["destination_ip"] == get_local_ip():
            print(f"Juhu die Nachricht ist angekommen :)")
        else:
            forward_message(data)
