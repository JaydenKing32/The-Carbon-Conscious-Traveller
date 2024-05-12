import SwiftUI
import UIKit
import shared

struct ComposeView: UIViewControllerRepresentable {
  func makeUIViewController(context: Context) -> UIViewController {
    MainViewController_iosKt.MainViewController()
  }

  func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
  var body: some View {
    // Compose has own keyboard handler
    ComposeView().ignoresSafeArea(.keyboard)
  }
}
