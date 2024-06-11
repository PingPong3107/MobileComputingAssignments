import json
import socket
import struct
import sys
import uuid
import argparse



def send_message(message_text, header):

    header_json = json.dumps(header).encode('utf-8')
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    team_number = 6
    udp_packet = header_json + b'\n' + message_text
    broadcast_address = "192.168.210.255"
    broadcast_port = 5000 + team_number

    for i in range(10):
        sock.sendto(udp_packet, (broadcast_address, broadcast_port))

    print(f"Sent message: {udp_packet} to {broadcast_address}:{broadcast_port}")
    sock.close()

def resend_message(udpPacket):

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    team_number = 6

    broadcast_address = "192.168.210.255"
    broadcast_port = 5000 + team_number

    for i in range(10):
        sock.sendto(udpPacket, (broadcast_address, broadcast_port))

    print(f"Resent message: {udpPacket} to {broadcast_address}:{broadcast_port}")
    sock.close()




if __name__ == "__main__":
    message_id = uuid.uuid4()
    header = {
        'uuid': str(uuid.uuid4()),  # Beispiel UUID
        'ttl': 10,  # Beispiel TTL
        'source_ip': sys.argv[0],  # Beispiel Quell-IP
        'destination_ip': sys.argv[1]  # Beispiel Ziel-IP
    }    
    messagePayload="Hello, world, prepare for KONSEQUENZEN!"
    send_message(messagePayload,header)