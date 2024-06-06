#!/bin/bash

for i in "60" "61" "62" "64" "65" "66"
do
    mkdir "/home/dumudingirak/${i}team6"
    adress="192.168.210.$i:/home/team6"
    sshpass -p "ohn2IDef" sshfs $adress "/home/dumudingirak/${i}team6"
    echo "Mounter folder for Node ${i}"
done