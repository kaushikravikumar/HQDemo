var pubnub;

const get_answers_url = 'INSERT_GET_ANSWERS_PFUNC_URL';

const subscribe_key = "INSERT_SUBSCRIBE_KEY";

const publish_key = "INSERT_PUBLISH_KEY";

const secret_key = "INSERT_SECRET_KEY";

var jsonReqOptions = {
    "body": {
        "which": "A"
    }
};

/*
*  This function is called when the Admin user presses the submit button.
*/
function publishQuestionAnswer() {
    pubnub = new PubNub({
        subscribeKey: subscribe_key,
        publishKey: publish_key,
        secretKey: secret_key,
        ssl: true
    });

    pubnub.publish({
            message: {
                "question": document.getElementById('question').value,
                "optionA": document.getElementById('optionA').value,
                "optionB": document.getElementById('optionB').value,
                "optionC": document.getElementById('optionC').value,
                "optionD": document.getElementById('optionD').value
            },
            channel: 'question_post',
            sendByPost: false, // true to send via post
            storeInHistory: false //override default storage options
        },
        function(status, response) {
            if (status.error) {
                // handle error
                console.log(status);
            } else {
                console.log("message Published w/ timetoken", response.timetoken);
            }
        });

    // Waits for 12 seconds then publishes correct answer and answer results.
    // Want to consider up to 2 second latency between answers being sent in and results being published.
    setTimeout(getResults, 12000);
}

/*
* This function gets the correct answer from the admin's entry. It then makes chained promisified XMLHttpRequests
* to the getAnswers PubNub function with the route specified as 'getcount' in order to obtain the count of how many people
* answered each option. Then publishes this data and resets the counters.
*/
function getResults() {
    var correctAnswer;
    if (document.getElementById('a_correct').checked) {
        correctAnswer = "optionA";
    } else if (document.getElementById('b_correct').checked) {
        correctAnswer = "optionB";
    } else if (document.getElementById('c_correct').checked) {
        correctAnswer = "optionC";
    } else if (document.getElementById('d_correct').checked) {
        correctAnswer = "optionD";
    }

    jsonReqOptions.body.which = "A";
    return request(get_answers_url + '?route=getcount', 'POST', jsonReqOptions).then((firstResponse) => {
        var countA = firstResponse.optionA;
        jsonReqOptions.body.which = "B";
        return request(get_answers_url + '?route=getcount', 'POST', jsonReqOptions).then((secondResponse) => {
            var countB = secondResponse.optionB;
            jsonReqOptions.body.which = "C";
            return request(get_answers_url + '?route=getcount', 'POST', jsonReqOptions).then((thirdResponse) => {
                var countC = thirdResponse.optionC;
                jsonReqOptions.body.which = "D";
                return request(get_answers_url + '?route=getcount', 'POST', jsonReqOptions).then((fourthResponse) => {
                    var countD = fourthResponse.optionD;
                    publishAnswerResults(countA, countB, countC, countD, correctAnswer);
                    // Waits a second then reset counters
                    setTimeout(resetCounters, 1000);
                })
            })
        })
    }).catch((error) => {
        console.log(error);
    });
}

/**
* This function is called by getResults() to publish the answer results onto the answer_post channel for users
* to see the correct answer and the stats of how many people answered which option.
* @param {Integer} countA number of users that answered optionA
* @param {Integer} countB number of users that answered optionB
* @param {Integer} countC number of users that answered optionC
* @param {Integer} countD number of users that answered optionD
* @param {String} correctAnswer either 'optionA', 'optionB', 'optionC', or 'optionD'
*/
function publishAnswerResults(countA, countB, countC, countD, correctAnswer) {
    pubnub.publish({
            message: {
                "optionA": countA,
                "optionB": countB,
                "optionC": countC,
                "optionD": countD,
                "correct": correctAnswer
            },
            channel: 'answer_post',
            sendByPost: false, // true to send via post
            storeInHistory: false //override default storage options
        },
        function(status, response) {
            if (status.error) {
                // handle error
                console.log(status);
            } else {
                console.log("message Published w/ timetoken", response.timetoken);
            }
        });
}

/*
* This function is called by getResults() to reset all our counters in the KV Store.
* This is done by calling our getAnswers PubNub function, which is modeled as a Rest API.
* We will specify the route as 'reset', so the appropriate function is executed.
*/
function resetCounters() {
    jsonReqOptions.body.which = "A";
    return request(get_answers_url + '?route=reset', 'POST', jsonReqOptions).then((firstResponse) => {
      jsonReqOptions.body.which = "B";
        return request(get_answers_url + '?route=reset', 'POST', jsonReqOptions).then((secondResponse) => {
          jsonReqOptions.body.which = "C";
            return request(get_answers_url + '?route=reset', 'POST', jsonReqOptions).then((thirdResponse) => {
              jsonReqOptions.body.which = "D";
                return request(get_answers_url + '?route=reset', 'POST', jsonReqOptions).then((fourthResponse) => {
                    console.log('Reset all counters!');
                })
            })
        })
    }).catch((error) => {
        console.log(error);
    });
}

/**
 * Helper function to make an HTTP request wrapped in an ES6 Promise.
 *
 * @param {String} url URL of the resource that is being requested.
 * @param {String} method POST, GET, PUT, etc.
 * @param {Object} options JSON Object with HTTP request options, "header"
 *     Object of possible headers to set, and a body Object of a request body.
 *
 * @return {Promise} Resolves a parsed JSON Object or String response text if
 *     the response code is in the 200 range. Rejects with response status text
 *     when the response code is outside of the 200 range.
 */
function request(url, method, options) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        let contentTypeIsSet = false;
        options = options || {};
        xhr.open(method, url);
        for (let header in options.headers) {
            if ({}.hasOwnProperty.call(options.headers, header)) {
                header = header.toLowerCase();
                contentTypeIsSet = header === 'content-type' ? true : contentTypeIsSet;
                xhr.setRequestHeader(header, options.headers[header]);
            }
        }
        if (!contentTypeIsSet) {
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        }
        xhr.onload = function() {
            if (xhr.status >= 200 && xhr.status < 300) {
                let response;
                try {
                    response = JSON.parse(xhr.response);
                } catch (e) {
                    response = xhr.response;
                }
                resolve(response);
            } else {
                reject({
                    status: xhr.status,
                    statusText: xhr.statusText,
                });
            }
        };
        xhr.send(JSON.stringify(options.body));
    });
}
