const standardHeaders = {
    Accept: "application/json",
    "Content-Type": "application/json"
}

const toJson = res => res.json();

const GET = (path, headers) => fetch(path, {method: 'GET', headers: {...standardHeaders, ...headers}});

export const getHelloWorld = () => GET('/api').then(toJson)
