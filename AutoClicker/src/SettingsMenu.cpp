#include "SettingsMenu.h"
#include <QFormLayout>
#include <QScrollArea>
#include "IPC.h"

SettingsMenu::SettingsMenu(QWidget *parent)
    : QMainWindow(parent), settings("Sheik", "AutoClicker")
{
    auto *root = new QWidget(this);
    setCentralWidget(root);

    auto *rootLayout = new QVBoxLayout(root);
    rootLayout->setSpacing(0);
    rootLayout->setContentsMargins(0, 0, 0, 0);

    // ── Top bar ──────────────────────────────────────────────────
    auto *topBar = new QWidget(this);
    topBar->setObjectName("topBar");
    topBarLayout = new QHBoxLayout(topBar);
    topBarLayout->setContentsMargins(16, 10, 16, 10);

    btnBack = new QPushButton("← Back", this);
    btnBack->setObjectName("btnSecondary");
    auto *titleLabel = new QLabel("Settings", this);
    titleLabel->setObjectName("screenTitle");

    topBarLayout->addWidget(btnBack, 0, Qt::AlignLeft);
    topBarLayout->addWidget(titleLabel, 1, Qt::AlignCenter);
    topBarLayout->addSpacing(60);
    rootLayout->addWidget(topBar);

    // ── Scrollable content ───────────────────────────────────────
    auto *scroll = new QScrollArea(this);
    scroll->setWidgetResizable(true);
    scroll->setFrameShape(QFrame::NoFrame);

    centralWidget = new QWidget(scroll);
    scroll->setWidget(centralWidget);

    layout = new QVBoxLayout(centralWidget);
    layout->setSpacing(16);
    layout->setContentsMargins(20, 20, 20, 20);

    // ── Left click group ─────────────────────────────────────────
    grpLeft = new QGroupBox("LEFT CLICK", centralWidget);
    auto *leftForm = new QFormLayout(grpLeft);
    leftForm->setSpacing(10);
    leftForm->setContentsMargins(14, 20, 14, 14);
    leftForm->setLabelAlignment(Qt::AlignLeft);
    leftForm->setFieldGrowthPolicy(QFormLayout::AllNonFixedFieldsGrow);

    chkLeftEnabled       = new QCheckBox("Enabled", centralWidget);
    chkLeftAllowMining   = new QCheckBox("Allow mining blocks", centralWidget);
    chkLeftOnlyWithSword = new QCheckBox("Only with sword", centralWidget);
    chkLeftOnlyWithAxe   = new QCheckBox("Only with axe", centralWidget);

    spinLeftInterval = new QSpinBox(centralWidget);
    spinLeftInterval->setRange(10, 5000);
    spinLeftInterval->setSuffix(" ms");
    spinLeftInterval->setValue(100);

    spinLeftRandMin = new QSpinBox(centralWidget);
    spinLeftRandMin->setRange(0, 2000);
    spinLeftRandMin->setSuffix(" ms");
    spinLeftRandMin->setSpecialValueText("Off");

    spinLeftRandMax = new QSpinBox(centralWidget);
    spinLeftRandMax->setRange(0, 2000);
    spinLeftRandMax->setSuffix(" ms");
    spinLeftRandMax->setSpecialValueText("Off");

    leftForm->addRow(chkLeftEnabled);
    leftForm->addRow("Interval:",   spinLeftInterval);
    leftForm->addRow("Jitter min:", spinLeftRandMin);
    leftForm->addRow("Jitter max:", spinLeftRandMax);
    leftForm->addRow(chkLeftAllowMining);
    leftForm->addRow(chkLeftOnlyWithSword);
    leftForm->addRow(chkLeftOnlyWithAxe);

    layout->addWidget(grpLeft);

    connect(chkLeftEnabled, &QCheckBox::toggled, this, &SettingsMenu::updateLeftEnabled);
    connect(spinLeftRandMin, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinLeftRandMax->value() < v) spinLeftRandMax->setValue(v);
    });
    connect(spinLeftRandMax, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinLeftRandMin->value() > v) spinLeftRandMin->setValue(v);
    });

    // ── Right click group ────────────────────────────────────────
    grpRight = new QGroupBox("RIGHT CLICK", centralWidget);
    auto *rightForm = new QFormLayout(grpRight);
    rightForm->setSpacing(10);
    rightForm->setContentsMargins(14, 20, 14, 14);
    rightForm->setLabelAlignment(Qt::AlignLeft);
    rightForm->setFieldGrowthPolicy(QFormLayout::AllNonFixedFieldsGrow);

    chkRightEnabled      = new QCheckBox("Enabled", centralWidget);
    chkRightOnlyWithBlock = new QCheckBox("Only while holding a block", centralWidget);

    spinRightInterval = new QSpinBox(centralWidget);
    spinRightInterval->setRange(10, 5000);
    spinRightInterval->setSuffix(" ms");
    spinRightInterval->setValue(100);

    spinRightRandMin = new QSpinBox(centralWidget);
    spinRightRandMin->setRange(0, 2000);
    spinRightRandMin->setSuffix(" ms");
    spinRightRandMin->setSpecialValueText("Off");

    spinRightRandMax = new QSpinBox(centralWidget);
    spinRightRandMax->setRange(0, 2000);
    spinRightRandMax->setSuffix(" ms");
    spinRightRandMax->setSpecialValueText("Off");

    rightForm->addRow(chkRightEnabled);
    rightForm->addRow("Interval:",   spinRightInterval);
    rightForm->addRow("Jitter min:", spinRightRandMin);
    rightForm->addRow("Jitter max:", spinRightRandMax);
    rightForm->addRow(chkRightOnlyWithBlock);

    layout->addWidget(grpRight);

    connect(chkRightEnabled, &QCheckBox::toggled, this, &SettingsMenu::updateRightEnabled);
    connect(spinRightRandMin, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinRightRandMax->value() < v) spinRightRandMax->setValue(v);
    });
    connect(spinRightRandMax, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinRightRandMin->value() > v) spinRightRandMin->setValue(v);
    });

    layout->addStretch();

    // ── Action buttons ───────────────────────────────────────────
    auto *btnRow = new QHBoxLayout();
    btnReset = new QPushButton("Reset Defaults", centralWidget);
    btnSave  = new QPushButton("Save", centralWidget);
    btnReset->setObjectName("btnSecondary");
    btnSave->setObjectName("btnPrimary");
    btnRow->addWidget(btnReset);
    btnRow->addStretch();
    btnRow->addWidget(btnSave);
    layout->addLayout(btnRow);

    rootLayout->addWidget(scroll);

    connect(btnBack,  &QPushButton::clicked, this, &SettingsMenu::goBack);
    connect(btnSave,  &QPushButton::clicked, this, &SettingsMenu::saveSettings);
    connect(btnReset, &QPushButton::clicked, this, &SettingsMenu::resetDefaults);

    loadSettings();
}

