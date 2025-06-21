import React, { Component } from 'react';
declare global {
    const IgnitionLib: {
        pollWaitAck: (dispatch: any, actionCreator: any, interval: number) => () => void;
    };
    const IgnitionReact: {
        BlankState: React.ComponentType<any>;
        Gauge: React.ComponentType<any>;
        ItemTable: React.ComponentType<any>;
        Loading: React.ComponentType;
        UserFriendlyError: React.ComponentType<any>;
        StatusLabel: React.ComponentType<any>;
    };
}
interface Props {
    connections: any;
    connectionsError: any;
    dispatch: any;
}
declare class ConnectOverview extends Component<Props> {
    private cancelPoll?;
    constructor(props: Props);
    componentDidMount(): void;
    componentWillUnmount(): void;
    render(): import("react/jsx-runtime").JSX.Element;
}
declare const _default: import("react-redux").ConnectedComponent<typeof ConnectOverview, {
    ref?: React.LegacyRef<ConnectOverview> | undefined;
    key?: React.Key | null | undefined;
    context?: React.Context<import("react-redux").ReactReduxContextValue<any, import("redux").UnknownAction> | null> | undefined;
    store?: import("redux").Store | undefined;
}>;
export default _default;
