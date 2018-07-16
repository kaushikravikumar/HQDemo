export default (request) => {
    const kvstore = require('kvstore');
    const xhr = require('xhr');

    var answer = request.message.nameValuePairs.answer;

    if (answer !== "optionA" && answer !== "optionB" && answer !== "optionC" &&
        answer !== "optionD") {
        response.status = 400;
        return response.send();
    } else {
        kvstore.incrCounter(answer, 1);
    }
    return request.ok(); // Return a promise when you're done
}
