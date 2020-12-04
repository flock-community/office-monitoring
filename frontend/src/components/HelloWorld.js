import React from "react";
import {usePromiseState} from "../hooks/promise.hook";
import {getHelloWorld} from "../services/HelloWorldService";

export function HelloWorld() {

    const [helloWorld, loading] = usePromiseState(getHelloWorld, '')

    return loading
        ? (<div>Loading 🕸</div>)
        : (<div>Greeting: {helloWorld.greeting}</div>)
}
