import QtQuick 6.0
import QtQuick.Window 6.0
import QtQuick.Controls 6.0
import "components"

Window {
    id: root
    visible: true
    minimumWidth: 900
    minimumHeight: 560
    width: 900
    height: 560
    title: "Sheik"

    // Dark background
    color: "#1a1a1a"

    Row {
        anchors.fill: parent

        // ── Sidebar ──────────────────────────────────────────────────
        Sidebar {
            id: sidebar
            height: parent.height
            onNavigate: function(screen) { stack.replace(screen) }
        }

        // ── Main content area ─────────────────────────────────────────
        StackView {
            id: stack
            width: parent.width - sidebar.width
            height: parent.height

            initialItem: Qt.resolvedUrl("screens/HomeScreen.qml")

            // Slide transition
            pushEnter: Transition {
                PropertyAnimation { property: "x"; from: 40; to: 0; duration: 180; easing.type: Easing.OutQuad }
                PropertyAnimation { property: "opacity"; from: 0; to: 1; duration: 180 }
            }
            pushExit: Transition {
                PropertyAnimation { property: "opacity"; from: 1; to: 0; duration: 120 }
            }
            replaceEnter: pushEnter
            replaceExit: pushExit
        }
    }
}

