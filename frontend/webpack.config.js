const path = require("path");
const HtmlWebPackPlugin = require("html-webpack-plugin"); // eslint-disable-line import/no-extraneous-dependencies
const CopyWebpackPlugin = require("copy-webpack-plugin");
const EnvironmentPlugin = require("webpack/lib/EnvironmentPlugin");

module.exports = env => {
    // console.log("process.env: ", process.env)

    const production = process.env.NODE_ENV !== 'DEBUG'
    if (production) {
        console.log('Welcome to production');
    } else {
        console.log("Welcome to non-production")
    }
    if (process.env.DEBUG) {
        console.log('Debugging output');
    }

    return {
        entry: [
            // 'babel-polyfill',
            path.join(__dirname)
        ],
        devtool: "cheap-module-source-map",
        module: {
            rules: [
                {
                    test: /\.js|jsx$/,
                    exclude: /node_modules\/(?!(@flock-eco)\/).*/,
                    use: {
                        loader: "babel-loader",
                        options: {
                            plugins: ["@babel/plugin-proposal-class-properties"],
                            presets: ["@babel/preset-env", "@babel/preset-react"]
                        }
                    }
                },
                {
                    test: /\.css$/i,
                    use: ["style-loader", "css-loader"]
                }
            ]
        },

        plugins: [
            new HtmlWebPackPlugin({
                template: path.join(__dirname, "index.html"),
                filename: "./index.html"
            }),
            new CopyWebpackPlugin([
                // "src/main/react/images"
                // {from: "react/images", to: "images", context: "src/main" },
                // { from: "react/manifest.json", to: "webapp/", context: "src/main" },
                "./manifest.json",
                // "src/main/react/eventSource.js"
            ]),
            new EnvironmentPlugin(['HOST'])

        ],

        devServer: {
            historyApiFallback: true,
            port: 3000,
            proxy: {
                "/ws/**": "http://localhost:8080",
                "/sockjs-node/**": "http://localhost:8080",
                '/api': 'http://localhost:8080'
            },
        }
    }
};
