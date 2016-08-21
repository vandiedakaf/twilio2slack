#!/usr/bin/env bash

FUNC="twilio2slack"
POLICY="lambda-basic"
ROLE="dev-lambda-role"
S3_KEY="lambda/twilio2slack-1.0-SNAPSHOT.zip"


aws iam get-role --role-name ${ROLE}
if [ $? -ne 0 ]
then
    echo "Creating Roles and Policies"
    ROLE_ARN=$(aws iam create-role --role-name ${ROLE} --assume-role-policy-document file://travis/trust_policy.json | jq -r .Role.Arn)
    POLICY_ARN=$(aws iam create-policy --policy-document file://travis/policy_lambda_basic.json --output json --policy-name ${POLICY} | jq -r .Policy.Arn)
    aws iam attach-role-policy --role-name ${ROLE} --policy-arn ${POLICY_ARN}
fi

echo "Creating $FUNC"
aws lambda create-function --function-name ${FUNC} --runtime java8 --role ${ROLE_ARN} --handler ForwardSms::processSms --code S3Bucket=${S3_BUCKET},S3Key=${S3_KEY} --output json
if [ $? -eq 0 ]
then
  echo "Successfully created function"
else
  echo "Updating $FUNC"
  aws lambda update-function-code --function-name ${FUNC} --s3-bucket ${S3_BUCKET} --s3-key ${S3_KEY} --output json

    if [ $? -eq 0 ]
    then
        echo "Successfully updated function"
    else
        echo "Failed to update lambda"
        exit 1
    fi
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
