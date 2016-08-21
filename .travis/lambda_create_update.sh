#!/usr/bin/env bash

FUNC="blambda"
S3_BUCKET="vdda-sandbox"
S3_KEY="lambda/twilio2slack-1.0-SNAPSHOT.zip"

echo "Creating $FUNC"
aws lambda create-function --function-name $FUNC --runtime java8 --role arn:aws:iam::522161570381:role/dev-lambda-LambdaExecutionRole-17S53XUZ6GVT --handler ForwardSms::processSms --code S3Bucket=$S3_BUCKET,S3Key=$S3_KEY --output json

echo "Updating $FUNC"
echo "aws lambda update-function-code --function-name $FUNC --s3-bucket $S3_BUCKET --s3-key $S3_KEY --output json"
aws lambda update-function-code --function-name $FUNC --s3-bucket $S3_BUCKET --s3-key $S3_KEY --output json

echo "Invoking $FUNC"
echo "aws lambda invoke --function-name $FUNC --payload "Hello World" --log-type Tail --output json"
aws lambda invoke --function-name $FUNC --payload "Hello World" --log-type Tail --output json
