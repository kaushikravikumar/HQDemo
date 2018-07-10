var pubnub;

function publishQuestionAnswer() {
    pubnub = new PubNub({
        subscribeKey: "sub-c-3436c79c-78a1-11e8-a43f-d6f8762e29f7",
        publishKey: "pub-c-cfe3c983-4203-40d2-8f53-7e27c1be5e7b",
        secretKey: "sec-c-NDllYmU5ZTItY2Y3OS00N2UzLThlNWUtYTc0ODM2ODhkZGM0",
        ssl: true
    });

//     pubnub.grant(
//     {
//         authKeys: ['userauthkey'],
//         channels: ['question_post', 'answer_post', 'presence'],
//         read: true,
//         write: false,
//         ttl: 14400
//     },
//     function (status) {
//         console.log(status);
//     }
// );
//     pubnub.grant(
//       {
//           authKeys: ['userauthkey'],
//           channels: ['submitAnswer'],
//           read: false,
//           write: true,
//           ttl: 14400
//       },
//       function(status){
//         console.log(status);
//       }
//     );

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

    // WAIT FOR 12 SECONDS AND THEN PUBLISH ANSWER!!
    // Want to consider up to 2 second latency between answers being sent in and results being published.
    setTimeout(getResults, 12000);
}

var url = 'https://pubsub.pubnub.com/v1/blocks/sub-key/sub-c-3436c79c-78a1-11e8-a43f-d6f8762e29f7/getanswers';

var firstjsonReq = {
    "which": "a"
};
var secondjsonReq = {
    "which": "b"
};
var thirdjsonReq = {
    "which": "c"
};
var fourthjsonReq = {
    "which": "d"
};

var jsonRequests = [firstjsonReq, secondjsonReq, thirdjsonReq, fourthjsonReq];

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

    var firstRequest = new XMLHttpRequest();
    var secondRequest = new XMLHttpRequest();
    var thirdRequest = new XMLHttpRequest();
    var fourthRequest = new XMLHttpRequest();

    firstRequest.open('POST', url + '?route=getcount');
    firstRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

    secondRequest.open('POST', url + '?route=getcount');
    secondRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

    thirdRequest.open('POST', url + '?route=getcount');
    thirdRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

    fourthRequest.open('POST', url + '?route=getcount');
    fourthRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');


    // var answerResults = {
    //     "optionA": 0,
    //     "optionB": 0,
    //     "optionC": 0,
    //     "optionD": 0
    // };
    //
    // var optionKeys = ["optionA", "optionB", "optionC", "optionD"];
    //
    //     var f = (function(){
    //     var xhr = [], i;
    //     for(i = 0; i < 4; i++){ //for loop
    //       (function(i){
    //             xhr[i] = new XMLHttpRequest();
    //             xhr[i].open("POST", url + '?route=getcount');
    //             xhr[i].setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    //             xhr[i].onreadystatechange = function(){
    //                 if (xhr[i].readyState == XMLHttpRequest.DONE){
    //                   answerResults[optionKeys[i]] = JSON.parse(xhr[i].responseText)[optionKeys[i]];
    //                 }
    //             };
    //             xhr[i].send(JSON.stringify(jsonRequests[i]));
    //             })(i);
    //     }
    //     })();

    // for (i = 0; i < 4; i++) {
    //   xmlRequest[i] = new XMLHttpRequest();
    //   xmlRequest.open('POST', url + '?route=getcount');
    //   xmlRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    //   xmlRequest.onreadystatechange = function() {
    //       if(xmlRequest.readyState == XMLHttpRequest.DONE)
    //       {
    //           console.log(JSON.parse(xmlRequest.responseText).optionKeys[i]);
    //           // answerResults.optionKeys[i] = JSON.parse(xmlRequest.responseText).optionKeys[i];
    //       }
    //   };
    //   xmlRequest.send(JSON.stringify(jsonRequests[i]));
    // }
    // console.log(answerResults);

    firstRequest.onreadystatechange = function() {
        if (firstRequest.readyState == XMLHttpRequest.DONE) {
            var countA = JSON.parse(firstRequest.responseText).optionA;

            secondRequest.onreadystatechange = function() {
                if (secondRequest.readyState == XMLHttpRequest.DONE) {
                    var countB = JSON.parse(secondRequest.responseText).optionB;

                    thirdRequest.onreadystatechange = function() {
                        if (thirdRequest.readyState == XMLHttpRequest.DONE) {
                            var countC = JSON.parse(thirdRequest.responseText).optionC;

                            fourthRequest.onreadystatechange = function() {
                                if (fourthRequest.readyState == XMLHttpRequest.DONE) {
                                    var countD = JSON.parse(fourthRequest.responseText).optionD;
                                    publishMessage(countA, countB, countC, countD, correctAnswer);
                                }
                            };
                            fourthRequest.send(JSON.stringify(fourthjsonReq));
                        }
                    };
                    thirdRequest.send(JSON.stringify(thirdjsonReq));
                }
            };
            secondRequest.send(JSON.stringify(secondjsonReq));
        }
    };
    firstRequest.send(JSON.stringify(firstjsonReq));
    
    // // Waits a second then reset counters
    setTimeout(resetCounters, 1000);
}

function publishMessage(countA, countB, countC, countD, correctAnswer) {
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

function resetCounters()
{
  var firstRequest = new XMLHttpRequest();
  var secondRequest = new XMLHttpRequest();
  var thirdRequest = new XMLHttpRequest();
  var fourthRequest = new XMLHttpRequest();

  firstRequest.open('POST', url + '?route=reset');
  firstRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

  secondRequest.open('POST', url + '?route=reset');
  secondRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

  thirdRequest.open('POST', url + '?route=reset');
  thirdRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

  fourthRequest.open('POST', url + '?route=reset');
  fourthRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

  firstRequest.onreadystatechange = function() {
      if (firstRequest.readyState == XMLHttpRequest.DONE) {

          secondRequest.onreadystatechange = function() {
              if (secondRequest.readyState == XMLHttpRequest.DONE) {

                thirdRequest.onreadystatechange = function() {
                    if (thirdRequest.readyState == XMLHttpRequest.DONE) {

                      fourthRequest.onreadystatechange = function() {
                          if (fourthRequest.readyState == XMLHttpRequest.DONE) {
                            // RESETS COUNTERS
                          }
                      };
                      fourthRequest.send(JSON.stringify(fourthjsonReq));
                    }
                };
                thirdRequest.send(JSON.stringify(thirdjsonReq));
              }
          };
          secondRequest.send(JSON.stringify(secondjsonReq));
      }
  };
  firstRequest.send(JSON.stringify(firstjsonReq));
}
