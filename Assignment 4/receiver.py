import json
import socket
from utils import get_local_ip, event_logger, MessageTypes, generate_message
from sender import forward_message, send_message_to


def send_route_response(path: list):
    nextHop = path[-1]
    send_message_to(
        generate_message(nextHop, MessageTypes.ROUTE_RESPONSE, "-"), nextHop
    )


def handle_route_request(header: dict, data: bytes):
    header["ttl"] -= 1
    uuid: str = header["uuid"]
    if uuid not in received_message_ids:
        received_message_ids.append(uuid)
        event_logger(f"Received new route request: {data}")
        if header["destination_ip"] == get_local_ip():
            event_logger(f"Message delivered successfully!")
            send_route_response(header["path"])
        else:
            forward_message(data)


def handle_route_response(header: dict, data:bytes):
    header["ttl"] -= 1
    uuid: str = header["uuid"]
    if uuid not in received_message_ids:
        received_message_ids.append(uuid)
        event_logger(f"Received route response message: {data}")
        if header["path"][0]== get_local_ip():
            event_logger(f"Found Route!")
            save_path_to_routing_table(header["path"])
        else:
            forward_route_response_or_data(header, data)

def forward_route_response_or_data(header:dict, data:bytes):
    path:list=header['path']
    nextHop=path[path.index(get_local_ip)-1]
    header_json : bytes = json.dumps(header).encode('utf-8')
    packet=header_json + b'\n' + data.encode('utf-8')
    send_message_to(packet,nextHop)

def save_path_to_routing_table(path:list):
    routing_table[get_local_ip()]=path

def handle_data_packet(header:dict, data:bytes):
    header["ttl"] -= 1
    uuid: str = header["uuid"]
    if uuid not in received_message_ids:
        received_message_ids.append(uuid)
        event_logger(f"Received data packet: {data}")
        if header["path"][0]== get_local_ip():
            event_logger(f"Received Data from {header['source_ip']}!")
        else:
            forward_route_response_or_data(header, data)


if __name__ == "__main__":
    received_message_ids: list = []
    routing_table:dict = {}

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
            handle_route_response(header=header, data=data)
        elif type == MessageTypes.DATA:
            handle_data_packet()
        elif type == MessageTypes.ROUTE_ERROR:
            pass
        else:
            event_logger(f"Unknown message type: {type}")
