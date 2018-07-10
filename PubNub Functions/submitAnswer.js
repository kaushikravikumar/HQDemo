export default (request) => {
    const kvstore = require('kvstore');
    const xhr = require('xhr');

    var answer = request.message.nameValuePairs.answer;

    if(answer === "optionA")
    {
        kvstore.incrCounter("optionA", 1);
        console.log('A Incremented');
    }
    else if(answer === "optionB")
    {
        kvstore.incrCounter("optionB", 1);
        console.log('B Incremented');
    }
    else if(answer === "optionC")
    {
        kvstore.incrCounter("optionC", 1);
        console.log('C Incremented');
    }
    else if(answer === "optionD")
    {
        kvstore.incrCounter("optionD", 1);
        console.log('D Incremented');
    }
    return request.ok(); // Return a promise when you're done
}
