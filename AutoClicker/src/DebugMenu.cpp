#include "DebugMenu.h"
#include <QDebug>

static QLabel* makeKeyLabel(const QString &text, QWidget *parent) {
    auto *lbl = new QLabel(text, parent);
    lbl->setObjectName("debugKey");
    return lbl;
}

DebugMenu::DebugMenu(QWidget *parent) : QMainWindow(parent) {
    centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

    layout = new QVBoxLayout(centralWidget);
    layout->setSpacing(0);
    layout->setContentsMargins(0, 0, 0, 0);

    // ── Top bar ──────────────────────────────────────────────────
    auto *topBar = new QWidget(this);
    topBar->setObjectName("topBar");
    topBarLayout = new QHBoxLayout(topBar);
    topBarLayout->setContentsMargins(16, 10, 16, 10);

    btnBack = new QPushButton("← Back", this);
    btnBack->setObjectName("btnSecondary");
    auto *titleLabel = new QLabel("DEBUG", this);
    titleLabel->setObjectName("appTitle");

    topBarLayout->addWidget(btnBack, 0, Qt::AlignLeft);
    topBarLayout->addWidget(titleLabel, 1, Qt::AlignCenter);
    topBarLayout->addSpacing(60);
    layout->addWidget(topBar);

    // ── Content ──────────────────────────────────────────────────
    auto *content = new QWidget(this);
    auto *contentLayout = new QVBoxLayout(content);
    contentLayout->setSpacing(12);
    contentLayout->setContentsMargins(20, 20, 20, 20);

    // ── Target HP panel ──────────────────────────────────────────
    auto *hpPanel = new QWidget(this);
    hpPanel->setObjectName("infoPanel");
    auto *hpPanelLayout = new QVBoxLayout(hpPanel);
    hpPanelLayout->setContentsMargins(14, 12, 14, 12);
    hpPanelLayout->setSpacing(6);

    auto *hpTitle = new QLabel("TARGET HEALTH", this);
    hpTitle->setObjectName("panelTitle");

    lblHealthVal = new QLabel("—", this);
    lblHealthVal->setObjectName("infoValue");

    hpBar = new QProgressBar(this);
    hpBar->setRange(0, 20);
    hpBar->setValue(0);
    hpBar->setTextVisible(false);
    hpBar->setFixedHeight(8);
    hpBar->setObjectName("hpBar");

    hpPanelLayout->addWidget(hpTitle);
    hpPanelLayout->addWidget(lblHealthVal);
    hpPanelLayout->addWidget(hpBar);
    contentLayout->addWidget(hpPanel);

    // ── Stats grid ───────────────────────────────────────────────
    auto *statsPanel = new QWidget(this);
    statsPanel->setObjectName("infoPanel");
    auto *grid = new QGridLayout(statsPanel);
    grid->setContentsMargins(14, 12, 14, 12);
    grid->setSpacing(8);
    grid->setColumnStretch(1, 1);

    auto addRow = [&](int row, const QString &key, QLabel *&valOut) {
        grid->addWidget(makeKeyLabel(key, this), row, 0);
        valOut = new QLabel("—", this);
        valOut->setObjectName("debugVal");
        grid->addWidget(valOut, row, 1);
    };

    addRow(0, "Yaw",        lblYawVal);
    addRow(1, "Pitch",      lblPitchVal);
    addRow(2, "Target ID",  lblTargetVal);
    addRow(3, "Held Item",  lblHeldVal);
    addRow(4, "Block Hit",  lblBlockVal);
    addRow(5, "In GUI",     lblGuiVal);

    contentLayout->addWidget(statsPanel);
    contentLayout->addStretch();
    layout->addWidget(content);

    // ── Connections ──────────────────────────────────────────────
    connect(btnBack, &QPushButton::clicked, this, [this]() {
        emit goBack();
    });

    timer = new QTimer(this);
    connect(timer, &QTimer::timeout, this, &DebugMenu::updateLoop);
    timer->start(100);
}

QLabel* DebugMenu::makeStatLabel(const QString &text) {
    auto *lbl = new QLabel(text, this);
    lbl->setObjectName("debugVal");
    return lbl;
}

void DebugMenu::updateLoop() {
    if (!ipc.valid()) {
        lblYawVal->setText("no shm");
        return;
    }

    ipc.update();
    const auto* s = ipc.state();

    lblYawVal->setText(QString("%1°").arg(s->yaw, 0, 'f', 2));
    lblPitchVal->setText(QString("%1°").arg(s->pitch, 0, 'f', 2));
    lblTargetVal->setText(s->targetId ? QString("#%1").arg(s->targetId) : "none");
    lblHeldVal->setText(QString("%1").arg(s->heldItem));
    lblBlockVal->setText(s->lookingAtBlock ? "Yes" : "No");
    lblGuiVal->setText(s->inGui ? "Open" : "Closed");

    float hp = s->targetHealth;
    lblHealthVal->setText(s->targetId
        ? QString("%1 / 20").arg(hp, 0, 'f', 1)
        : "—");
    hpBar->setValue(s->targetId ? static_cast<int>(hp) : 0);
}