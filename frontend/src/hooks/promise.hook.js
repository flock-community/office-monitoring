import { useEffect, useState } from "react";

export function usePromiseState(
    getPromise,
    initialState
) {
    const [data, setData] = useState(initialState);
    const loading = usePromiseEffect(getPromise, setData);
    return [data, loading];
}

function usePromiseEffect(fetch, setData) {
    const [loading, setLoading] = useState(true);
    useEffect(() => {
        let isSubscribed = true;
        const load = (data) => {
            if (isSubscribed) {
                setData(data);
                setLoading(false);
            }
        };
        fetch().then(load);
        return () => {
            isSubscribed = false; // mitigates failing to load data in unmounted component
        };
    }, []); // eslint-disable-line react-hooks/exhaustive-deps
    return loading;
}
