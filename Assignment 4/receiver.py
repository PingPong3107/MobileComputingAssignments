import socket

received_message_ids =[]

buffer_size = 1024
team_number = 6

zuccstnso = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

receiver_port = 5000 + team_number
zuccstnso.bind(('', receiver_port))

while True:
    data, addr = zuccstnso.recvfrom(buffer_size)
    message = data.decode()
    message_id, message_text = message.split(":", 1)
    if message_id not in received_message_ids:
        received_message_ids.append(message_id)
        print(f"Received new message: {data} from {addr}")
    print(f"Received duplicate message: {data} from {addr}")
    