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

    // Gets count of answer option.
    controllers.getcount.post = () => {
        var whichOption = JSON.parse(request.body).which;
        var option_string = `option${ whichOption }`;

        // Validates user input on the backend
        if (whichOption !== 'A' &&
            whichOption !== 'B' && whichOption !== 'C' &&
            whichOption !== 'D') {
            response.status = 400;
            return response.send();
        } else {
            return kvstore.getCounter(option_string).then((count) => {
                var jsonRes = {
                    [option_string]: count
                };
                return response.send(jsonRes);
            });
        }
    };

    // resets the answer count of answer option.
    controllers.reset.post = () => {
        var whichOption = JSON.parse(request.body).which;
        var option_string = `option${ whichOption }`;

        // Validates user input on backend  
        if (whichOption !== 'A' &&
            whichOption !== 'B' && whichOption !== 'C' &&
            whichOption !== 'D') {
            response.status = 400;
            return response.send();
        } else {
            return kvstore.getCounter(option_string).then((count) => {
                if (count !== 0) {
                    console.log('Reset ' + whichOption);
                    return kvstore.incrCounter(option_string, -1 * count).then((newCount) => {
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