package dev.henkle.screenshots

external fun takeScreenshot(destinationUrl: String)

fun main() {
//    NativeLoader.loadLibrary("macos_screenshots")
    System.loadLibrary("macos_screenshots")
    takeScreenshot(destinationUrl = "file:///Users/garrison/Desktop/test1.png")
}
