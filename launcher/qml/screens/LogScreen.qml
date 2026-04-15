import QtQuick 6.0
import QtQuick.Controls 6.0

Item {
    id: root

    // ── Restore log lines if navigating back to this screen ───────────
    Component.onCompleted: {
        const lines = Launcher.logLines
        for (let i = 0; i < lines.length; i++)
            logModel.append({ line: lines[i] })
        Qt.callLater(function() { logView.positionViewAtEnd() })

        // Reflect current process state
        if (Launcher.running) {
            statusText.text = "Running"
            statusDot.color = "#4caf50"
        }
    }

    ListModel { id: logModel }

    Connections {
        target: Launcher

        function onLogLine(text) {
            logModel.append({ line: text })
            Qt.callLater(function() { logView.positionViewAtEnd() })
        }

        function onProcessStarted() {
            logModel.clear()
            statusText.text = "Running"
            statusDot.color = "#4caf50"
        }

        function onProcessFinished(exitCode) {
            statusText.text = "Exited (" + exitCode + ")"
            statusDot.color = exitCode === 0 ? "#4caf50" : "#f44336"
        }
    }

    // ── Header bar ────────────────────────────────────────────────────
    Rectangle {
        id: header
        anchors.top: parent.top
        anchors.left: parent.left
        anchors.right: parent.right
        height: 44
        color: "#111111"

        Text {
            anchors.verticalCenter: parent.verticalCenter
            anchors.left: parent.left
            anchors.leftMargin: 16
            text: "Console"
            font.pixelSize: 14
            font.bold: true
            color: "#cccccc"
        }

        // ── Status indicator ──────────────────────────────────────────
        Row {
            anchors.verticalCenter: parent.verticalCenter
            anchors.right: killBtn.left
            anchors.rightMargin: 16
            spacing: 6

            Rectangle {
                id: statusDot
                width: 8; height: 8
                radius: 4
                anchors.verticalCenter: parent.verticalCenter
                color: "#555555"
            }

            Text {
                id: statusText
                text: "Idle"
                font.pixelSize: 12
                color: "#888888"
                anchors.verticalCenter: parent.verticalCenter
            }
        }

        // ── Kill button ───────────────────────────────────────────────
        Rectangle {
            id: killBtn
            anchors.verticalCenter: parent.verticalCenter
            anchors.right: parent.right
            anchors.rightMargin: 16
            width: 80
            height: 28
            radius: 5
            color: Launcher.running
                   ? (killHover.containsMouse ? "#c62828" : "#b71c1c")
                   : "#2a2a2a"
            opacity: Launcher.running ? 1.0 : 0.4

            Behavior on color { ColorAnimation { duration: 100 } }

            Text {
                anchors.centerIn: parent
                text: "Kill"
                font.pixelSize: 12
                font.bold: true
                color: Launcher.running ? "white" : "#666666"
            }

            MouseArea {
                id: killHover
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Launcher.running ? Qt.PointingHandCursor : Qt.ArrowCursor
                enabled: Launcher.running
                onClicked: Launcher.kill()
            }
        }
    }

    // ── Log view ──────────────────────────────────────────────────────
    ListView {
        id: logView
        anchors.top: header.bottom
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.bottom: statusBar.top
        clip: true
        model: logModel
        spacing: 0

        ScrollBar.vertical: ScrollBar { policy: ScrollBar.AsNeeded }

        delegate: Text {
            width: logView.width - 16
            leftPadding: 10
            topPadding: 1
            bottomPadding: 1
            text: model.line
            font.family: "monospace"
            font.pixelSize: 12
            color: model.line.startsWith("[Launcher]") ? "#6ea8d8"
                 : model.line.startsWith("[ERROR]")    ? "#f44336"
                 : model.line.startsWith("[WARN]")     ? "#ffb74d"
                 : "#cccccc"
            wrapMode: Text.WrapAnywhere
        }

        // ── Empty state ───────────────────────────────────────────────
        Text {
            anchors.centerIn: parent
            visible: logModel.count === 0
            text: "No output yet.\nPress Launch on the Home screen to start the client."
            horizontalAlignment: Text.AlignHCenter
            color: "#444444"
            font.pixelSize: 13
        }
    }

    // ── Status bar ────────────────────────────────────────────────────
    Rectangle {
        id: statusBar
        anchors.bottom: parent.bottom
        anchors.left: parent.left
        anchors.right: parent.right
        height: 24
        color: "#0d0d0d"

        Text {
            anchors.verticalCenter: parent.verticalCenter
            anchors.left: parent.left
            anchors.leftMargin: 10
            text: logModel.count + " lines"
            font.pixelSize: 10
            color: "#444444"
        }
    }
}
