#!/usr/bin/env bash

FUNC="twilio2slack"
POLICY="lambda-basic"
ROLE="dev-lambda-role"
S3_KEY="lambda/twilio2slack-1.0-SNAPSHOT.zip"

# assume that the policy also exists if the role exists
aws iam get-role --role-name ${ROLE}
if [ $? -ne 0 ]
then
    echo "Creating Roles and Policies"
    ROLE_ARN=$(aws iam create-role --role-name ${ROLE} --assume-role-policy-document file://travis/trust_policy.json | jq -r .Role.Arn)
    echo "Role ARN: ${ROLE_ARN}"
    POLICY_ARN=$(aws iam create-policy --policy-document file://travis/policy_lambda_basic.json --output json --policy-name ${POLICY} | jq -r .Policy.Arn)
    echo "Policy ARN: ${POLICY_ARN}"
    aws iam attach-role-policy --role-name ${ROLE} --policy-arn ${POLICY_ARN}
    # sleep so that roles and policies can propagate through system otherwise the lambda won't be able to use it
    sleep 10s
else
    ROLE_ARN=$(aws iam get-role --role-name ${ROLE} | jq -r .Role.Arn)
    echo "Role ARN: ${ROLE_ARN}"
    POLICY_ARN=$(aws iam list-policies --scope Local | jq -r ".Policies[] | select(.PolicyName==\"${POLICY}\").Arn")
    echo "Policy ARN: ${POLICY_ARN}"
fi

aws lambda get-function --function-name ${FUNC}
if [ $? -ne 0 ]
then
    echo "Creating lambda ${FUNC}"
    aws lambda create-function --function-name ${FUNC} --runtime java8 --role ${ROLE_ARN} --handler ForwardSms::processSms --code S3Bucket=${S3_BUCKET},S3Key=${S3_KEY} --output json
    if [ $? -ne 0 ]
    then
        echo "Failed to create lambda"
        exit 1
    fi
    echo "Successfully created lambda"
else
    echo "Updating lambda ${FUNC}"
    aws lambda update-function-code --function-name ${FUNC} --s3-bucket ${S3_BUCKET} --s3-key ${S3_KEY} --output json
    if [ $? -ne 0 ]
    then
        echo "Failed to update lambda"
        exit 1
    fi
    echo "Successfully updated function"
fi

echo "Invoking $FUNC"
aws lambda invoke --function-name ${FUNC} --payload file://travis/lambda_payload.json --log-type Tail output.json
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