void SettingsMenu::updateLeftEnabled(bool enabled) {
    spinLeftInterval->setEnabled(enabled);
    spinLeftRandMin->setEnabled(enabled);
    spinLeftRandMax->setEnabled(enabled);
    chkLeftAllowMining->setEnabled(enabled);
    chkLeftOnlyWithSword->setEnabled(enabled);
    chkLeftOnlyWithAxe->setEnabled(enabled);
}

void SettingsMenu::updateRightEnabled(bool enabled) {
    spinRightInterval->setEnabled(enabled);
    spinRightRandMin->setEnabled(enabled);
    spinRightRandMax->setEnabled(enabled);
    chkRightOnlyWithBlock->setEnabled(enabled);
}

void SettingsMenu::loadSettings() {
    chkLeftEnabled->setChecked(settings.value("leftEnabled", false).toBool());
    spinLeftInterval->setValue(settings.value("leftInterval", 100).toInt());
    spinLeftRandMin->setValue(settings.value("leftRandMin", 0).toInt());
    spinLeftRandMax->setValue(settings.value("leftRandMax", 0).toInt());
    chkLeftAllowMining->setChecked(settings.value("leftAllowMining", false).toBool());
    chkLeftOnlyWithSword->setChecked(settings.value("leftOnlyWithSword", false).toBool());
    chkLeftOnlyWithAxe->setChecked(settings.value("leftOnlyWithAxe", false).toBool());

    chkRightEnabled->setChecked(settings.value("rightEnabled", false).toBool());
    spinRightInterval->setValue(settings.value("rightInterval", 100).toInt());
    spinRightRandMin->setValue(settings.value("rightRandMin", 0).toInt());
    spinRightRandMax->setValue(settings.value("rightRandMax", 0).toInt());
    chkRightOnlyWithBlock->setChecked(settings.value("rightOnlyWithBlock", false).toBool());

    updateLeftEnabled(chkLeftEnabled->isChecked());
    updateRightEnabled(chkRightEnabled->isChecked());
}

