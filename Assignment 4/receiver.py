import json
import socket
from utils import get_local_ip, event_logger, MessageTypes, generate_message
from sender import forward_message, send_message_to
from uuid import uuid4
import sys
import threading
from uuid import uuid4
from sender import send_message, send_message_to
from utils import generate_message, MessageTypes, event_logger



def discover_route(destination: str):
    # Create and broadcast a route request message
    discovery_message = generate_message(
        destination, MessageTypes.ROUTE_REQUEST, f"Route to {destination}",str(uuid4())
    )
    event_logger("Jetz wird gefloodet")
    send_message(discovery_message)


"""
Use this method to send a message to the destination via a pre-determined route
"""


def send_routed(destination: str, message_payload: str, routing_table: dict):
    if destination not in routing_table.keys():
        discover_route(destination)
    else:
        send_message_to(
            generate_message(
                destination,
                MessageTypes.DATA,
                message_payload,
                path=routing_table[destination],
                uuid=str(uuid4())
            ),
            routing_table[destination][1],
        )

  




def send_route_response(path: list, uuid):
    nextHop = path[-1]
    send_message_to(
        generate_message(nextHop, MessageTypes.ROUTE_RESPONSE, "-",uuid,path=path), nextHop
    )


def handle_route_request(header: dict, data: bytes):
    header["ttl"] -= 1
    uuid: str = header["uuid"]
    if uuid not in received_message_ids:
        received_message_ids.append(uuid)
        event_logger(f"Received new route request: {data}")
        path:list=header['path']
        if header["destination_ip"] == get_local_ip():
            event_logger(f"I am the destination")
            path.append(get_local_ip())
            send_route_response(header['path'],str(uuid4()))
        else:
          
            event_logger(f"path ist {header['path']}")
            path.append(get_local_ip())
            header_json : bytes = json.dumps(header).encode('utf-8')
            packet=header_json + b'\n' + data
            forward_message(packet)


def handle_route_response(header: dict, data:bytes):
    header["ttl"] -= 1
    uuid: str = header["uuid"]
    if uuid not in received_message_ids:
        received_message_ids.append(uuid)
        event_logger(f"Received route response message: {data}")
        if header["path"][0]== get_local_ip():
            event_logger(f"Found Route!")
            save_path_to_routing_table(header["path"])
            send_routed(sys.argv[2], string_to_sand, routes)
        else:
            forward_route_response_or_data(header, data)

def forward_route_response_or_data(header:dict, data:bytes):
    path:list=header['path']
    nextHop=path[path.index(get_local_ip())-1]
    header_json : bytes = json.dumps(header).encode('utf-8')
    packet=header_json + b'\n' + data
    send_message_to(packet,nextHop)

def save_path_to_routing_table(path:list):
    routes[get_local_ip()]=path

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
    routes: dict = {}
    buffer_size: int = 1024
    team_number: int = 6

    sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    receiver_port: int = 5000 + team_number
    sock.bind(("", receiver_port))

    event_logger(f"Receiver is listening on port {receiver_port}")
    if sys.argv[1] == "source":
        string_to_sand="Verbum Domini manet in aeternum!"
        send_routed(sys.argv[2], string_to_sand, routes)

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
            handle_data_packet(header=header,data=data)
        elif type == MessageTypes.ROUTE_ERROR:
            pass
        else:
            event_logger(f"Unknown message type: {type}")
