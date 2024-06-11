import json
import socket
from sender import send_message, forward_message

received_message_ids =[]

buffer_size = 1024
team_number = 6

zuccstnso = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

receiver_port = 5000 + team_number
zuccstnso.bind(('', receiver_port))

while True:
    data, addr = zuccstnso.recvfrom(buffer_size)
    header_json, payload = data.split(b'\n', 1)
    header = json.loads(header_json.decode('utf-8'))
    uuid = header.get('uuid', None)
    if uuid and uuid not in received_message_ids:
        received_message_ids.append(uuid)
        print(f"Received new message: {data} from {addr}")
        forward_message(data)

    