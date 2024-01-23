import SwiftUI
import shared

struct SafeAreaObserverModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(GeometryReader { geometry in
                Color.clear.onAppear {
                    // Handle the initial safe area insets here
                    handleSafeAreaChange(geometry.safeAreaInsets)
                }
                .onChange(of: geometry.safeAreaInsets) { newInsets in
                    // Handle changes in safe area insets here
                    handleSafeAreaChange(newInsets)
                }
            })
    }

    private func handleSafeAreaChange(_ insets: EdgeInsets) {
        let safeAreaInsets = CGSafeAreaInsets(top: insets.top, left: insets.leading, bottom: insets.bottom, right: insets.trailing)
        NotificationCenter.default.post(name: NSNotification.Name("SafeAreaDidChange"), object: nil, userInfo: ["insets": safeAreaInsets])
    }
}

extension View {
    func observesSafeAreaChanges() -> some View {
        modifier(SafeAreaObserverModifier())
    }
}
