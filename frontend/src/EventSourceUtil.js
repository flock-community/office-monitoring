
export const createEventSource = (url, onMessage, onError = (e)=> {}) => {
    const eventSource = new EventSource(url);
    eventSource.onmessage = onMessage;

    eventSource.onerror = function (event) {
        console.log("Received error");
        console.error(event);
        eventSource.close();
        onError(event)
    };

    return eventSource
};
