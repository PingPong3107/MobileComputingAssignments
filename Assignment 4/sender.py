import socket
import sys
from utils import MessageTypes, generate_message, event_logger


def send_message_to(message: bytes, address: str):
    sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    team_number: int = 6
    port: int = 5000 + team_number

    burst_message(sock, message, address, port)

    event_logger(f"Sent message: {message} to {address}:{port}")

    sock.close()


def send_message(message: bytes):
    sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    team_number: int = 6
    broadcast_address: str = "192.168.210.255"
    broadcast_port: int = 5000 + team_number

    burst_message(sock, message, broadcast_address, broadcast_port)

    event_logger(f"Sent message: {message} to {broadcast_address}:{broadcast_port}")

    sock.close()


def forward_message(udp_packet: bytes):

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    team_number = 6

    broadcast_address = "192.168.210.255"
    broadcast_port = 5000 + team_number

    burst_message(sock, udp_packet, broadcast_address, broadcast_port)

    event_logger(
        f"Forwarded message: {udp_packet} to {broadcast_address}:{broadcast_port}"
    )
    sock.close()


def burst_message(
    socket: socket.socket, udp_packet: bytes, addr: str, port: int, burst_count: int = 5
):
    for _ in range(burst_count):
        socket.sendto(udp_packet, (addr, port))


if __name__ == "__main__":
    messagePayload: str = "Hello, world, prepare for KONSEQUENZEN!"
    test_message: bytes = generate_message(
        destination=sys.argv[1],
        msg_type=MessageTypes.ROUTE_REQUEST,
        payload=messagePayload,
    )
    send_message(test_message)
