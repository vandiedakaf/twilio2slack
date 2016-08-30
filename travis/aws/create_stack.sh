#!/usr/bin/env bash

FUNCTION_NAME="twilio2slack"
HANDLER=ForwardSms::handleRequest
ROLE=dev-lambda-role
# S3_BUCKET is set as an environment variable
S3_KEY=twilio2slack-1.0-SNAPSHOT.zip # TODO chicken & egg problem here...
STACK_NAME=twilio2slack

echo
echo Searching for stack
STACK_SEARCH=$(aws cloudformation list-stacks --stack-status-filter CREATE_IN_PROGRESS CREATE_COMPLETE DELETE_IN_PROGRESS ROLLBACK_COMPLETE| jq -r ".StackSummaries[] | select(.StackName==\"${STACK_NAME}\")")

#true if empty
if [ -z "$STACK_SEARCH" ]
then
    echo Creating stack...
    CREATE_STACK=$(aws cloudformation create-stack --stack-name ${STACK_NAME} --capabilities CAPABILITY_NAMED_IAM --parameters ParameterKey=FunctionName,ParameterValue=${FUNCTION_NAME},UsePreviousValue=false ParameterKey=Handler,ParameterValue=${HANDLER},UsePreviousValue=false ParameterKey=RoleName,ParameterValue=${ROLE},UsePreviousValue=false ParameterKey=S3Bucket,ParameterValue=${S3_BUCKET},UsePreviousValue=false ParameterKey=S3Key,ParameterValue=${S3_KEY},UsePreviousValue=false --template-body file://lambda_gateway.template)
    echo Stack templated uploaded.
else
    echo Stack already exists.
fi
