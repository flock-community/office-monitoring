const path = require("path");
const HtmlWebPackPlugin = require("html-webpack-plugin"); // eslint-disable-line import/no-extraneous-dependencies
const CopyWebpackPlugin = require("copy-webpack-plugin");

module.exports = {
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
        // use: ["style-loader", "css-loader"]
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
    ])
  ],

  devServer: {
    historyApiFallback: true,
    port: 3000,
    proxy: {
      "/wordclouds": "http://localhost:8080",
      "/wordclouds/**": "http://localhost:8080",
      "/ws/**": "http:localhost:8080",
      "/sockjs-node/**": "http:localhost:8080"
    }
  }
};
