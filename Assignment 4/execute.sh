#!/bin/bash

# IP-Adressen oder Hostnamen der Raspberry Pis
HOSTS=("129.69.210.60" "129.69.210.61" "129.69.210.62" "129.69.210.63" "129.69.210.64" "129.69.210.65" "129.69.210.66")

# Benutzername für die Raspberry Pis
USERNAME="team6"

# Passwort für die Raspberry Pis
PASSWORD="ohn2IDef"

# Pfad zum Skript, das auf den Raspberry Pis ausgeführt werden soll
REMOTE_SCRIPT="/home/team6/test.py"


open_terminal() {
    HOST=$1
    konsole --hold -e bash -c "
        sshpass -p '$PASSWORD' ssh -t $USERNAME@$HOST <<'EOF'
        python3 $REMOTE_SCRIPT
        exec bash
EOF
    "
}

# Schleife über die Hosts und öffne eine Konsole für jede Verbindung parallel
for HOST in "${HOSTS[@]}"; do
    echo "Öffne Konsole für $HOST..."
    open_terminal $HOST &
done

echo "Alle Konsolen wurden geöffnet."

