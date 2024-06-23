import socket
import sys
from uuid import uuid4
from utils import MessageTypes, generate_message, event_logger

IP = '127.0.0.1'
PORT = 5006

message = sys.argv[1]
destination = sys.argv[2]
msg_type = MessageTypes.SEND_REQUEST
if(sys.argv[3]):
    msg_type = MessageTypes.FLOOD_REQUEST

packet = generate_message(destination, msg_type, message, str(uuid4()))
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

try:
    sock.sendto(packet, (IP, PORT))
    event_logger(f"Sent the send request: {packet} to {IP}:{PORT}.")
except Exception as e:
    event_logger(f"Error with sending the send request: {e}")
finally:
    sock.close()
