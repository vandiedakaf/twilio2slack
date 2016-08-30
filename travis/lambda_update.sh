#!/usr/bin/env bash

FUNCTION_NAME="twilio2slack"
# S3_BUCKET is set as an environment variable
S3_KEY="$TRAVIS_REPO_SLUG/$TRAVIS_COMMIT/twilio2slack-1.0-SNAPSHOT.zip"

aws lambda update-function-code --function-name ${FUNCTION_NAME} --s3-bucket ${S3_BUCKET} --s3-key ${S3_KEY} --output json
if [ $? -ne 0 ]
then
    echo "Failed to update lambda"
    exit 1
fi
echo "Successfully updated function"

echo "Invoking $FUNCTION_NAME"
aws lambda invoke --function-name ${FUNCTION_NAME} --payload file://travis/lambda_payload.json --log-type Tail output.json
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
