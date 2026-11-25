import SwiftUI

struct ContentView: View {
    var body: some View {
        ContentUnavailableView(
            "AdventureLog",
            systemImage: "map",
            description: Text("The iOS app is being rebuilt.")
        )
    }
}

#Preview {
    ContentView()
}
