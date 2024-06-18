#!/bin/bash

# Datei, die gesendet werden soll
FILE_TO_SEND="/home/ingo/git/MobileComputingAssignments/Assignment 4/receiver.py"

# IP-Adressen oder Hostnamen der Raspberry Pis
HOSTS=("129.69.210.60" "129.69.210.61" "129.69.210.62" "129.69.210.63" "129.69.210.64" "129.69.210.65" "129.69.210.66")

# Benutzername für die Raspberry Pis
USERNAME="team6"

# Passwort für die Raspberry Pis
PASSWORD="ohn2IDef"

# Zielverzeichnis auf den Raspberry Pis
DEST_DIR="/home/team6"

# Schleife über die Hosts und sende die Datei
for HOST in "${HOSTS[@]}"; do
    echo "Sende $FILE_TO_SEND an $HOST..."
    sshpass -p "$PASSWORD" scp "$FILE_TO_SEND" "$USERNAME@$HOST:$DEST_DIR"
    if [ $? -eq 0 ]; then
        echo "Datei erfolgreich an $HOST gesendet."
    else
        echo "Fehler beim Senden der Datei an $HOST."
    fi
done

echo "Fertig!"
