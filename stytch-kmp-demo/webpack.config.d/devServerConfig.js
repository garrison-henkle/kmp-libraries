// serves index.html for any path
config.devServer = {
    ...config.devServer,
    historyApiFallback: true,
    proxy: [
        {
            target: "http://localhost:8080",
            context: ['/oauth'],
            pathRewrite: function(pathname, req) {
                if (pathname.indexOf("sampleApp.wasm") >= 0) {
                    return "/sampleApp.wasm"
                } else {
                    return "/" + pathname.split("/").pop()
                }
            }
        }
    ]
};
