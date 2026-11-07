// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorGoogleSignIn",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorGoogleSignIn",
            targets: ["GoogleSignInPlugin"])
    ],
    dependencies: [
        // A range on purpose: the app pins Capacitor exactly, and any 8.x has to resolve.
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0"),
        .package(url: "https://github.com/google/GoogleSignIn-iOS.git", exact: "10.1.0")
    ],
    targets: [
        .target(
            name: "GoogleSignInPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "GoogleSignIn", package: "GoogleSignIn-iOS")
            ],
            path: "ios/Sources/GoogleSignInPlugin"),
        .testTarget(
            name: "GoogleSignInPluginTests",
            dependencies: ["GoogleSignInPlugin"],
            path: "ios/Tests/GoogleSignInPluginTests")
    ]
)
