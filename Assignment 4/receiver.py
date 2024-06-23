import json
import socket
from utils import get_local_ip, event_logger, MessageTypes, generate_message
from uuid import uuid4
from sender import broadcast_message, send_message_to, send_route_response, forward_data, forward_route_response
import copy


def discover_route(destination: str):
    """
    Create and broadcast a route request message.
    """
    discovery_message = generate_message(
        destination, MessageTypes.ROUTE_REQUEST, f"Route to {destination}",str(uuid4())
    )
    event_logger("Jetz wird gevladet")
    broadcast_message(discovery_message)


def send_routed(destination: str, message_payload: str, routing_table: dict):
    """
    Use this method to send a message to the destination via a pre-determined route.
    If the route is not determined yet, route discovery will be initiated.
    """
    if destination not in routing_table.keys():
        discover_route(destination)
    else:
        msg = generate_message(
                destination,
                MessageTypes.DATA,
                message_payload,
                path=routing_table[destination],
                uuid=str(uuid4())
            )
        send_message_to(msg, routing_table[destination][1])


def handle_route_request(header: dict, data: bytes):
    """
    When receiving a route request, either forward it, or, if you are the destination, send route response.
    """
    event_logger(f"Received new route request: {data}")
    path:list=header['path']
    path.append(get_local_ip())
    if header["destination_ip"] == get_local_ip():
        event_logger("I am the destination")
        send_route_response(header['path'],str(uuid4()))
    else:
        header_json : bytes = json.dumps(header).encode('utf-8')
        packet=header_json + b'\n' + data
        broadcast_message(packet)


def store_routes(path, local_ip):
    """
    Store all partial paths of the received path in the routing table.
    """
    partial_path = []
    for node in path:
        if partial_path:
            partial_path.append(node)
            routes[node] = copy.deepcopy(partial_path)       
        elif node == local_ip:
            partial_path.append(node)


def handle_route_response(header: dict, data:bytes):
    """
    When receiving a route response, either forward it, or, if you are the source, send the data.
    """
    event_logger(f"Received route response message: {data}")
    local_ip = get_local_ip()
    store_routes(header["path"], local_ip)
    if header["path"][0] == local_ip:
        event_logger("Found Route!")
        send_routed(destination, text_to_send, routes)
    else:
        forward_route_response(header, data)


def handle_data_packet(header:dict, data:bytes):
    """
    When receiving a data packet, either forward it, or, if you are the destination, be happy :)
    """
    event_logger(f"Received data packet: {data}")
    if header["path"][-1]== get_local_ip():
        event_logger(f"Received Data from {header['source_ip']}!")
    else:
        forward_data(header, data)


def handle_send_request(header:dict):
    """
    When receiving a send request, send along the route or discover it.
    """
    send_routed(header["destination_ip"],text_to_send,routes)


def handle_flood_request(header:dict, payload):
    """
    When receiving a flood request, flood the message to the destination.
    """
    msg = generate_message(
                header["destination_ip"],
                MessageTypes.FLOODING,
                payload,
                uuid=str(uuid4())
            )
    broadcast_message(msg)


def handle_flooding(header, payload):
    """
    When receiving a flooding message, either forward it, or, if you are the destination, be happy :)
    """
    event_logger(f"Received flooding message: {payload}")
    if header["destination_ip"] == get_local_ip():
        event_logger(f"Received message {payload} through flooding.")
    else:
        header_json : bytes = json.dumps(header).encode('utf-8')
        packet=header_json + b'\n' + payload
        broadcast_message(packet)


if __name__ == "__main__":
    received_message_ids: list = []
    routes: dict = {}
    buffer_size: int = 1024
    team_number: int = 6
    destination="-"
    text_to_send="-"

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
        else:
            header["ttl"] -= 1
            uuid: str = header["uuid"]
            if uuid not in received_message_ids:
                received_message_ids.append(uuid)
                msg_type = header["type"]

                match msg_type:
                    case MessageTypes.ROUTE_REQUEST:
                        handle_route_request(header=header, data=payload)
                    case MessageTypes.ROUTE_RESPONSE:
                        handle_route_response(header=header, data=payload)
                    case MessageTypes.DATA:
                        handle_data_packet(header=header,data=payload)
                    case MessageTypes.SEND_REQUEST:
                        destination = header["destination_ip"]
                        text_to_send = payload.decode('utf-8')
                        handle_send_request(header=header)
                    case MessageTypes.FLOOD_REQUEST:
                        handle_flood_request(header, payload)
                    case MessageTypes.FLOODING:
                        handle_flooding(header, payload)
                    case _:
                        event_logger(f"Unknown message type: {msg_type}")
