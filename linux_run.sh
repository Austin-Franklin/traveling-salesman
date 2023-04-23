#!/bin/bash

no_java=false

#check if javac exists
if ! which java > /dev/null 2>&1; then
    echo "Java JRE not installed."
    no_java=true
fi

if ! which javac > /dev/null 2>&1; then
    echo "Java SDK not installed."
    no_java=true
fi

if [ $no_java = true ]; then
    exit 1
fi

#check if jar doesnt exists
if [ ! -f "target/traveling-salesman-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    #if not, try to install
    if [ "$(command -v mvn)" ]; then
        mvn install
    else
        echo "Maven not installed."
        exit 1
    fi
fi

#check if enough ram
total_ram=$(free -m | awk '/^Mem:/{print $2}')

if [ "$total_ram" -lt 2048 ]; then
    echo "Your sytem only has $total_ram MB of ram, 2048 MB are required."
    exit 1
fi
#im serious 9 nodes and it take 900MB of ram
#checkmate, electron enthusiasts

user_input=""

while true; do 
    read -p "Run 'normal' or 'demo': " user_input

    # convert user input and valid options to lowercase
    user_input=$(echo "$user_input" | tr '[:upper:]' '[:lower:]')
    valid_options=("normal" "demo")
    valid_options_lowercase=("${valid_options[@],,}")

    # check if valid input
    if [[ " ${valid_options_lowercase[*]} " == *" $user_input "* ]]; then
        break;
    else
        echo "Invalid input, enter only 'normal' or 'demo"
    fi
done

#run jar
if [ "$user_input" = "normal" ]; then
    java -jar -Xms256m -Xmx2048m target/traveling-salesman-1.0-SNAPSHOT-jar-with-dependencies.jar
else
    java -jar -Xms256m -Xmx2048m target/traveling-salesman-1.0-SNAPSHOT-jar-with-dependencies.jar < demo.txt
fi


