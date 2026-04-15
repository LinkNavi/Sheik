import QtQuick 6.0

// A single icon button in the sidebar
Rectangle {
    id: btn
    width: 44
    height: 44
    radius: 8
    color: active ? "#2a2a2a" : (hover.containsMouse ? "#1e1e1e" : "transparent")

    property string icon: ""
    property string tooltip: ""
    property bool active: false

    signal clicked()

    Behavior on color {
        ColorAnimation { duration: 100 }
    }

    Text {
        anchors.centerIn: parent
        text: btn.icon
        font.pixelSize: 20
        color: btn.active ? "#e0e0e0" : (hover.containsMouse ? "#bbbbbb" : "#666666")

        Behavior on color {
            ColorAnimation { duration: 100 }
        }
    }

    // Simple tooltip
    Rectangle {
        visible: hover.containsMouse
        x: btn.width + 6
        anchors.verticalCenter: parent.verticalCenter
        width: tipText.implicitWidth + 16
        height: 28
        radius: 5
        color: "#222222"
        z: 10

        Text {
            id: tipText
            anchors.centerIn: parent
            text: btn.tooltip
            color: "#cccccc"
            font.pixelSize: 12
        }
    }

    MouseArea {
        id: hover
        anchors.fill: parent
        hoverEnabled: true
        cursorShape: Qt.PointingHandCursor
        onClicked: btn.clicked()
    }
}
