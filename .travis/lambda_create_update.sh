#!/usr/bin/env bash

FUNC="twilio2slack"
S3_KEY="lambda/twilio2slack-1.0-SNAPSHOT.zip"

echo "Creating $FUNC"
aws lambda create-function --function-name $FUNC --runtime java8 --role arn:aws:iam::522161570381:role/dev-lambda-LambdaExecutionRole-17S53XUZ6GVT --handler ForwardSms::processSms --code S3Bucket=$S3_BUCKET,S3Key=$S3_KEY --output json
if [ $? -eq 0 ]
then
  echo "Successfully created function"
else
  echo "Updating $FUNC"
  aws lambda update-function-code --function-name $FUNC --s3-bucket $S3_BUCKET --s3-key $S3_KEY --output json

    if [ $? -eq 0 ]
    then
        echo "Successfully updated function"
    else
        echo "Failed to update lambda"
        exit 1
    fi
fi

echo "Invoking $FUNC"
aws lambda invoke --function-name $FUNC --payload file://.travis/lambda_payload.json --log-type Tail --output json output.json
if [ $? -ne 0 ]
then
    echo "Failed to invoke lambda"
    exit 1
fi

echo "Successfully created or updated lambda"
exit 0
