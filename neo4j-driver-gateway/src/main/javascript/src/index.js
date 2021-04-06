import React from 'react';
import { createStore, applyMiddleware } from 'redux';
import { Provider } from 'react-redux';
import thunkMiddleware from 'redux-thunk';
var createReactClass = require('create-react-class');

import reducer from './model.js';
import ConnectStatus from './Neo4JStatus.jsx';

const createStoreWithMiddleware = applyMiddleware(thunkMiddleware)(createStore);
const store = createStoreWithMiddleware(reducer);

const MountableApp = createReactClass({
    render: function() {
        return <Provider store={store}><ConnectStatus dispatch={store.dispatch}/></Provider>
    }
});

export default MountableApp;