import {combineReducers} from 'redux';
import {fetchJson, checkStatus, JSCache} from 'ignition-lib';

const CONNECTIONS_LOAD = 'neo/CONNECTIONS_LOAD';
const CONNECTIONS_ERR = 'neo/CONNECTIONS_ERR';
const CONNECTIONS_DETAIL_LOAD = 'neo/CONNECTIONS_DETAIL_LOAD';
const CONNECTIONS_DETAIL_ERR = 'neo/CONNECTIONS_DETAIL_ERR';

const VIEW_ALL = "neo/VIEW_ALL";
const VIEW_CONNECTION = "neo/VIEW_CONNECTION";
const FETCH_PERMISSIONS = "neo/FETCH_PERMISSIONS";

function getConnections(state = null, action) {
    if (action.type === CONNECTIONS_LOAD) {
        return action.connections;
    } else if (action.type === CONNECTIONS_ERR) {
        return null;
    }
    return state;
}

function getConnectionsError(state = null, action) {
    if (action.type === CONNECTIONS_ERR) {
        return action.reason;
    } else if (action.type === CONNECTIONS_LOAD) {
        return null;
    }
    return state;
}

function getDetails(state = null, action) {
    if (action.type === CONNECTIONS_DETAIL_LOAD) {
        return action.connection;
    } else if (action.type === CONNECTIONS_DETAIL_ERR) {
        return null;
    }
    return state;
}

function getDetailsError(state = null, action) {
    if (action.type === CONNECTIONS_DETAIL_ERR) {
        return action.reason;
    } else if (action.type === CONNECTIONS_DETAIL_LOAD) {
        return null;
    }
    return state;
}

function connectionName(state = null, action) {
    if (action.type === VIEW_CONNECTION) {
        return action.connection;
    } else if (action.type === VIEW_ALL) {
        return null;
    }
    return state;
}


function permissions(state = {'config': false}, action) {
    if (action.type === FETCH_PERMISSIONS) {
        return action.permissions;
    }
    return state;
}

const reducer = combineReducers({
    getConnections,
    getConnectionsError,
    connectionName,
    permissions,
    getDetails,
    getDetailsError,
});
export default reducer;

/*
 ACTION CREATORS
 */
export function getConnectionsStatus(startNextPoll) {
    return function (dispatch) {
        fetch(`/main/data/neo4j/status/connections`, {
            method: 'get',
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                if(startNextPoll()){
                    dispatch({type: CONNECTIONS_LOAD, connections: json})
                }

            })
            .catch(reason => {
                startNextPoll();
                dispatch({type: CONNECTIONS_ERR, reason: reason.toString()});
            });
    }
}