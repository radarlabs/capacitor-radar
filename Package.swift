// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorRadar",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorRadar",
            targets: ["RadarPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0"),
        .package(url: "https://github.com/radarlabs/radar-sdk-ios-spm.git", .upToNextMinor(from: "3.38.0")),
        .package(url: "https://github.com/radarlabs/radar-sdk-ios-fraud-spm.git", .upToNextMinor(from: "1.3.0"))
    ],
    targets: [
        .target(
            name: "RadarPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "RadarSDK", package: "radar-sdk-ios-spm"),
                .product(name: "RadarSDKFraud", package: "radar-sdk-ios-fraud-spm")
            ],
            path: "ios/Sources/RadarPlugin"),
        .testTarget(
            name: "RadarPluginTests",
            dependencies: ["RadarPlugin"],
            path: "ios/Tests/RadarPluginTests")
    ]
)