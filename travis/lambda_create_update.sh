#!/usr/bin/env bash

LAMBDA="twilio2slack"
HANDLER="ForwardSms::processSms"
POLICY_LOG="lambda-logs"
POLICY_S3="lambda-s3"
ROLE="dev-lambda-role"
S3_KEY="lambda/twilio2slack-1.0-SNAPSHOT.zip"

# assume that the policies also exists if the role exists
aws iam get-role --role-name ${ROLE}
if [ $? -ne 0 ]
then
    echo "Creating Roles and Policies"
    ROLE_ARN=$(aws iam create-role --role-name ${ROLE} --assume-role-policy-document file://travis/trust_policy.json | jq -r .Role.Arn)
    echo "Role ARN: ${ROLE_ARN}"
    POLICY_LOG_ARN=$(aws iam create-policy --policy-document file://travis/policy_logs.json --output json --policy-name ${POLICY_LOG} | jq -r .Policy.Arn)
    echo "Policy Log ARN: ${POLICY_LOG_ARN}"
    POLICY_S3_ARN=$(aws iam create-policy --policy-document file://travis/policy_s3.json --output json --policy-name ${POLICY_S3} | jq -r .Policy.Arn)
    echo "Policy Log ARN: ${POLICY_S3_ARN}"
    aws iam attach-role-policy --role-name ${ROLE} --policy-arn ${POLICY_LOG_ARN}
    aws iam attach-role-policy --role-name ${ROLE} --policy-arn ${POLICY_S3_ARN}
    # sleep so that roles and policies can propagate through system otherwise the lambda won't be able to use it
    sleep 10s
else
    ROLE_ARN=$(aws iam get-role --role-name ${ROLE} | jq -r .Role.Arn)
    echo "Role ARN: ${ROLE_ARN}"
    POLICY_LOG_ARN=$(aws iam list-policies --scope Local | jq -r ".Policies[] | select(.PolicyName==\"${POLICY_LOG}\").Arn")
    echo "Policy Log ARN: ${POLICY_LOG_ARN}"
    POLICY_S3_ARN=$(aws iam list-policies --scope Local | jq -r ".Policies[] | select(.PolicyName==\"${POLICY_S3}\").Arn")
    echo "Policy S3 ARN: ${POLICY_S3_ARN}"
fi

aws lambda get-function --function-name ${LAMBDA}
if [ $? -ne 0 ]
then
    echo "Creating lambda ${LAMBDA}"
    aws lambda create-function --function-name ${LAMBDA} --runtime java8 --role ${ROLE_ARN} --handler ${HANDLER} --code S3Bucket=${S3_BUCKET},S3Key=${S3_KEY} --timeout 4 --memory-size 512 --output json
    if [ $? -ne 0 ]
    then
        echo "Failed to create lambda"
        exit 1
    fi
    echo "Successfully created lambda"
else
    echo "Updating lambda ${LAMBDA}"
    aws lambda update-function-code --function-name ${LAMBDA} --s3-bucket ${S3_BUCKET} --s3-key ${S3_KEY} --output json
    if [ $? -ne 0 ]
    then
        echo "Failed to update lambda"
        exit 1
    fi
    echo "Successfully updated function"
fi

echo "Invoking $LAMBDA"
aws lambda invoke --function-name ${LAMBDA} --payload file://travis/lambda_payload.json --log-type Tail output.json
if [ $? -ne 0 ]
then
    echo "Failed to invoke lambda"
    exit 1
fi
if grep -q "errorMessage" output.json
then
    echo "Lambda invoke error: "
    cat output.json
    exit 1;
fi

echo "Successfully created or updated lambda"
exit 0
