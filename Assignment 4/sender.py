import socket
import uuid



def send_message(message_text, message_id=None):

    if message_id is None:
        message_id = uuid.uuid4()

    
    bcast_msg = f"{message_id}:{message_text}".encode()
    team_number = 6

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    broadcast_address = "192.168.210.255"
    broadcast_port = 5000 + team_number

    for i in range(10):
        sock.sendto(bcast_msg, (broadcast_address, broadcast_port))

    print(f"Sent message: {bcast_msg} to {broadcast_address}:{broadcast_port}")
    sock.close()


if __name__ == "__main__":
    send_message("Hello, world, prepare for KONSEQUENZEN!")