from sender import send_message
from utils import generate_message, MessageTypes


def discover_route(destination: str, routing_table:dict):
    discovery_message = generate_message(destination, MessageTypes.ROUTE_REQUEST, f"Route to {destination}")
    

def send_routed(destination: str, message: str):


if __name__ == "__main__":
    routes: dict = {}
