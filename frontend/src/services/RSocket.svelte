<script context="module" lang="ts">
    import {IdentitySerializer, JsonSerializer, RSocketClient,} from "rsocket-core";
    import RSocketWebSocketClient from "rsocket-websocket-client";
    import {writable} from "svelte/store";
    import {BaseDeviceCommandDTO, DeviceCommandDTO, DeviceDto, FlockMonitorCommand} from "./StreamDtos";
    import {Flowable} from 'rsocket-flowable';
    import * as rxjs from 'rxjs';
    import {Observable, Subscription} from 'rxjs';
    import * as rxjsop from "rxjs/operators";

    let backendUrl = process.env._HOST
    export const connectSocket = (route: String, commands: rxjs.Observable<FlockMonitorCommand<any>>) => {

        const store = writable(Array());

        const client = new RSocketClient({
            serializers: {
                data: JsonSerializer,
                metadata: IdentitySerializer,
            },
            setup: {
                keepAlive: 60000,
                lifetime: 180000,
                dataMimeType: "application/json",
                metadataMimeType: "message/x.rsocket.routing.v0",
            },
            transport: new RSocketWebSocketClient({
                url: backendUrl + route,
            }),
        });

        client.connect().subscribe({
            onComplete: (socket) => {
                socket
                    .requestChannel(setupChannel(route, commands))
                    // .requestStream({
                    //     data: {},
                    //     metadata: getMetadata(route),
                    // })
                    .subscribe({
                        onComplete: () => console.debug("RSocket requestStream completed"),
                        onError: (error) => {
                            console.debug("RSocket requestStream error", error);
                        },
                        onNext: (value) => {
                            console.debug("RSocket requestStream onNext:", value);
                            store.update((devices) => devices.concat(value.data as DeviceDto));
                        },
                        // Nothing happens until `request(n)` is called
                        onSubscribe: (subscription) => {
                            console.debug("RSocket requestStream received onSubscribed (from server)");
                            // TODO: backpressure and stuff!
                            subscription.request(7000);
                        },
                    });
            },
            onError: (error) => {
                console.warn("RSocket connection error", error);
            },
            onSubscribe: (cancel) => {
                console.debug(`RSocket connection subscription on '${route}'`);
            },
        });

        return store
    };

    const getMetadata = (route: string) => String.fromCharCode(route.length) + route

    const setupChannel = (route: String, commands: Observable<FlockMonitorCommand<any>>) => {
        console.log("Setting up subscription for commands from application")
        let subscription: Subscription | undefined = undefined

        return new Flowable<DeviceCommandDTO<any>>((subscriber) => {
            const processCommand = (command: FlockMonitorCommand<any>) => {
                console.log("Process command")

                let deviceCommandDto = new BaseDeviceCommandDTO(command, getMetadata(route));
                subscriber.onNext(deviceCommandDto)
                return deviceCommandDto
            };

            const processError = err => {
                console.log("Processing error")
                subscriber.onError(err);
                throw err;

            };

            const something = commands
                .pipe(
                    rxjsop.map(command => processCommand(command)),
                    rxjsop.catchError((err,_) => processError(err))
                );


            subscriber.onSubscribe({
                cancel(): void {
                    console.log("Cancel is called")
                    // Our subscriber send us a cancel signal
                    // 1. ensure request: n are stopped.
                    // ?? (cancel subscription to `commands`)
                    // console.warn("Not unsubscribing from subscription to backend")
                    console.log("Unsubscribing from application observable..")
                    subscription.unsubscribe()

                    // 2. Send completion signal (cause convention?)
                    // subscriber.onComplete()
                },
                request: n => {
                    console.log(`Request '${n}' is called`)

                    // TODO:  Deal with request `n` (backpressure) properly
                    // for(let i = 0; i < n; i++) {
                    // }

                    if (!subscription) {
                        subscription = something
                            .subscribe(
                                // Added these logs for debug purposes
                                command => console.log(`Received another command:`, command),
                                error => console.error(`Error occurred`, error),
                                () => console.log("Subscription is finished")
                            )
                    }
                }

            })

        });
    };
</script>
