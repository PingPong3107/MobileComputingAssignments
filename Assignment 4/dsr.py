import socket
import sys
from uuid import uuid4
from utils import MessageTypes, generate_message

# IP-Adresse und Port konfigurieren
IP = '127.0.0.1'  # Hier sollte Ihre eigene IP-Adresse stehen
PORT = 5006

# Nachricht, die gesendet werden soll
message = sys.argv[1]
destination=sys.argv[2]

packet = generate_message(destination,MessageTypes.SEND_REQUEST,message,str(uuid4()))
# Socket erstellen
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

try:
    # Nachricht senden
    sock.sendto(packet, (IP, PORT))
    print(f"Nachricht '{packet}' erfolgreich an {IP}:{PORT} gesendet.")
except Exception as e:
    print(f"Fehler beim Senden der Nachricht: {e}")
finally:
    # Socket schließen
    sock.close()




