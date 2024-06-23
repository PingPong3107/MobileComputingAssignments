import socket
import sys
from utils import MessageTypes, generate_message, event_logger, get_local_ip
import json


def send_message_to(message: bytes, address: str):
    """
    Send out a message to a specified address.
    """
    sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    team_number: int = 6
    port: int = 5000 + team_number
    burst_message(sock, message, address, port)
    event_logger(f"Sent message: {message} to {address}:{port}")
    sock.close()



def broadcast_message(message: bytes):
    """
    Send out a message to the broadcast address.
    """
    sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    team_number: int = 6
    broadcast_address: str = "192.168.210.255"
    broadcast_port: int = 5000 + team_number
    burst_message(sock, message, broadcast_address, broadcast_port)
    event_logger(f"Broadcast message: {message} to {broadcast_address}:{broadcast_port}")
    sock.close()



def burst_message(
    socket: socket.socket, udp_packet: bytes, addr: str, port: int, burst_count: int = 5
):
    """
    Burst a message for the specified amount of times.
    Default burst count is 5.
    """
    for _ in range(burst_count):
        socket.sendto(udp_packet, (addr, port))


def send_route_response(path: list, uuid):
    """
    If the route request has reached the destination, initiate the route response.
    """
    next_hop = path[-1]
    send_message_to(
        generate_message(next_hop, MessageTypes.ROUTE_RESPONSE, "-",uuid,path=path), next_hop
    )


def forward_route_response(header:dict, data:bytes):
    """
    Forward route response to the predecessor on the path.
    """
    path:list=header['path']
    next_hop=path[path.index(get_local_ip())-1]
    header_json : bytes = json.dumps(header).encode('utf-8')
    packet=header_json + b'\n' + data
    send_message_to(packet, next_hop)


def forward_data(header:dict, data:bytes):
    """
    Forward data message to the successor on the path.
    """
    path:list=header['path']
    next_hop=path[path.index(get_local_ip())+1]
    header_json : bytes = json.dumps(header).encode('utf-8')
    packet=header_json + b'\n' + data
    send_message_to(packet, next_hop)