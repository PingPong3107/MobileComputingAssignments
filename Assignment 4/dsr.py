import sys
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


routes: dict = {}

if __name__ == "__main__":
    # Generate a dicitionary, which maps an IP-address (type: String) to a neighbouring node
    # If this node has been determined to be the source node in this network, send a message to the specified destination
    if sys.argv[1] == "source":
        send_routed(sys.argv[2], "Verbum Domini manet in aeternum!", routes)
