#!/bin/bash
if [ $# -le 2 ]
then
        echo "illegal number of parameters, specify below 3 arguments"
        echo "1: namespace: env"
        echo "2: Service name: "
        echo "3: command to be executed"
        exit 1
fi

# Assign the arguments to variables
namespace=${@:1:1}
svcName=${@:2:1}
grepCmd=${@:3}

# Get the pod name
podName=$(kubectl get pods -n $namespace -l app=$svcName -o jsonpath="{.items[0].metadata.name}")

# Fetch and filter the logs
kubectl -n $namespace exec -it $podName -- bash -c "$grepCmd"