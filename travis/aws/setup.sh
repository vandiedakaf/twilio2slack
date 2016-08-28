#!/usr/bin/env bash

STACK_NAME=twilio2slack

STACK_SEARCH=$(aws cloudformation list-stacks --stack-status-filter CREATE_IN_PROGRESS CREATE_COMPLETE DELETE_IN_PROGRESS ROLLBACK_COMPLETE| jq -r ".StackSummaries[] | select(.StackName==\"${STACK_NAME}\")")

#true if empty
if [ -z "$STACK_SEARCH" ]
then
    echo Creating stack...
    CREATE_STACK=$(aws cloudformation create-stack --stack-name ${STACK_NAME} --template-body file://api_gateway.template)
else
    echo Stack already exists.
fi
