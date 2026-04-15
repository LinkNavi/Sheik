import QtQuick 6.0

// Sidebar — vertical nav strip
Rectangle {
    id: sidebar
    width: 64
    color: "#111111"

    // Emitted when user picks a screen.
    // `screen` is a URL string like "screens/ModulesScreen.qml"
    signal navigate(string screen)

    Column {
        anchors.top: parent.top
        anchors.topMargin: 16
        anchors.horizontalCenter: parent.horizontalCenter
        spacing: 8

        SidebarButton {
            icon: "⌂"
            tooltip: "Home"
            active: true
            onClicked: sidebar.navigate(Qt.resolvedUrl("../screens/HomeScreen.qml"))
        }

        SidebarButton {
            icon: "⚒"
            tooltip: "Settings"
            onClicked: sidebar.navigate(Qt.resolvedUrl("../screens/SettingsScreen.qml"))
        }

        SidebarButton {
            icon: "≡"
            tooltip: "Console"
            onClicked: sidebar.navigate(Qt.resolvedUrl("../screens/LogScreen.qml"))
        }
    }

    // Version label at the very bottom
    Text {
        anchors.bottom: parent.bottom
        anchors.bottomMargin: 12
        anchors.horizontalCenter: parent.horizontalCenter
        text: "v0.1"
        font.pixelSize: 10
        color: "#444444"
    }
}
