var path = require('path');
var webpack = require('webpack');
var UglifyJsPlugin = require('uglifyjs-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');
var CleanWebpackPlugin = require('clean-webpack-plugin');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const devMode = process.env.NODE_ENV !== 'production';
var babelOptions = {
  "presets": [["es2015", {
    "modules": false
  }]]
};
module.exports = {
  target: 'node',
  entry: {
    'downloadTool.min': path.resolve(__dirname, '../src/Bootstrap.js'),

  },
  output: {
    /* Webpack producing results */
    path: path.resolve(__dirname, '../build'),
    filename: 'downloadTool/js/[name].js',
    library: 'DownloadToolClient',
    libraryTarget: 'var'
  },
  module: {
		rules: [{
			test: /\.js$/,
			exclude: /node_modules/,
			use: [{
				loader: 'babel-loader',
				options: {
					"presets": ["env"]
				}
			}]
		},

    {
      test: /\.hbs$/,
      loader: 'handlebars-loader'
    },
    // added to webpack to be able to import css file to index.js (render)
    // installed: npm install style-loader css-loader --save-dev
    {
      test: /\.css$/,
      use: [MiniCssExtractPlugin.loader, "css-loader"]
    }, {
      test: /\.scss$/,
      use: [MiniCssExtractPlugin.loader, "css-loader", // translates
                                // CSS into
                                // CommonJS
      "sass-loader" // compiles Sass to CSS, using Node Sass by default
      ]
    }, {
      test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
      use: [{
        loader: 'url-loader',
        options: {
          limit: 10000,
          url: false
        }
      },

      ]
    },

    ]
  },
  stats: {
    colors: true
  },

  plugins: [
      new CleanWebpackPlugin(['build']),
      new webpack.ContextReplacementPlugin(/graphql-language-service-interface[\\/]dist$/, new RegExp(
              '^\\./.*\\.js$')),
      new MiniCssExtractPlugin({
        filename: "downloadTool/styles/downloadTool.min.css"
      })],

};
