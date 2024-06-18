import sys
from sender import send_message
from utils import generate_message, MessageTypes


def discover_route(destination: str, routing_table:dict):
    #Create and broadcast a route request message
    discovery_message = generate_message(destination, MessageTypes.ROUTE_REQUEST, f"Route to {destination}")
    send_message(discovery_message)

"""
Use this method to send a message to the destination via a pre-determined route
"""
def send_routed(destination: str, message_payload: str, routing_table: dict):
    if destination not in routing_table.keys():
        discover_route(destination, routing_table)
    
    

if __name__ == "__main__":
    #Generate a dicitionary, which maps an IP-address (type: String) to a neighbouring node
    routes: dict = {}
    #If this node has been determined to be the source node in this network, send a message to the specified destination
    if(sys.argv[2] == "source"):
        send_routed(sys.argv[1], "Verbum Domini manet in aeternum!",routes)