uint8_t SettingsMenu::leftHeldItemMask() const {
    uint8_t mask = 0;
    if (chkLeftOnlyWithSword->isChecked()) mask |= (1 << static_cast<uint8_t>(HeldItem::SWORD));
    if (chkLeftOnlyWithAxe->isChecked())   mask |= (1 << static_cast<uint8_t>(HeldItem::AXE));
    return mask;
}

void SettingsMenu::saveSettings() {
    settings.setValue("leftEnabled",       chkLeftEnabled->isChecked());
    settings.setValue("leftInterval",      spinLeftInterval->value());
    settings.setValue("leftRandMin",       spinLeftRandMin->value());
    settings.setValue("leftRandMax",       spinLeftRandMax->value());
    settings.setValue("leftAllowMining",   chkLeftAllowMining->isChecked());
    settings.setValue("leftOnlyWithSword", chkLeftOnlyWithSword->isChecked());
    settings.setValue("leftOnlyWithAxe",   chkLeftOnlyWithAxe->isChecked());

    settings.setValue("rightEnabled",        chkRightEnabled->isChecked());
    settings.setValue("rightInterval",       spinRightInterval->value());
    settings.setValue("rightRandMin",        spinRightRandMin->value());
    settings.setValue("rightRandMax",        spinRightRandMax->value());
    settings.setValue("rightOnlyWithBlock",  chkRightOnlyWithBlock->isChecked());

    settings.sync();
    emit settingsChanged();
    emit goBack();
}

void SettingsMenu::resetDefaults() {
    chkLeftEnabled->setChecked(false);
    spinLeftInterval->setValue(100);
    spinLeftRandMin->setValue(0);
    spinLeftRandMax->setValue(0);
    chkLeftAllowMining->setChecked(false);
    chkLeftOnlyWithSword->setChecked(false);
    chkLeftOnlyWithAxe->setChecked(false);

    chkRightEnabled->setChecked(false);
    spinRightInterval->setValue(100);
    spinRightRandMin->setValue(0);
    spinRightRandMax->setValue(0);
    chkRightOnlyWithBlock->setChecked(false);
}

bool SettingsMenu::leftEnabled()      const { return chkLeftEnabled->isChecked(); }
int  SettingsMenu::leftIntervalMs()   const { return spinLeftInterval->value(); }
int  SettingsMenu::leftRandMin()      const { return spinLeftRandMin->value(); }
int  SettingsMenu::leftRandMax()      const { return spinLeftRandMax->value(); }
bool SettingsMenu::leftAllowMining()  const { return chkLeftAllowMining->isChecked(); }

bool SettingsMenu::rightEnabled()       const { return chkRightEnabled->isChecked(); }
int  SettingsMenu::rightIntervalMs()    const { return spinRightInterval->value(); }
int  SettingsMenu::rightRandMin()       const { return spinRightRandMin->value(); }
int  SettingsMenu::rightRandMax()       const { return spinRightRandMax->value(); }
bool SettingsMenu::rightOnlyWithBlock() const { return chkRightOnlyWithBlock->isChecked(); }
