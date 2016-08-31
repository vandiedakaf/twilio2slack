#!/usr/bin/env bash

STACK_NAME=twilio2slack

echo
echo Searching for stack
STACK_SEARCH=$(aws cloudformation list-stacks --stack-status-filter CREATE_IN_PROGRESS CREATE_COMPLETE DELETE_IN_PROGRESS ROLLBACK_COMPLETE| jq -r ".StackSummaries[] | select(.StackName==\"${STACK_NAME}\")")

#true if empty
if [ -z "$STACK_SEARCH" ]
then
    echo Stack does not exist.
else
    echo Deleting stack...
    CREATE_STACK=$(aws cloudformation delete-stack --stack-name ${STACK_NAME})
    echo Delete request submitted.
fi
