export default (request, response) => {
    const pubnub = require('pubnub');
    const kvstore = require('kvstore');

    let headersObject = request.headers;
    let paramsObject = request.params;
    let methodString = request.method;
    let bodyString = request.body;

    response.headers['Access-Control-Allow-Origin'] = '*';
    response.headers['Access-Control-Allow-Headers'] = 'Origin, X-Requested-With, Content-Type, Accept';
    response.headers['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS, PUT, DELETE';
    response.headers['Content-Type'] = 'application/json';

    var uuid = JSON.parse(request.body).uuid;

    return pubnub.grant({
        channels: ['question_post', 'answer_post', 'presence'],
        read: true, // false to disallow
        write: false, // false to disallow,
        authKeys: [uuid],
        ttl: 0
    }).then(() => {
        return pubnub.grant({
            channels: ['submitAnswer'],
            read: false, // false to disallow
            write: true, // false to disallow,
            authKeys: [uuid],
            ttl: 0
        }).then(() => {
            response.status = 200;
            return response.send({});
        });
    }).catch((error) => {
        console.log(error);
        response.status = 400;
        return response.send();
    });
};
