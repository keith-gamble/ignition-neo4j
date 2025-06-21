import React, { Component } from 'react';
import { connect } from 'react-redux';
import { pollWaitAck } from 'ignition-lib';
import { getConnectionsStatus } from '../model/model';
import { BlankState, Gauge, ItemTable, Loading, UserFriendlyError, StatusLabel } from 'ignition-react';

const BLANK_STATE = {
    image: <img src="/main/res/alarm-notification/img/blank_alarms.png" alt="" />,
    heading: 'There are no connections defined.',
    body: 'Neo4J connections allow access to Neo4J Graph databases through Ignition.',
    links: [
        <a key="add-connection" className="primary button" target="_blank" href="/main/web/config/neo4j.neo4j">
            Add Connection
        </a>
    ]
};

interface ConnectionStatusLabelProps {
    connection: any;
}

function ConnectionStatusLabel({ connection }: ConnectionStatusLabelProps) {
    if (connection.exception) {
        return <UserFriendlyError link={connection.status} exception={connection.exception} />;
    }

    return (
        <StatusLabel status={connection.status}>
            <span>{connection.status}</span>
        </StatusLabel>
    );
}

interface ConnectOverviewProps {
    connections: any;
    connectionsError: any;
    dispatch: any;
}

class ConnectOverview extends Component<ConnectOverviewProps> {
    private cancelPoll?: () => void;

    constructor(props: ConnectOverviewProps) {
        super(props);
    }

    componentWillMount() {
        const { dispatch } = this.props;
        // refresh the connection status every 5 seconds, but don't start a new request until the last one has returned
        this.cancelPoll = pollWaitAck(dispatch, getConnectionsStatus, 5000);
    }

    componentWillUnmount() {
        if (this.cancelPoll) {
            this.cancelPoll();
        }
    }

    render() {
        const { connections, connectionsError } = this.props;
        console.log("connections", connections);

        // if we haven't received the connections yet, show a loading indicator
        if (connections === null) {
            console.log("connections is null");
            return (<div><Loading /></div>);
        }

        const connectionCount = connections.count;
        // if there are no connections, show a blank state
        if (connectionCount === 0) {
            console.log("connectionCount is 0");
            return (<div><BlankState {...BLANK_STATE} /></div>);
        }

        const HEADERS = [
            { header: 'Connection Name', weight: 1 },
            { header: 'Connection Type', weight: 1 },
            { header: 'Connections', weight: 1 },
            { header: 'Connection Status', weight: 1 },
        ];

        const connectionList = connections.connections;

        let items: any[] = [];
        if (connectionList != null) {
            items = connectionList.map(function (connection: any) {
                return [
                    connection.name,
                    connection.type,
                    connection.activeConnections + "/" + connection.maxConnectionPoolSize,
                    <ConnectionStatusLabel key={connection.name} connection={connection} />
                ];
            });
        }

        const activeConnections = connectionList.filter(function (connection: any) {
            return connection.status === "Valid";
        }).length;

        const validConnections = activeConnections + "/" + connectionCount;

        return (<div>
            <div className="row">
                <div className="small-12 columns">
                    <div className="page-heading">
                        <div className="quick-links">
                            <a href="/main/web/config/neo4j.neo4j">Configure</a>
                        </div>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="small-12 medium-5 large-3 columns">
                    <Gauge label="Valid Connections" value={validConnections} />
                </div>
            </div>
            <div className="row">
                <div className="small-12 columns">
                    <ItemTable headers={HEADERS} items={items} errorMessage={connectionsError} />
                </div>
            </div>
        </div>);
    }
}

function selector(state: any) {
    return {
        connections: state.getConnections,
        connectionsError: state.getConnectionsError,
    }
}

export default connect(selector)(ConnectOverview);