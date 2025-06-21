declare const reducer: import("redux").Reducer<{
    getConnections: any;
    getConnectionsError: any;
    connectionName: any;
    permissions: any;
    getDetails: any;
    getDetailsError: any;
}, any, Partial<{
    getConnections: null | undefined;
    getConnectionsError: null | undefined;
    connectionName: null | undefined;
    permissions: {
        config: boolean;
    } | undefined;
    getDetails: null | undefined;
    getDetailsError: null | undefined;
}>>;
export default reducer;
export declare function getConnectionsStatus(startNextPoll: () => boolean): (dispatch: any) => void;
