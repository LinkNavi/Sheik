#pragma once
#include <QString>

inline QString sheikStyleSheet() {
    return R"(
/* ================================================================
   SHEIK AutoClicker — Dark Industrial Theme
   Palette:
     bg-deep   #0e0f11
     bg-mid    #16181c
     bg-panel  #1e2128
     bg-hover  #252830
     border    #2e3240
     accent    #39d353   (terminal green)
     accent2   #1fa83e
     danger    #e05252
     text-pri  #e8eaf0
     text-sec  #7a8099
     text-dim  #3d4258
   ================================================================ */

/* ── Base ──────────────────────────────────────────────────────── */
QMainWindow, QWidget {
    background-color: #0e0f11;
    color: #e8eaf0;
    font-family: "JetBrains Mono", "Cascadia Code", "Fira Code", monospace;
    font-size: 13px;
}

QStackedWidget {
    background-color: #0e0f11;
}

/* ── Top bar ───────────────────────────────────────────────────── */
QWidget#topBar {
    background-color: #16181c;
    border-bottom: 1px solid #2e3240;
}

QLabel#appTitle {
    font-size: 15px;
    font-weight: bold;
    letter-spacing: 4px;
    color: #39d353;
}

QLabel#screenTitle {
    font-size: 14px;
    font-weight: bold;
    letter-spacing: 3px;
    color: #e8eaf0;
}

/* ── Status dots ───────────────────────────────────────────────── */
QLabel#dotConnected {
    color: #39d353;
    font-size: 10px;
}
QLabel#dotIdle {
    color: #f0a030;
    font-size: 10px;
}
QLabel#dotDisconnected {
    color: #e05252;
    font-size: 10px;
}
QLabel#statusText {
    color: #7a8099;
    font-size: 12px;
}

/* ── Info / stat panels ────────────────────────────────────────── */
QWidget#infoPanel {
    background-color: #16181c;
    border: 1px solid #2e3240;
    border-radius: 6px;
}

QLabel#panelTitle {
    font-size: 10px;
    letter-spacing: 2px;
    color: #3d4258;
    font-weight: bold;
}

QLabel#infoValue {
    font-size: 18px;
    font-weight: bold;
    color: #e8eaf0;
}

QLabel#infoValueSub {
    font-size: 12px;
    color: #7a8099;
}

QLabel#debugKey {
    color: #7a8099;
    font-size: 12px;
    min-width: 80px;
}

QLabel#debugVal {
    color: #39d353;
    font-size: 13px;
    font-weight: bold;
}

/* ── HP Bar ────────────────────────────────────────────────────── */
QProgressBar#hpBar {
    background-color: #1e2128;
    border: none;
    border-radius: 3px;
}
QProgressBar#hpBar::chunk {
    background-color: qlineargradient(x1:0, y1:0, x2:1, y2:0,
        stop:0 #1fa83e, stop:1 #39d353);
    border-radius: 3px;
}

/* ── Buttons ───────────────────────────────────────────────────── */
QPushButton {
    border-radius: 4px;
    padding: 7px 16px;
    font-size: 12px;
    font-weight: bold;
    letter-spacing: 1px;
    font-family: "JetBrains Mono", "Cascadia Code", "Fira Code", monospace;
    border: 1px solid #2e3240;
    background-color: #1e2128;
    color: #e8eaf0;
}
QPushButton:hover {
    background-color: #252830;
    border-color: #3d4258;
}
QPushButton:pressed {
    background-color: #16181c;
}

/* Start/stop toggle */
QPushButton#btnToggleOff {
    background-color: #1a2e1f;
    border: 1px solid #39d353;
    color: #39d353;
    font-size: 15px;
    letter-spacing: 3px;
}
QPushButton#btnToggleOff:hover {
    background-color: #1e3523;
    border-color: #4ae866;
    color: #4ae866;
}
QPushButton#btnToggleOn {
    background-color: #2e1a1a;
    border: 1px solid #e05252;
    color: #e05252;
    font-size: 15px;
    letter-spacing: 3px;
}
QPushButton#btnToggleOn:hover {
    background-color: #381f1f;
    border-color: #f07070;
    color: #f07070;
}

