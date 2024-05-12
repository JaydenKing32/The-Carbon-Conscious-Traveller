import SwiftUI
import shared

@main
struct iosAppApp: App {
  init() {
    KoinHelper_iosKt.doInitKoin()
  }
  var body: some Scene {
    WindowGroup {
      ContentView()
    }
  }
}
