import SwiftUI
import shared

@main
struct iosApp: App {
  init() {
    KoinHelper_iosKt.doInitKoin()
  }
  var body: some Scene {
    WindowGroup {
      ContentView()
    }
  }
}
