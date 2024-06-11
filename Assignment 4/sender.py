import json
import socket
import sys
from utils import MessageTypes, get_local_ip, generate_message

def send_message(message: bytes):
    sock : socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    team_number : int = 6
    broadcast_address : str = "192.168.210.255"
    broadcast_port : int = 5000 + team_number

    for _ in range(5):
        sock.sendto(message, (broadcast_address, broadcast_port))

    print(f"Sent message: {message} to {broadcast_address}:{broadcast_port}")
    sock.close()

def forward_message(udp_packet : bytes):

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    team_number = 6

    broadcast_address = "192.168.210.255"
    broadcast_port = 5000 + team_number

    for _ in range(5):
        sock.sendto(udp_packet, (broadcast_address, broadcast_port))

    print(f"Forwarded message: {udp_packet} to {broadcast_address}:{broadcast_port}")
    sock.close()




if __name__ == "__main__":
    header = {
        'uuid': str(uuid4()),  # Beispiel UUID
        'ttl': 10,  # Beispiel TTL
        'source_ip': get_local_ip(),  # Beispiel Quell-IP
        'destination_ip': sys.argv[1],  # Beispiel Ziel-IP
        'type': MessageTypes.ROUTE_REQUEST  # Beispiel Typ
    }    
    messagePayload : str = "Hello, world, prepare for KONSEQUENZEN!"
    test_message: bytes = generate_message(sys.argv[1], MessageTypes.ROUTE_REQUEST, messagePayload)
    send_message(test_message)