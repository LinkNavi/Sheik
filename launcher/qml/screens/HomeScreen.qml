import QtQuick 6.0
import QtQuick.Controls 6.0

// HomeScreen — Phase 1: just launches the jar
Item {
    id: root

    // This signal will be connected to a C++ LaunchController later.
    // For now the button just logs.
    signal launchRequested()

    Rectangle {
        anchors.fill: parent
        color: "transparent"

        // ── Big logo / title ──────────────────────────────────────────
        Column {
            anchors.centerIn: parent
            spacing: 32

            Text {
                anchors.horizontalCenter: parent.horizontalCenter
                text: "Sheik"
                font.pixelSize: 52
                font.bold: true
                color: "#e0e0e0"
                // Simple letter-spacing via font.letterSpacing if needed
            }

            Text {
                anchors.horizontalCenter: parent.horizontalCenter
                text: "1.8.9 — OptiFine HD U H8"
                font.pixelSize: 14
                color: "#888888"
            }

            // ── Launch button ─────────────────────────────────────────
            Rectangle {
                id: launchBtn
                anchors.horizontalCenter: parent.horizontalCenter
                width: 220
                height: 48
                radius: 8
                color: launchHover.containsMouse ? "#5b8dd9" : "#3a6fd8"

                Behavior on color {
                    ColorAnimation { duration: 120 }
                }

                Text {
                    anchors.centerIn: parent
                    text: "LAUNCH"
                    font.pixelSize: 15
                    font.bold: true
                    font.letterSpacing: 2
                    color: "white"
                }

                MouseArea {
                    id: launchHover
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        Launcher.launch()
                        StackView.view.replace(Qt.resolvedUrl("LogScreen.qml"))
                    }
                }
            }
        }

        // ── Account info strip at the bottom ─────────────────────────
        Rectangle {
            anchors.bottom: parent.bottom
            anchors.left: parent.left
            anchors.right: parent.right
            height: 44
            color: "#111111"

            // Player head icon — swap source from C++ once logged in:
            //   playerHead.source = "image://heads/" + uuid
            Image {
                id: playerHead
                anchors.verticalCenter: parent.verticalCenter
                anchors.left: parent.left
                anchors.leftMargin: 12
                width: 28
                height: 28
                source: ""
                fillMode: Image.PreserveAspectFit
                smooth: false   // keep pixel-art sharp

                // Fallback shown while source is empty / loading
                Rectangle {
                    anchors.fill: parent
                    visible: playerHead.status !== Image.Ready
                    color: "#333333"
                    radius: 3
                }
            }

            Text {
                anchors.verticalCenter: parent.verticalCenter
                anchors.left: playerHead.right
                anchors.leftMargin: 8
                // TODO: bind to C++ account model
                text: "Not logged in"
                color: "#666666"
                font.pixelSize: 13
            }

            Rectangle {
                anchors.verticalCenter: parent.verticalCenter
                anchors.right: parent.right
                anchors.rightMargin: 16
                width: 100
                height: 28
                radius: 5
                color: "#2a2a2a"

                Text {
                    anchors.centerIn: parent
                    text: "Sign in"
                    color: "#aaaaaa"
                    font.pixelSize: 12
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: console.log("Auth flow — TODO")
                }
            }
        }
    }
}
