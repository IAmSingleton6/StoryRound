import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        InitializeStoryRoundIosKt.initializeStoryRoundIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
