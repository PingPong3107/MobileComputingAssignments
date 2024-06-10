import socket

buffer_size = 1024
team_number = 6

zuccstnso = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

receiver_port = 5000 + team_number
zuccstnso.bind(('', receiver_port))

while True:
    data, addr = zuccstnso.recvfrom(buffer_size)
    print(f"Received message: {data} from {addr}")