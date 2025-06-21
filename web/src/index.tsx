import React from 'react';
import { createStore, applyMiddleware } from 'redux';
import { Provider } from 'react-redux';
import { thunk } from 'redux-thunk';
const createReactClass = require('create-react-class');

import reducer from './model/model';
import ConnectStatus from './components/Neo4JStatus';

const createStoreWithMiddleware = applyMiddleware(thunk)(createStore);
const store = createStoreWithMiddleware(reducer);

const MountableApp = createReactClass({
    render: function () {
        // Remove manual dispatch prop - connect() provides it automatically
        return <Provider store={store}><ConnectStatus /></Provider>
    }
});

export default MountableApp;