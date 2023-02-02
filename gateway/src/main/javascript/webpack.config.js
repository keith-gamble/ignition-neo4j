var webpack = require('webpack');
var path = require('path');

var outputFile = 'neo4jstatus.js';

var config = {
  entry: './src/index.js',
  devtool: 'source-map',
  output: {
    path: path.join(
        __dirname,
        '../resources/mounted/js'),
    filename: outputFile,
    publicPath: '/dist',
    library: 'neo4jstatus',
    libraryTarget: 'var'
  },
  resolve: {
    modules: ['node_modules']
  },
  module: {
    rules: [
      {
        test: /(\.jsx|\.js)$/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ["@babel/preset-env", "@babel/preset-react"]
          }
        },
        exclude: /(node_modules|bower_components)/,
      }
    ]
  },
  plugins: [],
  externals: [
    {react: 'React'},
    {'react-dom': 'ReactDOM'},
    {'ignition-react': 'IgnitionReact'},
    {'ignition-lib': 'IgnitionLib'},
    {'moment': 'moment'},
    {'numeral': 'numeral'}
  ]
};

module.exports = config;