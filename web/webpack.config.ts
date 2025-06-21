const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const outputFile = 'neo4jstatus.js';

const config = {
    entry: './src/index.tsx',
    devtool: 'source-map',
    output: {
        path: path.join(__dirname, 'build/generated-resources/mounted/js'),
        filename: outputFile,
        publicPath: '/res/neo4j/js/',  // Updated to match your module's path
        library: 'neo4jstatus',
        libraryTarget: 'var'
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.jsx', '.js'],
        modules: ['node_modules']
    },
    module: {
        rules: [
            {
                test: /\.(tsx|ts)$/,
                use: {
                    loader: 'ts-loader',
                    options: {
                        transpileOnly: true
                    }
                },
                exclude: /(node_modules|bower_components)/,
            },
            {
                test: /\.css$/,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader'
                ],
            }
        ]
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: 'neo4jstatus.css',
        }),
    ],
    // Use the EXACT same externals format as your working config
    externals: [
        { react: 'React' },
        { 'react-dom': 'ReactDOM' },
        { 'ignition-react': 'IgnitionReact' },
        { 'ignition-lib': 'IgnitionLib' },
        { 'moment': 'moment' },
        { 'numeral': 'numeral' }
    ]
};

module.exports = config;