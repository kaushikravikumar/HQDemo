export default (request, response) => {
    const pubnub = require('pubnub');
    const kvstore = require('kvstore');

    response.headers['Access-Control-Allow-Origin'] = '*';
    response.headers['Access-Control-Allow-Headers'] = 'Origin, X-Requested-With, Content-Type, Accept';
    response.headers['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS, PUT, DELETE';

    let controllers = {
        getcount: {},
        reset: {}
    };

    // Gets count of each answer option.
    controllers.getcount.post = () => {
        var whichOption = JSON.parse(request.body).which;

        // send response with counts for a
        if (whichOption === "a") {
            return kvstore.getCounter("optionA").then((countA) => {
                console.log('countA', countA);
                var jsonRes = {
                    "optionA": countA
                };
                return response.send(jsonRes);
            });
        }
        // send response with counts for b
        else if (whichOption === "b") {
            return kvstore.getCounter("optionB").then((countB) => {
                console.log('countB', countB);
                var jsonRes = {
                    "optionB": countB
                };
                return response.send(jsonRes);
            });
        }
        // send response with counts for c
        else if (whichOption === "c") {
            return kvstore.getCounter("optionC").then((countC) => {
                console.log('countC', countC);
                var jsonRes = {
                    "optionC": countC
                };
                return response.send(jsonRes);
            });
        }

        // send response with counts for d
        else if (whichOption === "d") {
            return kvstore.getCounter("optionD").then((countD) => {
                console.log('countD', countD);
                var jsonRes = {
                    "optionD": countD
                };
                return response.send(jsonRes);
            });
        }
    };

    // resets the answer counts
    controllers.reset.post = () => {
        var whichOption = JSON.parse(request.body).which;

        // If call was made with parameter which as a
        if (whichOption === "a") {
            return kvstore.getCounter("optionA").then((countA) => {
                if (countA !== 0) {
                    console.log('Reset A');
                    return kvstore.incrCounter("optionA", -1 * countA).then((newCountA) => {
                        return response.send();
                    });
                } else {
                    return response.send();
                }
            }).catch((err) => {
                console.log(err);
            });
        }
        // If call was made with parameter which as b
        else if (whichOption === "b") {
            return kvstore.getCounter("optionB").then((countB) => {
                if (countB !== 0) {
                    console.log('Reset B');
                    return kvstore.incrCounter("optionB", -1 * countB).then((newCountB) => {
                        return response.send();
                    });
                } else {
                    return response.send();
                }
            }).catch((err) => {
                console.log(err);
            });
        }
        // If call was made with parameter which as c
        else if (whichOption === "c") {
            return kvstore.getCounter("optionC").then((countC) => {
                if (countC !== 0) {
                    console.log('Reset C');
                    return kvstore.incrCounter("optionC", -1 * countC).then((newCountC) => {
                        return response.send();
                    });
                } else {
                    return response.send();
                }
            }).catch((err) => {
                console.log(err);
            });
        }
        // If call was made with parameter which as d
        else if (whichOption === "d") {
            return kvstore.getCounter("optionD").then((countD) => {
                if (countD !== 0) {
                    console.log('Reset D');
                    return kvstore.incrCounter("optionD", -1 * countD).then((newCountD) => {
                        return response.send();
                    });
                } else {
                    return response.send();
                }
            }).catch((err) => {
                console.log(err);
            });
        }
    };

    const route = request.params.route;
    const method = request.method.toLowerCase();

    if (
        method &&
        route &&
        controllers[route] &&
        controllers[route][method]
    ) {
        return controllers[route][method]();
    } else {
        response.status = 404;
        return response.send();
    }
};
