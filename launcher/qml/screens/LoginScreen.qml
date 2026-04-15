import QtQuick 6.0
import QtQuick.Controls 6.0

Item {
    id: root

    // Navigate back to Home after a successful login
    Connections {
        target: Auth
        function onLoginSuccess() {
            root.StackView.view.replace(Qt.resolvedUrl("HomeScreen.qml"))
        }
    }

    Column {
        anchors.centerIn: parent
        spacing: 22
        width: Math.min(parent.width - 80, 440)

        // ── Title ──────────────────────────────────────────────────────
        Text {
            anchors.horizontalCenter: parent.horizontalCenter
            text: "Sign in with Microsoft"
            font.pixelSize: 22
            font.bold: true
            color: "#e0e0e0"
        }

        // ── Status text ────────────────────────────────────────────────
        Text {
            anchors.horizontalCenter: parent.horizontalCenter
            width: parent.width
            horizontalAlignment: Text.AlignHCenter
            text: Auth.status
            color: "#aaaaaa"
            font.pixelSize: 13
            wrapMode: Text.WordWrap
            visible: Auth.status !== ""
        }

        // ── User code display ──────────────────────────────────────────
        Rectangle {
            anchors.horizontalCenter: parent.horizontalCenter
            visible: Auth.userCode !== ""
            width: 260
            height: 70
            radius: 8
            color: "#1e1e1e"
            border.color: "#3a3a3a"

            Text {
                anchors.centerIn: parent
                text: Auth.userCode
                color: "#ffffff"
                font.pixelSize: 30
                font.bold: true
                font.letterSpacing: 5
                font.family: "monospace"
            }
        }

        // ── URL + buttons row ──────────────────────────────────────────
        Column {
            anchors.horizontalCenter: parent.horizontalCenter
            visible: Auth.verificationUrl !== ""
            spacing: 10

            Text {
                anchors.horizontalCenter: parent.horizontalCenter
                text: Auth.verificationUrl
                color: "#7aaef8"
                font.pixelSize: 13
            }

            Row {
                anchors.horizontalCenter: parent.horizontalCenter
                spacing: 10

                // Open in browser
                Rectangle {
                    width: 130
                    height: 30
                    radius: 5
                    color: openHover.containsMouse ? "#3a5595" : "#2a3f7a"
                    Behavior on color { ColorAnimation { duration: 100 } }

                    Text {
                        anchors.centerIn: parent
                        text: "Open Browser"
                        color: "#ccd6f6"
                        font.pixelSize: 12
                    }

                    MouseArea {
                        id: openHover
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: Qt.openUrlExternally(Auth.verificationUrl)
                    }
                }

                // Copy code
                Rectangle {
                    id: copyBtn
                    width: 100
                    height: 30
                    radius: 5
                    color: copyHover.containsMouse ? "#3a3a3a" : "#252525"
                    border.color: "#4a4a4a"
                    Behavior on color { ColorAnimation { duration: 100 } }

                    property bool copied: false

                    Text {
                        anchors.centerIn: parent
                        text: copyBtn.copied ? "Copied!" : "Copy code"
                        color: copyBtn.copied ? "#66bb6a" : "#cccccc"
                        font.pixelSize: 12
                    }

                    MouseArea {
                        id: copyHover
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: {
                            Auth.copyToClipboard(Auth.userCode)
                            copyBtn.copied = true
                            copyTimer.restart()
                        }
                    }

                    Timer {
                        id: copyTimer
                        interval: 2000
                        onTriggered: copyBtn.copied = false
                    }
                }
            }
        }

        // ── Cancel button ──────────────────────────────────────────────
        Rectangle {
            anchors.horizontalCenter: parent.horizontalCenter
            width: 130
            height: 34
            radius: 6
            color: cancelHover.containsMouse ? "#3a2020" : "#2a1818"
            border.color: "#5a3333"
            Behavior on color { ColorAnimation { duration: 100 } }

            Text {
                anchors.centerIn: parent
                text: "Cancel"
                color: "#cc9999"
                font.pixelSize: 13
            }

            MouseArea {
                id: cancelHover
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    Auth.cancelLogin()
                    root.StackView.view.replace(Qt.resolvedUrl("HomeScreen.qml"))
                }
            }
        }
    }

    // Start the flow as soon as this screen loads
    Component.onCompleted: Auth.startLogin()
}