/* Secondary (muted) button */
QPushButton#btnSecondary {
    background-color: transparent;
    border: 1px solid #2e3240;
    color: #7a8099;
}
QPushButton#btnSecondary:hover {
    background-color: #1e2128;
    color: #e8eaf0;
    border-color: #4a5070;
}

/* Danger button */
QPushButton#btnDanger {
    background-color: transparent;
    border: 1px solid #3d2020;
    color: #7a4040;
}
QPushButton#btnDanger:hover {
    background-color: #2e1a1a;
    border-color: #e05252;
    color: #e05252;
}

/* Icon-only button (settings gear) */
QPushButton#btnIcon {
    background-color: transparent;
    border: none;
    color: #7a8099;
    font-size: 16px;
    padding: 4px;
}
QPushButton#btnIcon:hover {
    color: #e8eaf0;
    background-color: #1e2128;
    border-radius: 4px;
}

/* Primary action button */
QPushButton#btnPrimary {
    background-color: #1a2e1f;
    border: 1px solid #39d353;
    color: #39d353;
}
QPushButton#btnPrimary:hover {
    background-color: #1e3523;
}

/* ── GroupBox ──────────────────────────────────────────────────── */
QGroupBox {
    border: 1px solid #2e3240;
    border-radius: 6px;
    margin-top: 10px;
    padding-top: 6px;
    font-size: 10px;
    letter-spacing: 2px;
    color: #3d4258;
    font-weight: bold;
}
QGroupBox::title {
    subcontrol-origin: margin;
    subcontrol-position: top left;
    padding: 0 6px;
    left: 10px;
    color: #3d4258;
}

/* ── Inputs ────────────────────────────────────────────────────── */
QSpinBox, QComboBox, QKeySequenceEdit {
    background-color: #16181c;
    border: 1px solid #2e3240;
    border-radius: 4px;
    padding: 5px 8px;
    min-height: 28px;
    color: #e8eaf0;
    font-size: 13px;
    font-family: "JetBrains Mono", "Cascadia Code", "Fira Code", monospace;
}
QSpinBox:focus, QComboBox:focus, QKeySequenceEdit:focus {
    border-color: #39d353;
}
QSpinBox::up-button, QSpinBox::down-button {
    background-color: #1e2128;
    border: none;
    width: 18px;
}
QSpinBox::up-arrow  { image: none; }
QSpinBox::down-arrow { image: none; }

QComboBox::drop-down {
    border: none;
    background: transparent;
}
QComboBox QAbstractItemView {
    background-color: #16181c;
    border: 1px solid #2e3240;
    selection-background-color: #1e2128;
    selection-color: #39d353;
}

/* ── Checkbox ──────────────────────────────────────────────────── */
QCheckBox {
    color: #e8eaf0;
    spacing: 8px;
    font-size: 13px;
}
QCheckBox::indicator {
    width: 16px;
    height: 16px;
    border: 1px solid #2e3240;
    border-radius: 3px;
    background-color: #16181c;
}
QCheckBox::indicator:checked {
    background-color: #39d353;
    border-color: #39d353;
}
QCheckBox::indicator:hover {
    border-color: #4a5070;
}

/* ── Scrollbar ─────────────────────────────────────────────────── */
QScrollBar:vertical {
    background: #0e0f11;
    width: 6px;
    margin: 0;
}
QScrollBar::handle:vertical {
    background: #2e3240;
    border-radius: 3px;
    min-height: 20px;
}
QScrollBar::add-line:vertical, QScrollBar::sub-line:vertical {
    height: 0;
}

/* ── QLabel (form rows) ────────────────────────────────────────── */
QLabel {
    color: #e8eaf0;
}
)";
}
