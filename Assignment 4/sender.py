import socket

bcast_msg = b"Hello, world, prepare for KONSEQUENZEN!"
team_number = 6

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

broadcast_address = "192.168.210.255"
broadcast_port = 5000 + team_number

sock.sendto(bcast_msg, (broadcast_address, broadcast_port))

print(f"Sent message: {bcast_msg} to {broadcast_address}:{broadcast_port}")
sock.close()