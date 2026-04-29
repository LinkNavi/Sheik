#pragma once

#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <cstdint>
#include <iostream>

#pragma pack(push, 1)
struct GameState {
    int32_t lookingAtBlock;
    int32_t heldItem;
    float yaw;
    float pitch;
    int32_t targetId;
    float targetHealth;
    uint8_t inGui;
};
#pragma pack(pop)

static_assert(sizeof(GameState) == 25, "Unexpected padding in GameState");

class SheikIPC {
public:
    explicit SheikIPC(const char* shmPath = "/dev/shm/sheik");
    ~SheikIPC();

    // Delete copy/move — mmap region shouldn't be duplicated
    SheikIPC(const SheikIPC&) = delete;
    SheikIPC& operator=(const SheikIPC&) = delete;
    SheikIPC(SheikIPC&&) = delete;
    SheikIPC& operator=(SheikIPC&&) = delete;

    void update();  // Refresh read with memory barrier
    const GameState* state() const { return state_; }

    bool valid() const { return state_ != nullptr; }

private:
    int fd_ = -1;
    void* mapped_ = nullptr;
    size_t size_ = sizeof(GameState);
    GameState* state_ = nullptr;
};
